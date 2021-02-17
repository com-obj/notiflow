package com.obj.nc.services;

import com.obj.nc.dto.mailchimp.MessageResponseDto;
import com.obj.nc.dto.mailchimp.SendMessageDto;
import com.obj.nc.dto.mailchimp.SendMessageWithTemplateDto;

import java.util.List;

public interface MailchimpService {

    List<MessageResponseDto> sendMessage(SendMessageDto sendMessageDto);

    List<MessageResponseDto> sendMessageWithTemplate(SendMessageWithTemplateDto sendMessageDto);

}
