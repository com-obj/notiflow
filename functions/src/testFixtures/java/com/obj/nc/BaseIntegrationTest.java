package com.obj.nc;

import java.nio.charset.Charset;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.obj.nc.utils.JsonUtils;

/*
    SINGLETON pattern class with containers for all test classes
    see more: https://www.testcontainers.org/test_framework_integration/manual_lifecycle_control/#singleton-containers
 */
@SpringBootTest(properties = "spring.main.allow-bean-definition-overriding=true")
public abstract class BaseIntegrationTest {

    static FixedPortPostgreSQLContainer<?> POSTGRESQL_CONTAINER;
    
    public static final MediaType APPLICATION_JSON_UTF8 = new MediaType(MediaType.APPLICATION_JSON.getType(), MediaType.APPLICATION_JSON.getSubtype(), Charset.forName("utf8"));

//	protected <T> ResponseEntity<T> postPojoInBody(String url, Object pojo, Class<T> resultClass) {
//		String requestJson = JsonUtils.writeObjectToJSONString(pojo);
//		
//		return postJsonStringInBody(url, requestJson, resultClass);
//	}
//	
//	protected <T> ResponseEntity<T> postJsonStringInBody(String url, String requestJson, Class<T> resultClass) {
//		RestTemplate restTemplate = new RestTemplate();
//    	
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.APPLICATION_JSON);
//
//		HttpEntity<String> entity = new HttpEntity<String>(requestJson, headers);
//        ResponseEntity<T> response = restTemplate.postForEntity(url, entity, resultClass);
//        return response;
//	}
}
