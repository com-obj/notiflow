package com.obj.nc.converterExtensions.genericData;

import com.obj.nc.converterExtensions.ConverterExtension;
import com.obj.nc.domain.dataObject.GenericData;

public interface GenericDataConverterExtension<PT, OUT> extends ConverterExtension<GenericData<PT>, OUT>{
    
    public Class<PT> getPayloadType();

}
