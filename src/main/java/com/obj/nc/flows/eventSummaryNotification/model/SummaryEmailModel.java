package com.obj.nc.flows.eventSummaryNotification.model;

import java.util.List;

import com.obj.nc.domain.dto.DeliveryStatsByEndpointType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SummaryEmailModel {
    
    private String eventName;
    private String eventDescription;
    private List<DeliveryStatsByEndpointType> eventStatsByType;
}
