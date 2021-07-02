package com.obj.nc.repositories.converters;

import org.postgresql.util.PGobject;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;

import com.obj.nc.domain.notifIntent.content.IntentContent;
import com.obj.nc.utils.JsonUtils;

import lombok.NonNull;
import lombok.extern.log4j.Log4j2;

@ReadingConverter
@Log4j2
public class PgObjectToIntentContentConverter implements Converter<PGobject, IntentContent> {
    
    private PgObjectToJsonNodeConverter pgObjectToJsonNodeConverter = new PgObjectToJsonNodeConverter();
    
    @Override
    public IntentContent convert(@NonNull PGobject jsonNode) {
        return JsonUtils.readObjectFromJSON(pgObjectToJsonNodeConverter.convert(jsonNode), IntentContent.class);
    }
    
}
