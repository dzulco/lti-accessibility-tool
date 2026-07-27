package com.innovalab.ltitool.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.stereotype.Service;

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

    public String generateTitleAndSections(String text) {
    String query = """
        Analiza el siguiente texto extraído de un PDF y divídelo en secciones lógicas en formato JSON.

        REGLAS DE INTEGRIDAD (ESTRICTAS):
        1. NO cambies, agregues, resumenes ni omitas NINGUNA palabra. La unión de todas las secciones debe reconstruir el texto original al 100%.
        2. Conserva el orden exacto del texto original.
        3. Solo puedes asignar 'titulo', 'subtitulo' y los 'titulo_seccion' de cada parte.

        FORMATO DE SALIDA (JSON ÚNICAMENTE):
        {
          "titulo": "Título representativo",
          "subtitulo": "Subtítulo representativo",
          "secciones": [
            {
              "titulo_seccion": "Nombre de la sección",
              "contenido": "Fragmento exacto del texto original correspondientes a esta parte"
            }
          ]
        }

        TEXTO A PROCESAR:
        """ + text;

        return executeAiCall(query);
    }


    // Centralized private method with error handling
    private String executeAiCall(String prompt) {
        try {
            return chatClient.prompt(prompt)
                    .options(ChatOptions.builder()
                            .temperature(0.0)
                            .topP(0.1)
                            .build())
                    .call()
                    .content();
        } catch (Exception e) {
            // Throw a RuntimeException to be caught by the Global Exception Handler
            throw new RuntimeException("Error de comunicación con la IA en el AiService: " + e.getMessage(), e);
        }
    }
}
