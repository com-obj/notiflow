# Getting started

1. Create spring boot application using [Spring Initializr](https://start.spring.io/)
1. Add following dependency to your build tool

    === "Maven"

        ``` Maven
        <dependency>
            <groupId>objectify</groupId>
            <artifactId>notif-center</artifactId>
            <version>1.1.6</version>
        </dependency>
        ```

    === "Gradle"

        ``` Gradle
        implementation group: 'objectify', name: 'notif-center', version: '1.1.6'
        ```

1. Configure database connection which will be used by notiflow for persistance of processing relevant information 

    === "application.properties"

        ``` 
        spring.datasource.platform=postgres
        spring.datasource.url=jdbc:postgresql://localhost:5432/db_name
        spring.datasource.username=user_name
        spring.datasource.password=password
        ```

    !!! note "Database configuration"
        You might need to create a new database if you don't have an existing one.
        There are other options on how to [configure data source](examples.md#ds-config).    

1. That's it. Check [examples](examples.md) to do something useful with Notiflow