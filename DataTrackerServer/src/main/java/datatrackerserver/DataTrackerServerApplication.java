package datatrackerserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import datatrackerserver.entitymanagement.DataHandler;
import datatrackerserver.entitymanagement.DeviceHandler;
import datatrackerserver.entitymanagement.AccountHandler;


@Configuration
@ComponentScan
@EnableAutoConfiguration
@SpringBootApplication
public class DataTrackerServerApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(DataTrackerServerApplication.class, args);
        //context.getBean(AccountHandler.class).setApplicationContext(context);
        //context.getBean(DeviceHandler.class).setApplicationContext(context);
        //context.getBean(DataHandler.class).setApplicationContext(context);
        //context.getBean(RESTHandler.class).setApplicationContext(context);
    }
}
