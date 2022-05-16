package com.obj.nc.flows.dataSources.firestore.extensions;

import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.Query;

public interface FirestoreQueryExtension {

    Query createCustomQuery(CollectionReference collectionReference);
}
