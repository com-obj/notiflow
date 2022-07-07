package com.obj.nc.domain.dto.content;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.obj.nc.domain.Attachment;
import com.obj.nc.domain.dto.endpoint.EmailEndpointDto;
import com.obj.nc.domain.dto.endpoint.SmsEndpointDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.http.MediaType;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@JsonTypeName("EMAIL")
public class EmailContentDto extends MessageContentDto {

    public static final String JSON_TYPE_IDENTIFIER = "EMAIL";
    private String subject;
    private String text;
    private String contentType = MediaType.TEXT_PLAIN_VALUE;
    private List<Attachment> attachments = new ArrayList<>();

    public static EmailContentDto create(String subject, String text, String contentType, List<Attachment> attachments) {
        EmailContentDto dto = new EmailContentDto();
        dto.setSubject(subject);
        dto.setText(text);
        dto.setContentType(contentType);
        dto.setAttachments(attachments);
        return dto;
    }
}
