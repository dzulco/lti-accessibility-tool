package com.innovalab.ltitool.config;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.InputStream;

@Configuration
public class FirebaseConfig {

    @Bean
    public Firestore firestore() {
        try {
            // Lee el archivo JSON desde la carpeta 'src/main/resources'
            InputStream serviceAccount = getClass()
                    .getClassLoader()
                    .getResourceAsStream("lti-external-tool-firebase-adminsdk-fbsvc-2a5e705315.json");

            if (serviceAccount == null) {
                throw new RuntimeException("No se encontró el archivo firebase-service-account.json en resources");
            }

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            // Evita inicializar duplicados si Spring recarga el contexto
            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
                System.out.println(">>> Firebase Admin SDK inicializado con éxito.");
            }

            // Retornamos la instancia de Firestore como un Bean de Spring
            return FirestoreClient.getFirestore();

        } catch (Exception e) {
            System.err.println("Error crítico al inicializar Firebase: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
