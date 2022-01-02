package com.obj.nc.converterExtensions.pullNotifData;

import com.obj.nc.converterExtensions.ConverterExtension;
import com.obj.nc.domain.pullNotifData.PullNotifData;

public interface PullNotifDataConverterExtension<PT, OUT> extends ConverterExtension<PullNotifData<PT>, OUT>{
    
    public Class<PT> getPayloadType();

}
