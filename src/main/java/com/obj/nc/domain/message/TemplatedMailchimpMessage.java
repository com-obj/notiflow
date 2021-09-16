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

package com.obj.nc.domain.message;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.obj.nc.domain.content.mailchimp.TemplatedMailchimpContent;
import com.obj.nc.domain.endpoints.MailchimpEndpoint;
import com.obj.nc.domain.endpoints.ReceivingEndpoint;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
@ToString(callSuper = false)
public class TemplatedMailchimpMessage extends Message<TemplatedMailchimpContent>  {
    public static final String JSON_TYPE_IDENTIFIER = "MAILCHIMP_TEMPLATE_MESSAGE";
    
    public TemplatedMailchimpMessage() {
        setBody(new TemplatedMailchimpContent());
    }
    
    @Override
    public List<MailchimpEndpoint> getReceivingEndpoints() {
        return (List<MailchimpEndpoint>) super.getReceivingEndpoints();
    }
    
    @Override
    @JsonIgnore
    public String getPayloadTypeName() {
        return JSON_TYPE_IDENTIFIER;
    }
    
    //TODO: refactor as class parameter
    @JsonIgnore
    public Class<? extends ReceivingEndpoint> getReceivingEndpointType() {
        return MailchimpEndpoint.class;
    }
    

    
}
