package com.obj.nc.domain.event;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GenericEventWithStats {
    
    private GenericEvent event;
    private GenericEventStats stats;
    
}
