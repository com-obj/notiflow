package com.obj.nc.domain.content.email;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.MediaType;

import com.obj.nc.domain.Attachement;
import com.obj.nc.domain.content.MessageContent;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class EmailContent extends MessageContent {
	
	public static final String TEXT_CONCAT_DELIMITER = "\n\n";
	public static final String SUBJECT_CONCAT_DELIMITER = ", ";

	@EqualsAndHashCode.Include
	private String subject;
	
	@EqualsAndHashCode.Include
	private String text;
	
	@EqualsAndHashCode.Include
	@Builder.Default
	private String contentType=MediaType.TEXT_PLAIN_VALUE;

	@EqualsAndHashCode.Include
	@Builder.Default
	private List<Attachement> attachments = new ArrayList();
	
	
}
