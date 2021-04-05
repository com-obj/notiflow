package com.obj.nc.koderia.mapper;

import com.obj.nc.domain.message.Message;
import com.obj.nc.koderia.dto.mailchimp.SendMessageWithTemplateDto;

public interface MailchimpMessageMapper {

    SendMessageWithTemplateDto mapWithTemplate(Message message);

}
