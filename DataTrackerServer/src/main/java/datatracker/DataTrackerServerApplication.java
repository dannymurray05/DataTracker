package datatracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import datatracker.requesthandling.RegistrationHandler;


@Configuration
@ComponentScan
@EnableAutoConfiguration
@SpringBootApplication
public class DataTrackerServerApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(DataTrackerServerApplication.class, args);
        RegistrationHandler.INSTANCE.setApplicationContext(context);
    }
}
