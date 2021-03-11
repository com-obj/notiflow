package com.obj.nc.utils;

import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetupTest;
import lombok.Data;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;

@Component
@Profile({"dev","test"})
@Log4j2
@Data
public class GreenMailManager {

	private static GreenMail GREEN_MAIL;

	static {
		GREEN_MAIL = new GreenMail(ServerSetupTest.SMTP_IMAP);
		GREEN_MAIL.setUser("no-reply@objectify.sk", "xxx");
		GREEN_MAIL.setUser("testmode_no-reply@objectify.sk", "xxx");
		GREEN_MAIL.start();
	}

	@PreDestroy
	public void stop() {
		GREEN_MAIL.stop();
	}

	public GreenMail getGreenMail() {
		return GREEN_MAIL;
	}
	
}
