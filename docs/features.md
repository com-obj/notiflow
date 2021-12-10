# Notiflow features

There are many applications that communicate with its users in an asynchronous fashion using various means like emails or mobile push messages. 
Sending a message seems to be a simple task at first and most application decide for straight forward solution and use Emails to communicate with users. In most cases they simply call SMTP server when condition for notification are met. This approach can quickly result in unexpected and non-trivial development costs as the requirements evolve. 

Example of such requirements are

  * Email has to be generated from template
  * Notification has to have some delivery guaranties and the engine has to be able to handle error cases
  * What was send? To whom? And when?
  * Notification has to be send using different channel, not only via Email. This can be Push notification on mobil phone, Slack message, SMS, ..
  * and there are many more. 

Notiflow solve a lot of these generic problems. The list of all its features is described in the next chapiters.

## Different messaging channels

Notiflow let you abstract from channel you want to use to inform your users or customers an takes care about implementation details of these channels.

  * Email
  * Android/iOS push message 
  * SMS 
  * Facebook post 
  * Tweet 
  * Slack
  * Microsoft Teams message
... 

To find out more about how to configure the various channels [check the configuration options of specific sender]()

## Message templates 

You can define beautiful dynamic message content using [Thymeleaf](https://www.thymeleaf.org/) templates across all of the message channels. 

To find out more about configuration of templates check our Cook book in the [templates]() section.

## Test mode

Test your configuration before going to production. Notiflow let you turn on the Test mode which ensures that all processing happens like in normal production environment except of the send step. Instead, messages get aggregated and can be inspected using [Web UI]() or will be send in single digest Email. 

To find out more about turning on the test mode check our Cook book in the [Test mode]() section.

## Message delivery in predefined hours (future versions)

You can configure how and when recipients should receive your message. For example 

  * you might want to send SMS in working hours and Email on weekends 
  * you can postpone the message delivery if outside of working hours. 

## Message aggregation (future versions)

Instead of sending message each time some application event occurs, configure notiflow to send single aggregated message which consists of all messages which would have been send in the given time period

## Massage delivery tracking  

Have a detailed view on status of your notification. For most channels we provided information if the messages is currently being

  * Processing in notiflow
  * Sent to recipient
  * Delivered
  * Read
  * Failed

You can further inspect when and how the message or event has been processed using the [Web UI]()

## Fault tolerance  

Notiflow provides several level of fault tolerance. 

You can deploy notiflow on you computer cluster and provide fault tolerance of HW infrastructure by duplicating components which are processing messages. The components ([functions]()) communicate using some message broker and the deployment provides not only assurance against HW outages but allows for high throughput if required. 

If message processing fails because of problems in data of network connectivity to 3rd party components, notiflow will put the message to the area for failed messages and allow user to resurrect the message processing from the failed step once the problem has been resolved

## Recipient spam prevention 

Do you have customers that you definitively don't want to send more than N messages in a given time period? Configure spam prevention mechanism in notiflow to either reject messages which would pass the limit or send them as single aggregate message at the end of that period. 

## Message prioritization (future versions)

Notiflow does its best to process messages as fast as possible but there might be a case when you want the message to be delivered before Notiflow processes all prior messages. For this purpose, you can prioritize your messages so that the more important are always processed before the others.

## Scheduled notification / PULL based notification

If you need to query some other resources to find out if something important happened, so that you can notify those who are interested, you can schedule when the notification flow should start. If you have a data source exposing data aboul contracts expiration, you can configure Notiflow to inform you X days before the expiration happens

## Web UI for monitoring and statistics (Paid component)

Find out more about our [Web UI]() and information it can provide like

  * What event has beed received by the Notiflow
  * What are delivery statistics
  * What messages has been received by specific recipient
  * What messages would have been send if not in [Test mode]()

