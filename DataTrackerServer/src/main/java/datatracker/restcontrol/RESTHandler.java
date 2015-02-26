package datatracker.restcontrol;

import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import datatracker.entities.User;
import datatracker.requesthandling.RegistrationHandler;


/*@Configuration
@ComponentScan
@EnableAutoConfiguration
@SpringBootApplication*/
@Component
@RestController
public class RESTHandler {
	
	@RequestMapping("/new_user")
	public String registerUser(@RequestParam(value="phoneNumber") String phoneNumber,
			@RequestParam(value = "password") String password, @RequestParam(value = "email") String email) {
		return RegistrationHandler.INSTANCE.registerUser(phoneNumber, password, email).toString();
	}
	
	/*@RequestMapping("/login")
	public String userLogin(@RequestParam(value="phoneNumber") String phoneNumber,
			@RequestParam(value = "password") String password) {
		return RegistrationHandler.INSTANCE.registerUser(phoneNumber, password).toString();
	}*/

	/*@RequestMapping("/log_data")
	public String logData(@RequestParam(value="phoneNumber") String phoneNumber) {
		return RegistrationHandler.INSTANCE.registerUser(phoneNumber).toString();
	}*/
}