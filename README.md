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
