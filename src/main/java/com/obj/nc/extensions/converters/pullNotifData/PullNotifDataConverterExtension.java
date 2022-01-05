package com.obj.nc.extensions.converters.pullNotifData;

import com.obj.nc.domain.pullNotifData.PullNotifData;
import com.obj.nc.extensions.converters.ConverterExtension;

public interface PullNotifDataConverterExtension<PT, OUT> extends ConverterExtension<PullNotifData<PT>, OUT>{
    
    public Class<PT> getPayloadType();

}
