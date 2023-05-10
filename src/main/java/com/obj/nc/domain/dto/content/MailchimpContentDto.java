package com.obj.nc.domain.dto.content;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.obj.nc.domain.Attachment;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false, of = "html")
@JsonTypeName("MAILCHIMP")
public class MailchimpContentDto extends BaseMailchimpContentDto {

    private String html;

    public static MailchimpContentDto create(String html, String subject, List<Attachment> attachments) {
        MailchimpContentDto dto = new MailchimpContentDto();
        dto.setHtml(html);
        dto.setSubject(subject);
        dto.setAttachments(attachments);
        return dto;
    }
}
