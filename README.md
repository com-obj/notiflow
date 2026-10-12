# Notiflow
Notiflow is software framework for reliable delivery of email, push notifications and other types of messages. It is written in Java and it uses various Spring modules especially Spring integration. 

## Goals
The main goal of notiflow is to greatly simplify delivery of information messages to the end user of your application. This is achieved by providing solution for many generic problems of message delivery while taking into account the was diversity of use-cases in which application operate and send messages. The problems which notiflow is solving are

  * allow application to emit events and don't worry about who and what should be communicated to the end-users on those events
  * hide complexity of sending messages via different channels like Email, Push, Sms, Mailchimp, ..
  * allow for scalability from tiny to thousand messages per second
  * ensure reliable delivery with delivery error handling 
  * provide comprehensive monitoring and debugging of messages processing
  * provide UI for message delivery statistics, metrics and processing details
  * allow for testing message routing, formatting and delivery
  * end-user spam prevention
  * message aggregation

To understand in details how notiflow is solving this problems, check our [documentation](https://com-obj.github.io/notiflow/)

## Upgrading to 2.0
Notiflow 2.0 is built with JDK 25 and Spring Boot 4 (Spring Framework 7, Jakarta EE). Consuming applications must upgrade to Spring Boot 4 and Java 25 as well.

* Jackson 2 is still used for REST and persisted notification payloads, through Spring Boot's Jackson 2 compatibility module. Notiflow registers the required defaults (`spring.http.converters.preferred-json-mapper=jackson2`, `spring.main.allow-circular-references=true`); applications do not need to set them.
* When `nc.jwt.enabled=true`, `nc.jwt.signature-secret` must be Base64 that decodes to at least 64 bytes (HS512). Otherwise the application fails at startup. Tokens issued with an existing secret of that strength remain valid.
* The API documentation moved from Springfox (`/v2/api-docs`) to springdoc OpenAPI 3: `/v3/api-docs` and `/swagger-ui.html`.
