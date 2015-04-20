package datatrackerserver.entitymanagement;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;

import datatrackerserver.email.EmailManager;
import datatrackerserver.entities.Device;
import datatrackerserver.entities.User;
import datatrackerserver.repositories.DeviceRepository;
import datatrackerserver.repositories.UserRepository;
import datatrackerserver.security.SecurityManager;

@Configuration
@EnableAutoConfiguration
public class DeviceHandler {
	public static final DeviceHandler INSTANCE = new DeviceHandler();

	@Autowired
	private ApplicationContext appContext;


	public void setApplicationContext(ApplicationContext appContext) {
		this.appContext = appContext;
	}

	protected DeviceHandler() {}

	public static enum DeviceSettings {
		QUOTA,
		THRESHOLD,
		AUTO_SHUTOFF,
		;
	}

	/**
	 * Only to be used for registering the device of a new user.
	 * @param phoneNumber
	 * @param user
	 * @return true if the device was created, false otherwise.
	 */
	public boolean registerDevice(String phoneNumber, User user) {
		DeviceRepository deviceRepo = appContext.getBean(DeviceRepository.class);
		Device device = deviceRepo.findOne(phoneNumber);

		if(device != null) {
			System.out.println("Device already exists! Modifying controlling user!");
			device.setUser(user);
			deviceRepo.save(device);
			return false;
		}
		else {
			device = new Device(phoneNumber, user);
			deviceRepo.save(device);
			return true;
		}
	}
	
	public boolean registerDevice(String phoneNumber, String userPhoneNumber) {
		DeviceRepository deviceRepo = appContext.getBean(DeviceRepository.class);
		Device device = deviceRepo.findOne(phoneNumber);
        UserRepository userRepo = appContext.getBean(UserRepository.class);
        User user = userRepo.findOne(userPhoneNumber);

        boolean deviceCreated = false;
		if(device != null) {
			Logger.getAnonymousLogger().log(Level.INFO, "Device already exists! Requesting validation from user to modify.");
		}
		else {
			device = new Device(phoneNumber);
			deviceCreated = true;
		}
		
		device.setValidationCode(SecurityManager.generateRandomCode());
		EmailManager.INSTANCE.sendDeviceConfirmationRequest(user.getEmail(), userPhoneNumber, phoneNumber, device.getValidationCode());
		deviceRepo.save(device);
		
		return deviceCreated;
	}

	public boolean validateDevice(String phoneNumber, String userPhoneNumber, String code) {
		DeviceRepository deviceRepo = appContext.getBean(DeviceRepository.class);
		Device device = deviceRepo.findOne(phoneNumber);
		UserRepository userRepo = appContext.getBean(UserRepository.class);
		User user = userRepo.findOne(userPhoneNumber);

		boolean deviceValidated = false;

		if(device.getValidationCode().equals(code)) {
			//validated
			device.setUser(user);
			deviceRepo.save(device);
			deviceValidated = true;
		}
		
		return deviceValidated;
	}

	public Device getDeviceSettings(String phoneNumber) {
		DeviceRepository deviceRepo = appContext.getBean(DeviceRepository.class);
		Device device = deviceRepo.findOne(phoneNumber);

		return device;
	}

	public boolean setDeviceSetting(String phoneNumber, DeviceSettings setting, String value) {
		boolean success = false;
		DeviceRepository deviceRepo = appContext.getBean(DeviceRepository.class);
		Device device = deviceRepo.findOne(phoneNumber);
		
		if(device == null) {
			return false;
		}

		switch(setting) {
			case AUTO_SHUTOFF:
				if(value.equalsIgnoreCase("true")) {
					device.setAutoShutoff(true);
					success = true;
				}
				else if(value.equalsIgnoreCase("false")) {
					device.setAutoShutoff(false);
					success = true;
				}
				break;
			case QUOTA:
				long quota = 0;
				try {
					quota = Long.valueOf(value);
				}
				catch(NumberFormatException nfe) {
					nfe.printStackTrace();
					System.out.println("Invalid quota string: " + value);
					break;
				}
				
				device.setQuota(quota);
				success = true;
				break;
			case THRESHOLD:
				long threshold = 0;
				try {
					threshold = Long.valueOf(value);
				}
				catch(NumberFormatException nfe) {
					nfe.printStackTrace();
					System.out.println("Invalid threshold string: " + value);
					break;
				}
				
				device.setThreshold(threshold);
				success = true;
				break;
			default:
				break;
		}
		
		if(success) {
			deviceRepo.save(device);
		}

		return success;
	}
}
