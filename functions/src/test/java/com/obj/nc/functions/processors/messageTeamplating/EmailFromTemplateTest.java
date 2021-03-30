package com.obj.nc.functions.processors.messageTeamplating;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Locale;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.obj.nc.BaseIntegrationTest;
import com.obj.nc.SystemPropertyActiveProfileResolver;
import com.obj.nc.domain.content.EmailContent;
import com.obj.nc.domain.message.Message;
import com.obj.nc.functions.processors.messageTemplating.EmailTemplateFormatter;
import com.obj.nc.utils.JsonUtils;

@ActiveProfiles(value = "test", resolver = SystemPropertyActiveProfileResolver.class)
@SpringBootTest
class EmailFromTemplateTest extends BaseIntegrationTest {
	
	@Autowired private EmailTemplateFormatter teamplte2Html;

	@Test
	void createSimpleHtmlEmailFromTemplate() {
		//GIVEN
		String INPUT_JSON_FILE = "messages/templated/teamplate_message.json";
		Message msg = JsonUtils.readObjectFromClassPathResource(INPUT_JSON_FILE, Message.class);
		
		//WHEN
		List<Message> htmlMessages = teamplte2Html.apply(msg);
		
		//THEN
		assertThat(htmlMessages.size()).isEqualTo(1);
		
		EmailContent content = htmlMessages.iterator().next().getContentTyped();
		
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
		
		EmailContent content = htmlMessages.iterator().next().getContentTyped();
		
		System.out.println(content.getText());
		assertThat(content.getSubject()).isEqualTo("Subject");
		assertThat(content.getText()).contains("val11", "val12", "val21", "val22", "John Doe", "<html>");
		assertThat(content.getText()).doesNotContain("No parts available");
	}
	
	@Test
	void createI18NHtmlEmailFromTemplate() {
		//GIVEN
		String INPUT_JSON_FILE = "messages/templated/teamplate_message_en_de.json";
		Message msg = JsonUtils.readObjectFromClassPathResource(INPUT_JSON_FILE, Message.class);
		
		//WHEN
		List<Message> htmlMessages = teamplte2Html.apply(msg);
		
		//THEN
		assertThat(htmlMessages.size()).isEqualTo(2);
		
		EmailContent deContent = htmlMessages
				.stream()
				.filter(m-> Locale.GERMAN.equals(m.getAttributes().get(EmailTemplateFormatter.LOCALE_ATTR_NAME)))
				.findFirst()
				.get().getContentTyped();
		
		System.out.println(deContent.getText());
		assertThat(deContent.getSubject()).isEqualTo("Subject");
		assertThat(deContent.getText()).contains("Grues gott");
		assertThat(deContent.getText()).contains("John Doe");
		
		EmailContent enContent = htmlMessages
				.stream()
				.filter(m-> Locale.US.equals(m.getAttributes().get(EmailTemplateFormatter.LOCALE_ATTR_NAME)))
				.findFirst()
				.get().getContentTyped();
		
		System.out.println(enContent.getText());
		assertThat(enContent.getSubject()).isEqualTo("Subject");
		assertThat(enContent.getText()).contains("Hallo World");
		assertThat(deContent.getText()).contains("John Doe");
	}

}
