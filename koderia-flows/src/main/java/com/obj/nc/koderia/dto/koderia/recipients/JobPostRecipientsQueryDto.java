package com.obj.nc.koderia.dto.koderia.recipients;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

@Data
@Validated
@EqualsAndHashCode(callSuper = true)
public class JobPostRecipientsQueryDto extends RecipientsQueryDto {
    
    @NotNull @Valid private JobPostData data;
    
    @lombok.Data
    @EqualsAndHashCode(callSuper = true)
    static class JobPostData extends Data {
        @NotBlank private String type;
        @NotEmpty private List<String> technologies;
    }

}
