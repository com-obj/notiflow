package com.obj.nc.domain.content.email;

import java.util.ArrayList;
import java.util.List;

import com.obj.nc.domain.content.MessageContent;
import com.obj.nc.domain.content.TrackableContent;
import org.springframework.http.MediaType;

import com.obj.nc.domain.Attachment;

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
public class EmailContent extends MessageContent implements TrackableContent {
	
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
	private List<Attachment> attachments = new ArrayList();
	
	@Override
	public boolean hasHtmlText() {
		return MediaType.TEXT_HTML_VALUE.equals(contentType) && getText() != null;
	}
	
	@Override
	public String getHtmlText() {
		return getText();
	}
	
	@Override
	public void setHtmlText(String text) {
		setText(text);
	}
	
}
