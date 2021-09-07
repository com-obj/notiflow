package com.obj.nc.repositories.converters;

import org.postgresql.util.PGobject;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;

import com.obj.nc.domain.content.MessageContent;
import com.obj.nc.utils.JsonUtils;

import lombok.NonNull;

@ReadingConverter
public class PgObjectToContentConverter implements Converter<PGobject, MessageContent> {
    
    private PgObjectToJsonNodeConverter pgObjectToJsonNodeConverter = new PgObjectToJsonNodeConverter();
    
    @Override
    public MessageContent convert(@NonNull PGobject jsonNode) {
        return JsonUtils.readObjectFromJSON(pgObjectToJsonNodeConverter.convert(jsonNode), MessageContent.class);
    }
    
}
