package com.obj.nc.mapper;

import com.obj.nc.domain.message.Message;
import com.obj.nc.dto.mailchimp.SendMessageWithTemplateDto;

public interface MailchimpMessageMapper {

    SendMessageWithTemplateDto mapWithTemplate(Message message);

}