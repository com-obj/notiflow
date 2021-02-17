package com.obj.nc.mapper;

import com.obj.nc.domain.message.Message;
import com.obj.nc.dto.mailchimp.SendMessageDto;
import com.obj.nc.dto.mailchimp.SendMessageWithTemplateDto;

public interface MailchimpMessageMapper {

    SendMessageDto map(Message message);

    SendMessageWithTemplateDto mapWithTemplate(Message message);

}
