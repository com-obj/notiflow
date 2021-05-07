# Build and run
To build the fat jar run (you need to run this command from the root of the repository)

`gradlew build -x test`

This will place fat jar into ./dist directory along with external resources like application.yaml, thymeleaf templates and message resources.

### Dependent components
To run the application you will need running postgres database. You can run pre-configured using the command

     cd docker
     docker-compose up -d
 

### Running application
To start application run
   
    ./osk-flows-1.0.0.jar --spring.config.location=file:./resources/application.properties --spring.profiles.active=test_mode
    
### Application configuration
The application configuration file used when running distribution can be found in 
     
    ./dist/resources/application.properties