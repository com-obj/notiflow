package com.obj.nc.domain;

import java.util.List;
import java.util.UUID;

public interface HasPreviousMessageIds {
    
    List<UUID> getPreviousMessageIds();
    
}
