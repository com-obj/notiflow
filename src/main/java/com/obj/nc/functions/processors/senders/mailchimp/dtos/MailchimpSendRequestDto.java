/*
 *   Copyright (C) 2021 the original author or authors.
 *
 *   This file is part of Notiflow
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Lesser General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Lesser General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public License
 *   along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.obj.nc.functions.processors.senders.mailchimp.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.NotNull;

import java.util.List;


@Data
@SuperBuilder
public class MailchimpSendRequestDto {
    
    @NotNull
    @JsonProperty("key")
    protected String key;
    
    @NotNull
    @JsonProperty("message")
    protected MailchimpMessageDto message;
    
    @JsonProperty("template_name")
    private String templateName;
    
    @JsonProperty("template_content")
    private List<MailchimpTemplateContentDto> templateContent;
    
}
