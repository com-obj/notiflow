package com.obj.nc.domain.content.sms;

import com.fasterxml.jackson.annotation.JsonTypeName;
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
@JsonTypeName(SimpleTextContent.JSON_TYPE_IDENTIFIER)
public class SimpleTextContent extends Content implements Cloneable {
	
	public final static String JSON_TYPE_IDENTIFIER = "TEXT_CONTENT";

	public static final String TEXT_CONCAT_DELIMITER = "\n\n";

	@NonNull
	@EqualsAndHashCode.Include
	private String text;

	public SimpleTextContent concat(SimpleTextContent other) {
		SimpleTextContent concated = new SimpleTextContent();
		concated.text = text.concat(TEXT_CONCAT_DELIMITER).concat(other.text);
		return concated;
	}
	
	@Override
	public SimpleTextContent clone() throws CloneNotSupportedException {
		return (SimpleTextContent) super.clone(); // return deep copy
	}

}
