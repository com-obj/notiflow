package com.obj.nc.flows.dataSources.firestore;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import com.obj.nc.flows.dataSources.firestore.properties.FirestoreDataSourceProperties;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class FirestoreFactoryImpl implements FirestoreFactory {

    private static final Logger log = LoggerFactory.getLogger(FirestoreFactoryImpl.class);

    private final Map<String, FirebaseApp> nameToFirebaseAppMap = new ConcurrentHashMap<>();

    @Override
    public Firestore getFirestore(FirestoreDataSourceProperties dataSourceProperties) {
        FirebaseApp firebaseApp = initializeOrGetFirebaseApp(dataSourceProperties);
        Firestore firestore = FirestoreClient.getFirestore(firebaseApp);

        return firestore;
    }

    @SneakyThrows
    private FirebaseApp initializeOrGetFirebaseApp(FirestoreDataSourceProperties dataSourceProperties) {
        FirebaseApp firebaseApp = nameToFirebaseAppMap.get(dataSourceProperties.getAppName());

        if (firebaseApp == null) {
            synchronized (nameToFirebaseAppMap) {
                firebaseApp = nameToFirebaseAppMap.get(dataSourceProperties.getAppName());

                if (firebaseApp == null) {
                    try {
                        // FirebaseApp instance already exists
                        firebaseApp = FirebaseApp.getInstance(dataSourceProperties.getAppName());
                        nameToFirebaseAppMap.put(dataSourceProperties.getAppName(), firebaseApp);
                        log.info("FirebaseApp instance {} already exists and will be reused", firebaseApp);
                        return firebaseApp;
                    } catch (IllegalStateException e) {
                        // not initialized, OK
                    }
                    FileInputStream serviceAccount = new FileInputStream(dataSourceProperties.getServiceKeyPath());

                    FirebaseOptions options = FirebaseOptions.builder()
                            .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                            .setDatabaseUrl(dataSourceProperties.getDatabaseUrl())
                            .build();

                    log.info("Initializing new FirebaseApp instance");
                    firebaseApp = FirebaseApp.initializeApp(options, dataSourceProperties.getAppName());
                    nameToFirebaseAppMap.put(dataSourceProperties.getAppName(), firebaseApp);
                }
            }
        }
        return firebaseApp;
    }
}
