package com.innovalab.ltitool.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
public class AiService {

    private final ChatClient chatClient;

    public AiService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    public String generateQuizFromText(String text) {
        String query = "Analiza el texto y devuelve un JSON que contenga un arreglo llamado 'quiz'. \n" +
                "Cada ítem debe tener exactamente las propiedades: 'pregunta', 'opciones' (un arreglo de 4 strings) y \n" +
                " 'respuestaCorrecta'. Debe tener exactamente 10 preguntas. No agregues texto adicional ni marcas de Markdown. Texto: ";

        return executeAiCall(query + text);
    }

    public String summarizeText(String text) {
        String query = "Actúa como un Rol experto editor, no quiero tu respuesta tenga asteriscos. " +
                "Resume el siguiente texto: ";

        return executeAiCall(query + text);
    }

    public String explainText(String text) {
        String query = "Actúa como un Rol experto profesor. Estudia el siguiente texto académico y explicamelo con otras " +
                "palabras porque no lo entiendo, no quiero tu respuesta tenga asteriscos y no hagas preguntas al final. Texto: ";

        return executeAiCall(query + text);
    }

    public String generateFlashcards(String text) {
        String query = "Actúa como un profesor experto en educación universitaria.\n" +
                "        Tu tarea es analizar el siguiente texto académico y extraer los conceptos clave para crear flashcards (tarjetas de memoria).\n" +
                "\n" +
                "        CONTEXTO DEL USUARIO:\n" +
                "        Tus alumnos necesitan repasar y memorizar mediante el método de \"recuperación activa\" (Active Recall). Por lo tanto, las tarjetas deben ser simples, directas y enfocadas en un solo concepto.\n" +
                "\n" +
                "        REGLAS ESTRICTAS:\n" +
                "        1. Extrae un máximo de 8 conceptos fundamentales del texto.\n" +
                "        2. Cada concepto debe tener el siguiente formato estricto de JSON (para que mi programa lo procese fácilmente):\n" +
                "[\n" +
                "        {\n" +
                "            \"frente\": \"Pregunta clara, definición incompleta o palabra clave.\",\n" +
                "                \"reverso\": \"Respuesta exacta, breve y precisa basada únicamente en el texto.\"\n" +
                "        }\n" +
                "]\n" +
                "        3. NO inventes información, no agregues introducciones, saludos ni explicaciones. Devuelve SOLAMENTE el formato JSON solicitado.\n" +
                "\n" +
                "        TEXTO A ANALIZAR: ";

        return executeAiCall(query + text);
    }

   public Flux<String> generateTitleAndSections(String text) {

    System.out.println(">>> LONGITUD DEL TEXTO RECIBIDO EN JAVA: "
            + (text != null ? text.length() : 0));

    String query = """
            Eres un experto en analizar documentos educativos.

            Tu tarea consiste en dividir el documento en secciones lógicas y generar un resumen de cada una.

            REGLAS:

            1. Responde ÚNICAMENTE con un JSON válido.
            2. No escribas explicaciones.
            3. No utilices Markdown.
            4. No agregues texto antes ni después del JSON.
            5. No inventes información que no aparezca en el documento.
            6. Utiliza títulos claros y representativos.
            7. Cada resumen debe tener entre 80 y 120 palabras.
            8. Conserva los conceptos más importantes de cada sección.
            9. Utiliza un lenguaje claro y sencillo.

            El JSON debe tener exactamente esta estructura:

            {
              "titulo": "",
              "subtitulo": "",
              "secciones": [
                {
                  "titulo_seccion": "",
                  "resumen": ""
                }
              ]
            }

            Documento:

            ----------------------------------------
            """ + text + """
            ----------------------------------------
            """;

    return chatClient.prompt(query)
            .stream()
            .content();
}


    // Centralized private method with error handling
    private String executeAiCall(String prompt) {
        try {
            return chatClient.prompt(prompt)
                    .call()
                    .content();
        } catch (Exception e) {
            throw new RuntimeException("Error de comunicación con la IA en el AiService: " + e.getMessage(), e);
        }
    }
}
