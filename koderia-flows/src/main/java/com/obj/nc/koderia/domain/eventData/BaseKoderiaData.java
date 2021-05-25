package com.obj.nc.koderia.domain.eventData;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class BaseKoderiaData {
    public abstract Object asMailchimpMergeVarContent();
}
