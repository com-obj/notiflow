package com.obj.nc.domain.endpoints;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper=false)
@NoArgsConstructor
public class MailChimpEndpoint extends RecievingEndpoint {
	
	public static final String JSON_TYPE_IDENTIFIER = "MAIL_CHIMP";

	@NotNull
	private String distributionListId;
	@NotNull
	private Recipient recipient;
	
	public static MailChimpEndpoint create(String distributionList) {
		MailChimpEndpoint r = new MailChimpEndpoint();
		r.setDistributionListId(distributionList);
		return r;
	}

	@Override
	@JsonIgnore
	public String getEndpointId() {
		return distributionListId;
	}

	@Override
	public String getEndpointTypeName() {
		return JSON_TYPE_IDENTIFIER;
	}
	
}
