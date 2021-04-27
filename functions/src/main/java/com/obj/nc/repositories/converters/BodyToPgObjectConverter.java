package com.obj.nc.repositories.converters;

import com.obj.nc.domain.Body;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
import org.postgresql.util.PGobject;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;

@WritingConverter
@Log4j2
public class BodyToPgObjectConverter implements Converter<Body, PGobject> {
    
    private JsonNodeToPgObjectConverter jsonNodeToPgObjectConverter = new JsonNodeToPgObjectConverter();
    
    @Override
    public PGobject convert(@NonNull Body body) {
        return jsonNodeToPgObjectConverter.convert(body.toJSONNode());
    }
    
}
