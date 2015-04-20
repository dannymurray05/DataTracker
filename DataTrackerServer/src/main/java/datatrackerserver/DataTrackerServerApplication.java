package datatrackerserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import datatrackerserver.entitymanagement.DataHandler;
import datatrackerserver.entitymanagement.DeviceHandler;
import datatrackerserver.entitymanagement.UserHandler;


@Configuration
@ComponentScan
@EnableAutoConfiguration
@SpringBootApplication
public class DataTrackerServerApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(DataTrackerServerApplication.class, args);
        UserHandler.INSTANCE.setApplicationContext(context);
        DeviceHandler.INSTANCE.setApplicationContext(context);
        DataHandler.INSTANCE.setApplicationContext(context);
    }
}
