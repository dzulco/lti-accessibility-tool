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
    // Log para auditar en Spring Boot exactamente qué llega
    System.out.println(">>> LONGITUD DEL TEXTO RECIBIDO EN JAVA: " + (text != null ? text.length() : 0));

    String query = """
            Tu única tarea es analizar el texto suministrado, segmentarlo y devolver un objeto JSON estructurado.

            REGLAS CRÍTICAS DE CONTENIDO (OBLIGATORIAS):
            1. CONSERVACIÓN ÍNTEGRA: El campo "contenido" de cada sección DEBE incluir el texto VERBATIM (literal, palabra por palabra) del documento original.
            2. PROHIBIDO RESUMIR: Queda estrictamente prohibido resumir, sintetizar, parafrasear, omitir oraciones o acortar información. Todo el texto de entrada debe quedar repartido entre las secciones.
            3. COPIAR Y PEGAR: Trata el texto original como inmutable. Solo debes identificar dónde empieza/termina cada sección y colocar la totalidad de ese texto dentro de su respectivo "contenido".

            REGLAS ESTRICTAS DE FORMATO:
            1. Responde ÚNICAMENTE con el objeto JSON válido. 
            2. PROHIBIDO incluir introducciones, saludos, disculpas, notas o bloques de markdown (como ```json).
            3. Comienza directamente con '{' y termina con '}'.
            4. Si utilizas comillas dobles dentro del texto de "contenido", asegúrate de escaparlas adecuadamente con \\" para no romper el JSON.

            ESTRUCTURA DEL JSON:
            {
              "titulo": "Título representativo",
              "subtitulo": "Subtítulo representativo",
              "secciones": [
                {
                  "titulo_seccion": "Título de la sección",
                  "contenido": "Texto literal, completo y sin resumir de esta sección"
                }
              ]
            }

            A continuación se presenta el contenido a procesar:
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
