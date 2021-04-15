package com.obj.nc.domain.content.email;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.MediaType;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.obj.nc.domain.Attachement;
import com.obj.nc.domain.content.Content;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Data
@NoArgsConstructor
@RequiredArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@JsonTypeName(EmailContent.JSON_TYPE_IDENTIFIER)
public class EmailContent extends Content implements Cloneable {
	
	public final static String JSON_TYPE_IDENTIFIER = "EMAIL_MESSAGE_CONTENT";

	public static final String TEXT_CONCAT_DELIMITER = "\n\n";
	public static final String SUBJECT_CONCAT_DELIMITER = ", ";

	private String subject;
	
	@NonNull
	@EqualsAndHashCode.Include
	private String text;
	
	@EqualsAndHashCode.Include
	private String contentType=MediaType.TEXT_PLAIN_VALUE;

	@EqualsAndHashCode.Include
	private List<Attachement> attachments = new ArrayList<Attachement>();
	
	public static EmailContent createWithSubject(String subject, String text) {
		EmailContent emailContent = new EmailContent(text);
		emailContent.setSubject(subject);
		return emailContent;
	}

	public EmailContent concat(EmailContent other) {
		EmailContent concated = new EmailContent();
		concated.text = text.concat(TEXT_CONCAT_DELIMITER).concat(other.text);
		concated.subject = subject.concat(SUBJECT_CONCAT_DELIMITER).concat(other.subject);
		concated.attachments.addAll(attachments);
		concated.attachments.addAll(other.attachments);
		return concated;
	}
	
	@Override
	public EmailContent clone() throws CloneNotSupportedException {
		EmailContent clone = (EmailContent) super.clone();
		clone.attachments = new ArrayList<>(attachments);
		return clone; // return deep copy
	}

}
