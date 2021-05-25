package com.obj.nc.repositories.converters;

import org.postgresql.util.PGobject;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;

import com.obj.nc.domain.content.email.EmailContent;

import lombok.NonNull;
import lombok.extern.log4j.Log4j2;

@WritingConverter
@Log4j2
public class EmailContentToPgObjectConverter implements Converter<EmailContent, PGobject> {
    
    private JsonNodeToPgObjectConverter jsonNodeToPgObjectConverter = new JsonNodeToPgObjectConverter();
    
    @Override
    public PGobject convert(@NonNull EmailContent body) {
        return jsonNodeToPgObjectConverter.convert(body.toJSONNode());
    }
    
}
