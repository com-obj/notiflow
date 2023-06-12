package com.obj.nc.flows.dataSources.firestore.properties;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class FirestoreJobProperties {

    @NotEmpty
    private String name;

    @NotEmpty
    private String collectionName;

    @NotEmpty
    private String cron;

    private List<String> selectedProperties = new ArrayList<>(0);

    private String pojoFCCN;

    private String spelFilterExpression;

    private String externalIdAttrName;

    private String queryExtensionBeanName;

    // empty == all
    private List<String> hashAttributes = new ArrayList<>();
}
