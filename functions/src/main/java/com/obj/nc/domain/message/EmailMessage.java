package com.obj.nc.domain.message;

import java.util.List;

import org.springframework.data.relational.core.mapping.Table;

import com.obj.nc.domain.content.email.EmailContent;
import com.obj.nc.domain.endpoints.EmailEndpoint;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
@ToString(callSuper = false)
@Table("nc_message")
public class EmailMessage extends Message<EmailContent> {

	@Override
	public List<EmailEndpoint> getRecievingEndpoints() {
		return (List<EmailEndpoint>) super.getRecievingEndpoints();
	}

}
