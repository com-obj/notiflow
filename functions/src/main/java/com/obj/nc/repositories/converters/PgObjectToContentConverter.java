package com.obj.nc.repositories.converters;

import org.postgresql.util.PGobject;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;

import com.obj.nc.domain.content.Content;
import com.obj.nc.utils.JsonUtils;

import lombok.NonNull;
import lombok.extern.log4j.Log4j2;

@ReadingConverter
@Log4j2
public class PgObjectToContentConverter implements Converter<PGobject, Content> {
    
    private PgObjectToJsonNodeConverter pgObjectToJsonNodeConverter = new PgObjectToJsonNodeConverter();
    
    @Override
    public Content convert(@NonNull PGobject jsonNode) {
        return JsonUtils.readObjectFromJSON(pgObjectToJsonNodeConverter.convert(jsonNode), Content.class);
    }
    
}
