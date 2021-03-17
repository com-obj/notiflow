package com.obj.nc.functions.processors.messageTeamplating;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;

import com.obj.nc.BaseIntegrationTest;
import com.obj.nc.SystemPropertyActiveProfileResolver;
import com.obj.nc.domain.message.Email;
import com.obj.nc.domain.message.Message;
import com.obj.nc.functions.processors.messageTemplating.EmailTemplateFormatter;
import com.obj.nc.utils.JsonUtils;

@ActiveProfiles(value = "test", resolver = SystemPropertyActiveProfileResolver.class)
class EmailFromTemplateTest extends BaseIntegrationTest {
	
	@Autowired
	private EmailTemplateFormatter teamplte2Html;

	@Test
	void createSimpleHtmlEmailFromTemplate() {
		//GIVEN
		String INPUT_JSON_FILE = "messages/templated/teamplate_message.json";
		Message msg = JsonUtils.readObjectFromClassPathResource(INPUT_JSON_FILE, Message.class);
		
		//WHEN
		List<Message> htmlMessages = teamplte2Html.apply(msg);
		
		//THEN
		assertThat(htmlMessages.size()).isEqualTo(1);
		
		Email content = htmlMessages.iterator().next().getContentTyped();
		
		System.out.println(content.getText());
		assertThat(content.getSubject()).isEqualTo("Subject");
		assertThat(content.getText()).contains("part1", "part2", "John Doe", "<html>");
	}
	
	@Test
	void createHtmlEmailFromPojoModelAndTemplate() {
		//GIVEN
		String INPUT_JSON_FILE = "messages/templated/teamplate_message_pojo_model.json";
		Message msg = JsonUtils.readObjectFromClassPathResource(INPUT_JSON_FILE, Message.class);
		
		//WHEN
		List<Message> htmlMessages = teamplte2Html.apply(msg);
		
		//THEN
		assertThat(htmlMessages.size()).isEqualTo(1);
		
		Email content = htmlMessages.iterator().next().getContentTyped();
		
		System.out.println(content.getText());
		assertThat(content.getSubject()).isEqualTo("Subject");
		assertThat(content.getText()).contains("val11", "val12", "val21", "val22", "John Doe", "<html>");
		assertThat(content.getText()).doesNotContain("No parts available");
		
		
	}

}
