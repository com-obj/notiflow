package com.obj.nc.flows.dataSources.firestore;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.QuerySnapshot;
import com.obj.nc.Get;
import com.obj.nc.flows.dataSources.firestore.extensions.FirestoreQueryExtension;
import com.obj.nc.flows.dataSources.firestore.properties.FirestoreJobProperties;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.springframework.integration.endpoint.AbstractMessageSource;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class FirestoreChannelAdapter extends AbstractMessageSource<Object> {

    private final Firestore firestore;

    private final FirestoreJobProperties firestoreJobProperties;

    public FirestoreChannelAdapter(Firestore firestore, FirestoreJobProperties firestoreJobProperties) {
        this.firestore = firestore;
        this.firestoreJobProperties = firestoreJobProperties;
    }

    @SneakyThrows
    @Override
    protected Object doReceive() {
        CollectionReference collectionReference = firestore.collection(firestoreJobProperties.getCollectionName());
        Query query = collectionReference.offset(0);

        if (!StringUtils.isBlank(firestoreJobProperties.getQueryExtensionBeanName())) {
            FirestoreQueryExtension queryExtension = Get.getApplicationContext().getBean(firestoreJobProperties.getQueryExtensionBeanName(), FirestoreQueryExtension.class);
            query = queryExtension.createCustomQuery(collectionReference);
        }

        if (!firestoreJobProperties.getSelectedProperties().isEmpty()) {
            String[] fieldsToSelect = firestoreJobProperties.getSelectedProperties().toArray(new String[0]);
            query = query.select(fieldsToSelect);
        }

        ApiFuture<QuerySnapshot> future = query.get();

        List<Map<String, Object>> result = future
                .get()
                .getDocuments()
                .stream()
                .map(docSnapshot -> {
                    String id = docSnapshot.getId();
                    Map<String, Object> data = docSnapshot.getData();
                    data.put("id", id);
                    return new TreeMap<>(data);
                }).collect(Collectors.toList());

        return result;
    }

    @Override
    public String getComponentType() {
        return "firebase";
    }
}
