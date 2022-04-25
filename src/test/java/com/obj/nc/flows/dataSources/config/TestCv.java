package com.obj.nc.flows.dataSources.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.obj.nc.flows.dataSources.firestore.serialization.TimestampToInstantDeserializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestCv {
    private String id;

    @JsonDeserialize(using = TimestampToInstantDeserializer.class)
    @JsonProperty("updatedAt")
    private Instant updatedAt;

    public boolean isExpired(int numberOfDaysBeforeExpiration) { //use instead of complicated SPeL expression
        return updatedAt.isBefore(Instant.now().plus(numberOfDaysBeforeExpiration, ChronoUnit.DAYS));
    }
}
