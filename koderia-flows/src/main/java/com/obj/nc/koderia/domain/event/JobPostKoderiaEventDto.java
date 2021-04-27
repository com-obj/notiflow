package com.obj.nc.koderia.domain.event;

import com.obj.nc.koderia.domain.eventData.JobPostEventDataDto;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Data
@EqualsAndHashCode(callSuper = true)
public class JobPostKoderiaEventDto extends BaseKoderiaEvent {
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

