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

package com.obj.nc.domain.content;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.obj.nc.domain.BaseDynamicAttributesBean;
import com.obj.nc.domain.content.email.EmailContent;
import com.obj.nc.domain.dto.content.MessageContentDto;
import lombok.Data;
import lombok.EqualsAndHashCode;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, defaultImpl = EmailContent.class)
@Data
@EqualsAndHashCode(callSuper = false)
public abstract class MessageContent extends BaseDynamicAttributesBean {
    public abstract MessageContentDto toDto();
}
