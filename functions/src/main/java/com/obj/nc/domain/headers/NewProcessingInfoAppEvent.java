package com.obj.nc.domain.headers;

import lombok.Data;

@Data
public class NewProcessingInfoAppEvent {
	
	private final Header header;
	
	//Dirty Hack: Spring fires notifications of this type outside of my intentional firing. How: no fucking idea... 
	//Debuged, traced, loged,.. nothign help.
	private boolean ready = false; 

	public void makeReady() {
		this.ready = true;
	}

}
