package com.obj.nc.koderia.services;

import java.util.List;

import com.obj.nc.koderia.dto.mailchimp.MessageResponseDto;
import com.obj.nc.koderia.dto.mailchimp.SendMessageWithTemplateDto;

public interface MailchimpClient extends RestClient {

    List<MessageResponseDto> sendMessageWithTemplate(SendMessageWithTemplateDto sendMessageDto);

}
