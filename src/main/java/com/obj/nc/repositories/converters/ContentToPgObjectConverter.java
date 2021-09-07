package com.obj.nc.repositories.converters;

import org.postgresql.util.PGobject;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;

import com.obj.nc.domain.content.MessageContent;

import lombok.NonNull;

@WritingConverter
public class ContentToPgObjectConverter implements Converter<MessageContent, PGobject> {
    
    private JsonNodeToPgObjectConverter jsonNodeToPgObjectConverter = new JsonNodeToPgObjectConverter();
    
    @Override
    public PGobject convert(@NonNull MessageContent body) {
        return jsonNodeToPgObjectConverter.convert(body.toJSONNode());
    }
    
}
