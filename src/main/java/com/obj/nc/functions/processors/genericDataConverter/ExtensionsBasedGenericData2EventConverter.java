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

package com.obj.nc.functions.processors.genericDataConverter;

import com.obj.nc.converterExtensions.genericData.GenericData2EventConverterExtension;
import com.obj.nc.converterExtensions.genericData.GenericDataConverterExtension;
import com.obj.nc.domain.event.GenericEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ExtensionsBasedGenericData2EventConverter extends BaseExtensionsBasedGenericDataConverter<GenericEvent> {
	
	@Autowired(required = false)
	private final List<GenericData2EventConverterExtension<?>> converters = new ArrayList<>();

	@Override
	public List<? extends GenericDataConverterExtension<?, GenericEvent>> getConverterExtensions() {
		return converters;
	}


	
	
	
}
