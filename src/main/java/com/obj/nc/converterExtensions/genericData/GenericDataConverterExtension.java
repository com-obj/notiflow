package com.obj.nc.converterExtensions.genericData;

import com.obj.nc.converterExtensions.ConverterExtension;
import com.obj.nc.domain.dataObject.PulledNotificationData;

public interface GenericDataConverterExtension<PT, OUT> extends ConverterExtension<PulledNotificationData<PT>, OUT>{
    
    public Class<PT> getPayloadType();

}
