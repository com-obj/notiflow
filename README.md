# Links
* https://cloud.spring.io/spring-cloud-static/Edgware.SR6/multi/multi__programming_model.html


## Funcitonal model
* https://spring.io/blog/2019/10/14/spring-cloud-stream-demystified-and-simplified
* https://spring.io/blog/2019/10/17/spring-cloud-stream-functional-and-reactive
* https://github.com/spring-cloud/spring-cloud-dataflow-samples/commit/2b3d2b63204c4a20ac8d813ea33c5fa87241c528
 
 
## Cloud stream
* https://github.com/spring-cloud/spring-cloud-stream/blob/master/docs/src/main/asciidoc/spring-cloud-stream.adoc#functional-binding-names


# Setup dev environment
* clone repository

## Intellij-idea
* File -> Open
* Select root directory of this project
* In import dialog window, select "Gradle project"

# API Authentication

## Properties
| Property                | Values                  | Default |
|-------------------------|-------------------------|---------|
| nc.jwt.enabled          | Boolean                 | false   |
| nc.jwt.username         | String                  | null    |
| nc.jwt.password         | String                  | null    |
| nc.jwt.signature-secret | String                  | null    |

## Example Request (HTTP POST)
```
curl -H "Content-Type: application/json" --data { "username" : "nc", "password" : "3jfAEmJKYqVCLE" } http://localhost:8080/authenticate
```

## Example Response
```
{"token":"eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJuYyIsImV4cCI6MTYyMTIwODUxNCwiaWF0IjoxNjIxMTkwNTE0fQ.Se6yitIkVvbyVkWZIZk8Vxpr_Gp0L9uONW9ErideekzKlQUfogSfdqVz7LPGAZcZR2JK0OiYsjRSQq5-ziIozw"}
```
