package com.obj.nc.domain.message;

import com.fasterxml.jackson.annotation.JsonTypeName;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Data
@NoArgsConstructor
@RequiredArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@JsonTypeName(SimpleText.JSON_TYPE_IDENTIFIER)
public class SimpleText extends Content {
	
	public final static String JSON_TYPE_IDENTIFIER = "TEXT_CONTENT";

	public static final String TEXT_CONCAT_DELIMITER = "\n\n";

	@NonNull
	@EqualsAndHashCode.Include
	private String text;

	public SimpleText concat(SimpleText other) {
		SimpleText concated = new SimpleText();
		concated.text = text.concat(TEXT_CONCAT_DELIMITER).concat(other.text);
		return concated;
	}

}
