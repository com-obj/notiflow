# Examples
You can find examples on how to use notiflow for specific use-cases. All of these example can be found in the git repository [here](https://github.com/com-obj/example-flows). If you are missing some example, let us know. We will add it in the near future. 

## Before you begin

1. Check our [gradle dependencies](https://github.com/com-obj/example-flows/blob/master/build.gradle) to see external libraries which are used across these examples. 

1. Add component scanning to you spring boot application so that the notiflow components are picked up by spring

    ``` java hl_lines="1 4 5"
    import com.obj.nc.Get;

    @SpringBootApplication
    @ComponentScan(basePackageClasses = {Get.class, SendEmailApplication.class})
    @IntegrationComponentScan(basePackageClasses = Get.class)
    public class YouSpringBootApplication {
    ```


## Send simple Email

This example illustrates the use-case when you simply want to send email with constant body text to single recipient. 

``` java

	@Autowired private EmailProcessingFlow emailFlow;

```

To start interacting with [email processing flow](flows.md#emailProcessingFlow) inject reference to EmailProcessingFlow interface. Then it is straight forward to send an email.

``` java

	public void sendEmail(String recipient, String subject, String msgText) {
		EmailContent body = EmailContent.builder()
				.subject(subject)
				.text(msgText)
				.build();		
		
		EmailEndpoint emailEndpoint = EmailEndpoint.builder().email(recipient).build();
		
		EmailMessage msg = new EmailMessage();
		msg.addReceivingEndpoints(emailEndpoint);
		msg.setBody(body);
		
		emailFlow.sendEmail(msg);		
	}

```

Notiflow is using [Spring Email Sender](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.email) for interaction with SMTP server. You need to configure it using you application.properties file

``` 

spring.mail.host=localhost
spring.mail.port=3025
spring.mail.username=john_doe
spring.mail.password=pwd

```

## Send Push notification
This example illustrates how to send push notification to one specific device using [push processing flow](flows.md#pushProcessingFlow). 
Notiflow uses [Firebase Cloud Messaging](https://firebase.google.com/docs/cloud-messaging/) for sending 
[Android](https://firebase.google.com/docs/cloud-messaging/android/client), 
[iOS](https://firebase.google.com/docs/cloud-messaging/ios/client), 
[Web](https://firebase.google.com/docs/cloud-messaging/js/client) and other push notifications. 
To set up client applications, please refer to included links. 
To be able to communicate with FCM, you need to add path to your Firebase service account file to your _application.properties_ file.

```properties

nc.firebase.service-account-file-path=$HOME/.firebase/service-account-key.json

```

Inject _PushProcessingFlow_ bean into your sending service.


```java

    @Service
    @RequiredArgsConstructor
    public class SendPushService {
        
        // inject push processing flow bean
        private final PushProcessingFlow pushProcessingFlow;
        
        public PushMessage createMessage(PushEndpoint endpoint, String subject, String msgText) {
            
            PushMessage message = new PushMessage();
            message.setBody(
                    PushContent
                            .builder()
                            .subject(subject)
                            .text(msgText)
                            .build()
            );
            message.setReceivingEndpoints(
                    Collections.singletonList(endpoint)
            );
            return message;
        }
        
        public void send(PushMessage message) {
            pushProcessingFlow.sendPushMessage(message);
        }
    
    }

```

Then you can create message and send it to desired endpoint.

```java

    public void sendDirectPush() {
        PushEndpoint endpoint = PushEndpoint.ofToken("someAndroidDeviceFCMToken");
        
        PushMessage message = pushService
            .createMessage(endpoint, "Subject", "Hello World!");
        
        pushService.send(message);
    }

```

## Send slack message

This example illustrates the use-case when you simply want to send slack message with constant body text to single channel.

To start sending messages to public slack channel, first create a bot in slack. For simplicity sake, use example [here](https://api.slack.com/tutorials/tracks/posting-messages-with-curl). Then set application properties as follows:
``` 
nc.slack.apiUrl=https://slack.com/api
nc.slack.botToken=xoxb-2660284751633-2647758251043-lettersAndNumbers
```
Bot of token always starts with '*xoxb-*'. Be sure. that your token starts with this prefix.

Next you need channel code. Usually it is part of url (when you are in channel, it is part of url behind last /). 
Replace the *public-slack-channel-code* in following code sample with your channel code.
``` java
    @Autowired
    private SlackMessageProcessingFlow processingFlow;

    void sendSlackMessage() {
        SlackMessage message = new SlackMessage();
        message.setBody(SlackMessageContent.builder().text("Hello World!").build());
        message.setReceivingEndpoints(Collections.singletonList(SlackEndpoint.builder().channel("public-slack-channel-code").build()));
        processingFlow.sendMessage(message);
    }
```



## Convert custom application event to Message

Covering custom event is very common use-case for notiflow. This separation of responsibility ensures that client application does its job and only emits application events if something important happens. The processing of such events, with regards to notification of users or 3rd parties, is in the responsibility of notiflow. 

In this example we will send an welcome email to  newly registered customer. Let us first define custom event 

``` java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonTypeInfo(use = Id.CLASS)
public class NewCustomerRegistrationEvent implements IsTypedJson {

	private String customerName;
	private String customerEmail;
}
```
Tha class can be any POJO class which can be serialized to JSON using [Jackson library](https://github.com/FasterXML/jackson). 

!!! note "IsTypedJson"
    Event can be any POJO class. This creates some challenges with JSON serialization and de-serialization. In serialization process, we need to store the FCCN of the POJO into @class attribute of the JSON. This is done using the `#!java @JsonTypeInfo(use = Id.CLASS)` annotation.
    
    To deserialize the JSON we need to define common base class `#!java IsTypedJson` and register theses using Jackson mixins like `#!java JsonUtils.getObjectMapper().addMixIn(IsTypedJson.class, NewCustomerRegistrationEvent.class);`

    This process will be simplified in the future versions and the IsTypedJson interface will be removed

Next step is to create custom converter which understand the Event class and is able to create notification from it. To achieve this goal we need to provide implementation of `#!java InputEvent2MessageConverterExtension` interface and register it as `#!java @Component` 

``` java
@Component
public class EventToMessageConverter implements InputEvent2MessageConverterExtension {

...

}
```

The interface define two methods. The method `#!java canHandle` is used, if our application has more implementations of `#!java InputEvent2MessageConverterExtension` and we want to decide which one to use for this specific event. If the converted wants to handle the event, it returns `#!java Optional.empty()` like so

``` java
	@Override
	public Optional<PayloadValidationException> canHandle(GenericEvent payload) {
		if (payload.getPayloadAsPojo() instanceof NewCustomerRegistrationEvent) {
			return Optional.empty();
		}

		return Optional.of(new PayloadValidationException("EventToMessageConverter only handles payload of type NewCustomerRegistrationEvent "));
	}
```

Last thing is to implement the conversion itself. 

``` java
	@Override
	public List<Message<?>> convertEvent(GenericEvent event) {
		NewCustomerRegistrationEvent regEvent = event.getPayloadAsPojo();
		
		EmailContent body = EmailContent.builder()
				.subject("Welcome on board " +  regEvent.getCustomerName())
				.text("We love to have you in the comunity.")
				.build();		
		
		EmailEndpoint emailEndpoint = EmailEndpoint.builder().email(regEvent.getCustomerEmail()).build();
		
		EmailMessage msg = new EmailMessage();
		msg.addReceivingEndpoints(emailEndpoint);
		msg.setBody(body);
		
		return Arrays.asList(msg);
	}
```

Now we can test the flow using the notiflow [REST-API events endpoint]() which is accepting application events. The test for the specific example could look like this

``` java
	@Autowired protected MockMvc mockMvc;

	@Test
	void eventToMessage() throws Exception {
		NewCustomerRegistrationEvent event = NewCustomerRegistrationEvent.builder()
				.customerName("John Doe")
				.customerEmail("john_doe@company.com")
				.build();
        
        ResultActions resp = mockMvc
        		.perform(MockMvcRequestBuilders.post("/events")
        		.content(JsonUtils.writeObjectToJSONString(event))
        		.contentType(APPLICATION_JSON_UTF8)        		
                .accept(APPLICATION_JSON_UTF8));
		
        resp
    		.andExpect(status().is2xxSuccessful())
    		.andExpect(jsonPath("$.ncEventId").value(CoreMatchers.notNullValue()));
        
		boolean recieved = greenMail.waitForIncomingEmail(5000L, 1);
		
		assertEquals(true, recieved);
		assertEquals(1, greenMail.getReceivedMessages().length);
		
	    MimeMessage receivedMessage = greenMail.getReceivedMessages()[0];
	    MimeMessageParser parsedEmail = new MimeMessageParser(receivedMessage).parse();
	    
	    assertEquals("We love to have you in the comunity.",parsedEmail.getPlainContent());
	    assertEquals("Welcome on board John Doe",parsedEmail.getSubject());
	    assertEquals(1, receivedMessage.getAllRecipients().length);
	    assertEquals("john_doe@company.com", receivedMessage.getAllRecipients()[0].toString());		
	}

    @RegisterExtension
	static GreenMailExtension greenMail = new GreenMailExtension(ServerSetupTest.SMTP)
	  	.withConfiguration(GreenMailConfiguration.aConfig().withUser("john_doe", "pwd"))
	  	.withPerMethodLifecycle(false);
```

    


## Templated Email

## Using Test mode

## PostgreSQL data source configuration <span id="ds-config"/>



