package com.obj.nc.domain.endpoints;

import javax.validation.constraints.NotNull;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
public class MailChimpEndpoint extends RecievingEndpoint {

	@NotNull
	private String distributionList;
	@NotNull
	private Recipient recipient;
	
	public static MailChimpEndpoint create(String distributionList) {
		MailChimpEndpoint r = new MailChimpEndpoint();
		r.setDistributionList(distributionList);
		return r;
	}


	
}
