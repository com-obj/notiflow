package com.obj.nc.koderia.domain.event;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.obj.nc.koderia.domain.eventData.JobPostEventDataDto;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import static com.obj.nc.koderia.domain.event.JobPostKoderiaEventDto.JSON_TYPE_NAME;

@Data
@EqualsAndHashCode(callSuper = true)
@JsonTypeName(JSON_TYPE_NAME)
public class JobPostKoderiaEventDto extends BaseKoderiaEvent {
    public static final String JSON_TYPE_NAME = "JOB_POST";
    
    @Valid @NotNull private JobPostEventDataDto data;
    
    @Override
    public String getMessageSubject() {
        return data.getName();
    }
    
    @Override
    public String getMessageText() {
        return data.getDescription();
    }
}

