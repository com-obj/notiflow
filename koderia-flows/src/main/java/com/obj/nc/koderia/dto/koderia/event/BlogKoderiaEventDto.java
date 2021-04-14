package com.obj.nc.koderia.dto.koderia.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.obj.nc.koderia.dto.koderia.data.BlogEventDataDto;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Data
@EqualsAndHashCode(callSuper = true)
public class BlogKoderiaEventDto extends BaseKoderiaEventDto {
    @JsonProperty("data")
    @Valid @NotNull private BlogEventDataDto data;
    
    @Override
    public String getMessageSubject() {
        return data.getTitle();
    }
    
    @Override
    public String getMessageText() {
        return data.getContent();
    }
    
    @Override
    public String getTypeName() {
        return "BLOG";
    }
}

