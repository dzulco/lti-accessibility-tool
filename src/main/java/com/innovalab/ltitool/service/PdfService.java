package com.innovalab.ltitool.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.innovalab.ltitool.config.MoodleWsProperties;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;


@Service
public class PdfService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final MoodleWsProperties props;

    public PdfService(MoodleWsProperties props) {
        this.props = props;
    }

    public String extractPdfUrl(JsonNode sections, Integer sectionId) {

        for (JsonNode section : sections) {

            if (section.get("section").asInt() != sectionId)
                continue;

            for (JsonNode module : section.get("modules")) {

                if (!"resource".equals(module.get("modname").asText()))
                    continue;

                JsonNode contents = module.get("contents");

                if (contents != null && contents.size() > 0) {

                    JsonNode file = contents.get(0);

                    if ("application/pdf".equals(file.get("mimetype").asText())) {
                        return file.get("fileurl").asText();
                    }
                }
            }
        }

        throw new RuntimeException("PDF no encontrado");
    }

    public byte[] downloadPdf(String fileUrl) {

        // agregar token SI es necesario
        String finalUrl = fileUrl + (fileUrl.contains("?") ? "&" : "?")
                + "token=" + props.getToken();

        return restTemplate.getForObject(finalUrl, byte[].class);
    }
}
