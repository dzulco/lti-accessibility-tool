package com.innovalab.ltitool.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.innovalab.ltitool.dto.LtiLaunchDTO;
import com.innovalab.ltitool.util.LtiMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LtiService {

    private final MoodleContentResolver moodleContentResolver;
    private final LtiPersistenceService ltiPersistenceService;

    @Value("${moodle.auth-url}")
    private String authUrl;

    @Value("${moodle.client-id}")
    private String expectedClientId;

    @Value("${app.react-url}")
    private String appReactURL;

    // Almacenes temporales en memoria (usar Redis o Session en producción)
    private final Map<String, Boolean> activeNonces = new ConcurrentHashMap<>();
    private final Map<String, Boolean> activeStates = new ConcurrentHashMap<>();

    public LtiService(MoodleContentResolver moodleContentResolver, LtiPersistenceService ltiPersistenceService) {
        this.moodleContentResolver = moodleContentResolver;
        this.ltiPersistenceService = ltiPersistenceService;
    }

    // ===================================================== //
    // PROCESAMIENTO DE LOGIN OIDC
    // ===================================================== //
    public String processLogin(Map<String, String> params) {
        // 1. Validar Client ID
        boolean isClientValid = Optional.ofNullable(params.get("client_id"))
                .map(expectedClientId::equals)
                .orElse(false);

        if (!isClientValid) {
            throw new IllegalArgumentException("Client ID no provisto o no válido.");
        }

        // 2. Extraer parámetros requeridos
        String targetLinkUri = Optional.ofNullable(params.get("target_link_uri"))
                .filter(s -> !s.isBlank())
                .orElseThrow(() -> new IllegalArgumentException("Parámetro 'target_link_uri' es obligatorio."));

        String loginHint = Optional.ofNullable(params.get("login_hint"))
                .filter(s -> !s.isBlank())
                .orElseThrow(() -> new IllegalArgumentException("Parámetro 'login_hint' es obligatorio."));

        // 3. Generar / Obtener state y nonce
        String state = Optional.ofNullable(params.get("state"))
                .filter(s -> !s.isBlank())
                .orElseGet(() -> UUID.randomUUID().toString());

        String nonce = Optional.ofNullable(params.get("nonce"))
                .filter(n -> !n.isBlank())
                .orElseGet(() -> UUID.randomUUID().toString());

        // Guardar state y nonce para validarlos posteriormente en /launch
        activeStates.put(state, Boolean.TRUE);
        activeNonces.put(nonce, Boolean.TRUE);

        // 4. Construir URL de Redirección
        StringBuilder redirectBuilder = new StringBuilder(authUrl)
                .append("?scope=openid")
                .append("&response_type=id_token")
                .append("&response_mode=form_post")
                .append("&client_id=").append(expectedClientId)
                .append("&redirect_uri=").append(URLEncoder.encode(targetLinkUri, StandardCharsets.UTF_8))
                .append("&login_hint=").append(loginHint)
                .append("&state=").append(state)
                .append("&nonce=").append(nonce);

        Optional.ofNullable(params.get("lti_message_hint"))
                .filter(s -> !s.isBlank())
                .ifPresent(hint -> redirectBuilder.append("&lti_message_hint=")
                        .append(URLEncoder.encode(hint, StandardCharsets.UTF_8)));

        return redirectBuilder.toString();
    }

    // ===================================================== //
    // PROCESAMIENTO DE LTI LAUNCH
    // ===================================================== //
    public String processLaunch(Map<String, String> params) {
        String idToken = params.get("id_token");
        String state = params.get("state");

        if (idToken == null || idToken.isBlank()) {
            throw new IllegalArgumentException("Falta el parámetro id_token.");
        }

        // 1. Validar 'state' (Anti-CSRF)
        if (state == null || activeStates.remove(state) == null) {
            throw new SecurityException("State inválido, expirado o ataque CSRF detectado.");
        }

        // 2. Decodificar JWT (Sin verificación de firma JWKS)
        DecodedJWT jwt = JWT.decode(idToken);

        // 3. Validar 'nonce' (Anti-Replay Attack)
        String nonce = jwt.getClaim("nonce").asString();
        if (nonce == null || activeNonces.remove(nonce) == null) {
            throw new SecurityException("Nonce inválido, caducado o ya utilizado.");
        }

        // 4. Validar Audience (Client ID)
        if (!jwt.getAudience().contains(expectedClientId)) {
            throw new SecurityException("La audiencia (aud) del token no coincide con el client_id.");
        }

        // 5. Mapeo y procesamiento de la sesión LTI
        LtiLaunchDTO dto = LtiMapper.fromJWT(jwt);
        moodleContentResolver.resolveSectionId(dto);
        ltiPersistenceService.saveLaunch(dto);

        // 6. Construir URL final hacia el Frontend
        return UriComponentsBuilder.fromUriString(appReactURL)
                .queryParam("userId", dto.getUserId())
                .queryParam("user", dto.getName())
                .queryParam("email", dto.getEmail())
                .queryParam("course", dto.getCourseTitle())
                .queryParam("section", dto.getSectionTitle())
                .queryParam("pdfUrl", dto.getPdfUrl())
                .build()
                .toUriString();
    }
}