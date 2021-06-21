package com.obj.nc.repositories.converters;

import com.obj.nc.domain.Body;
import com.obj.nc.utils.JsonUtils;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
import org.postgresql.util.PGobject;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;

@ReadingConverter
@Log4j2
public class PgObjectToBodyConverter implements Converter<PGobject, Body> {
    
    private PgObjectToJsonNodeConverter pgObjectToJsonNodeConverter = new PgObjectToJsonNodeConverter();
    
    @Override
    public Body convert(@NonNull PGobject jsonNode) {
        return JsonUtils.readObjectFromJSON(pgObjectToJsonNodeConverter.convert(jsonNode), Body.class);
    }
    
}
