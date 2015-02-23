package datatracker.requesthandling;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;

import datatracker.entities.User;
import datatracker.repositories.UserRepository;

@Configuration
@EnableAutoConfiguration
public class RegistrationHandler {
	
	public static final RegistrationHandler INSTANCE = new RegistrationHandler();
	
	@Autowired
	private ApplicationContext appContext;

	protected RegistrationHandler() {}
	
	public User registerUser(String phoneNumber, String password, String email) {
		UserRepository userRepo = appContext.getBean(UserRepository.class);
		User newUser = userRepo.findOne(phoneNumber);
		if(newUser != null) {
			System.out.println("User already exists");
		}
		else {
			newUser = new User(phoneNumber, password, email, 0, 0);
			userRepo.save(newUser);
		}
		
		Iterable<User> users = userRepo.findAll();
		for(User user : users) {
			System.out.println(user.toString());
		}
		return newUser;
	}

	public void setApplicationContext(ApplicationContext appContext) {
		this.appContext = appContext;
	}
}
