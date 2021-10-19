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

package com.obj.nc.converterExtensions;

import com.obj.nc.exceptions.PayloadValidationException;

import java.util.List;
import java.util.Optional;

/**
 * This is like marker interface. In reality the implementations use only the direct subtypes. We might remove it at some point
 * @author Jan Cuzy
 *
 * @param <RESULT_TYPE>
 */
public interface ConverterExtension<IN, OUT> {
    
    /**
     *
     * @param payload
     * @return
     * 		Optional.emtpy() if this extensions is capable of making payload->OUT transformation
     * 		Optional.of(new PayloadValidationException("Error description") if not (error description will be logged)	
     */
    Optional<PayloadValidationException> canHandle(IN payload);
    
    List<OUT> convert(IN payload);
    
}
