package com.obj.nc.domain.content.mailchimp;

import com.obj.nc.domain.IsTypedJson;
import com.obj.nc.utils.JsonUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.annotation.PostConstruct;

@Configuration
public class MailchimpContentSerializerConfig {
    @PostConstruct
    public void registerJsonMixins() {
    }
}
