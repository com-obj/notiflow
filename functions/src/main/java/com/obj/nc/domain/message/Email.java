package com.obj.nc.domain.message;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.obj.nc.domain.Attachement;
import com.obj.nc.domain.BaseJSONObject;
import com.obj.nc.domain.endpoints.EmailEndpoint;
import com.obj.nc.domain.endpoints.MailChimpEndpoint;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Data
@NoArgsConstructor
@RequiredArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class Email extends Content {
	
	public final static String JSON_TYPE_IDENTIFIER = "EMAIL_MESSAGE_CONTENT";

	public static final String TEXT_CONCAT_DELIMITER = "\n\n";
	public static final String SUBJECT_CONCAT_DELIMITER = ", ";

	@NonNull
	@EqualsAndHashCode.Include
	private String subject;
	
	@NonNull
	@EqualsAndHashCode.Include
	private String text;

	@EqualsAndHashCode.Include
	private List<Attachement> attachments = new ArrayList<Attachement>();

	public Email concat(Email other) {
		Email concated = new Email();
		concated.text = text.concat(TEXT_CONCAT_DELIMITER).concat(other.text);
		concated.subject = subject.concat(SUBJECT_CONCAT_DELIMITER).concat(other.subject);
		concated.attachments.addAll(attachments);
		concated.attachments.addAll(other.attachments);
		return concated;
	}

}
