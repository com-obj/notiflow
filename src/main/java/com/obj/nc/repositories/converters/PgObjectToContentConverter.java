package com.obj.nc.repositories.converters;

import org.postgresql.util.PGobject;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;

import com.obj.nc.domain.content.MessageContent;
import com.obj.nc.utils.JsonUtils;

import lombok.NonNull;
import lombok.extern.log4j.Log4j2;

@ReadingConverter
@Log4j2
public class PgObjectToContentConverter implements Converter<PGobject, MessageContent> {
    
    private PgObjectToJsonNodeConverter pgObjectToJsonNodeConverter = new PgObjectToJsonNodeConverter();
    
    @Override
    public MessageContent convert(@NonNull PGobject jsonNode) {
        return JsonUtils.readObjectFromJSON(pgObjectToJsonNodeConverter.convert(jsonNode), MessageContent.class);
    }
    
}
