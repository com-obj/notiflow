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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MailchimpMessageDto {
    
    @JsonProperty("subject") 
    private String subject;
    
    @JsonProperty("html")
    private String html;
    
    @JsonProperty("from_email") 
    private String fromEmail;
    
    @JsonProperty("from_name") 
    private String fromName;
    
    @JsonProperty("to") 
    private List<MailchimpRecipientDto> recipients;
    
    @JsonProperty("merge_language") 
    private String mergeLanguage;
    
    @JsonProperty("global_merge_vars") 
    private List<MailchimpMergeVariableDto> globalMergeVars;
    
    @JsonProperty("attachments") 
    private List<MailchimpAttachmentDto> attachments;
    
    @JsonProperty("track_opens")
    @Builder.Default
    private boolean trackOpens = false;
    
    @JsonProperty("track_clicks")
    @Builder.Default
    private boolean trackClicks = false;

}
