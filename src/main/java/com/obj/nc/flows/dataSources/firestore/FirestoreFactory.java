package com.obj.nc.flows.dataSources.firestore;

import com.google.cloud.firestore.Firestore;
import com.obj.nc.flows.dataSources.firestore.properties.FirestoreDataSourceProperties;

public interface FirestoreFactory {

    Firestore getFirestore(FirestoreDataSourceProperties dataSourceProperties);
}
