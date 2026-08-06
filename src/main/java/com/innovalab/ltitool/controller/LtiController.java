package com.innovalab.ltitool.controller;

import com.innovalab.ltitool.service.LtiService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/lti")
public class LtiController {

    private final LtiService ltiService;

    public LtiController(LtiService ltiService) {
        this.ltiService = ltiService;
    }

    // ===================================================== //
    // OIDC LOGIN
    // ===================================================== //
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestParam Map<String, String> params) {
        System.out.println("\n========== LTI LOGIN ==========");
        params.forEach((k, v) -> System.out.println(k + " = " + v));

        try {
            String redirectUrl = ltiService.processLogin(params);

            System.out.println("\n========== REDIRECT LMS AUTH ==========");
            System.out.println(redirectUrl);

            return ResponseEntity
                    .status(HttpStatus.FOUND)
                    .header("Location", redirectUrl)
                    .build();

        } catch (IllegalArgumentException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
    }

    // ===================================================== //
    // LTI LAUNCH
    // ===================================================== //
    @PostMapping("/launch")
    public ResponseEntity<Void> launch(@RequestParam Map<String, String> params) {
        System.out.println("\n========== LTI LAUNCH ==========");

        try {
            // Genera la URL con los query params hacia  frontend
            String frontendRedirectUrl = ltiService.processLaunch(params);

            // Retorna un HTTP 302 Redirect con la cabecera Location
            return ResponseEntity
                    .status(HttpStatus.FOUND) // HTTP 302
                    .header("Location", frontendRedirectUrl)
                    .build();

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
}