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


## JDK 25 / Spring Boot 4 build

This checkout uses JDK 25, Gradle 9.1.0 and Spring Boot 4.0.8. Select a Java 25 installation and use the wrapper:

```sh
docker compose -f ../osk-flows/docker/osk-flows-test/docker-compose.yml up -d --wait
./gradlew clean build
```

The complete build includes tests, a plain library JAR, sources and Javadoc. Tests use PostgreSQL on port 25433 and purge notification tables; run sequentially with the sibling application's tests. The adjacent `osk-flows` checkout automatically uses this source checkout through a composite build.

This is a Jakarta/Spring Framework 7 library; existing Spring Boot 2 consumers must upgrade with it. Jackson 2 is retained through Boot 4's supported compatibility module to preserve persisted notification payloads and API JSON. `nc.jwt.signature-secret` retains its Base64 format and, when JWT is enabled, must decode to at least 64 bytes for HS512. Existing sufficiently strong secrets and tokens remain compatible.

See [the application migration notes](../osk-flows/docs/jdk25-boot4-upgrade.md) for dependency versions, runtime commands, compatibility decisions and verification results.
