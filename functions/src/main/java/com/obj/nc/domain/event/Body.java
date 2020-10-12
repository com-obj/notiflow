package com.obj.nc.domain.event;

import java.util.ArrayList;
import java.util.List;

import com.obj.nc.domain.message.Message;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=true)
public class Body extends BaseJSONObject{

	Message message = new Message();
	List<Attachement> attachments = new ArrayList<Attachement>();

}
