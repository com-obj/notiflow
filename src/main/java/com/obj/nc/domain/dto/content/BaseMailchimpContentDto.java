package com.obj.nc.domain.dto.content;

import com.obj.nc.domain.Attachment;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public abstract class BaseMailchimpContentDto extends MessageContentDto {

    private String subject;
    private List<Attachment> attachments = new ArrayList<>();
}
