package com.obj.nc.flows.dataSources.firestore.properties;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.UniqueElements;

import javax.validation.constraints.NotEmpty;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class FirestoreDataSourceProperties {

    private String name;

    @NotEmpty
    private String serviceKeyPath;

    @NotEmpty
    private String appName;

    @NotEmpty
    private String databaseUrl;

    @UniqueElements
    private List<FirestoreJobProperties> jobs = new ArrayList<>();
}
