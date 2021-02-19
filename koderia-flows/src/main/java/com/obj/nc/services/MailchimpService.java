package com.obj.nc.services;

import com.obj.nc.dto.mailchimp.MessageResponseDto;
import com.obj.nc.dto.mailchimp.SendMessageWithTemplateDto;

import java.util.List;

public interface MailchimpService extends RestClient {

    List<MessageResponseDto> sendMessageWithTemplate(SendMessageWithTemplateDto sendMessageDto);

}
