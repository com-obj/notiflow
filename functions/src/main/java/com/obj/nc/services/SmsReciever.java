package com.obj.nc.services;

import java.util.List;

public interface SmsReciever<REQUEST_T, RESPONSE_T> {

	RESPONSE_T receive(REQUEST_T request);

	List<REQUEST_T> getAllRequests();

	void reset();

}