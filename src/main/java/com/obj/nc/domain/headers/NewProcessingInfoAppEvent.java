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

package com.obj.nc.domain.headers;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class NewProcessingInfoAppEvent {
	
	private final ProcessingInfo pi;
	
	//Dirty Hack: Spring fires notifications of this type outside of my intentional firing. How: no fucking idea... 
	//Debuged, traced, loged,.. nothign help.
	private boolean ready = false; 

	public void makeReady() {
		this.ready = true;
	}

}