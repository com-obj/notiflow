package com.obj.nc.domain.message;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.obj.nc.domain.Attachement;
import com.obj.nc.domain.BaseJSONObject;
import com.obj.nc.domain.event.Event;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@RequiredArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class MessageContent extends BaseJSONObject {

	public static final String TEXT_CONCAT_DELIMITER = "\n\n";
	public static final String SUBJECT_CONCAT_DELIMITER = ", ";

	@NonNull
	@EqualsAndHashCode.Include
	private String text;

	@NonNull
	@EqualsAndHashCode.Include
	private String subject;

	@EqualsAndHashCode.Include
	private List<Attachement> attachments = new ArrayList<Attachement>();

	public MessageContent concat(MessageContent other) {
		MessageContent concated = new MessageContent();
		concated.text = text.concat(TEXT_CONCAT_DELIMITER).concat(other.text);
		concated.subject = subject.concat(SUBJECT_CONCAT_DELIMITER).concat(other.subject);
		concated.attachments.addAll(attachments);
		concated.attachments.addAll(other.attachments);
		return concated;
	}

}
