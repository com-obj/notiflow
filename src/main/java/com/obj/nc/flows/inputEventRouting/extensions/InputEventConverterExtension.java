/*
 *   Copyright (C) 2021 the original author or authors.
 *
 *   This file is part of Notiflow
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.obj.nc.flows.inputEventRouting.extensions;

import java.util.List;
import java.util.Optional;

import com.obj.nc.domain.IsNotification;
import com.obj.nc.domain.event.GenericEvent;
import com.obj.nc.exceptions.PayloadValidationException;

/**
 * Will need to allow mapping specific instances of this class to flow_id
 * @author Jan Cuzy
 *
 * @param <RESULT_TYPE>
 */
public interface InputEventConverterExtension<RESULT_TYPE extends IsNotification> {
	
	/**
	 * 
	 * @param payload
	 * @return 
	 * 		Optional.emtpy() if this extensions is capable of making payload->RESULT_TYPE transformation
	 * 		Optional.of(new PayloadValidationException("Error description") if not (error description will be logged)	
	 */
	Optional<PayloadValidationException> canHandle(GenericEvent payload);
	
	List<RESULT_TYPE> convertEvent(GenericEvent event);
	
}
