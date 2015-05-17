package datatrackerserver.entitymanagement;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;

import datatrackerserver.email.EmailManager;
import datatrackerserver.entities.Account;
import datatrackerserver.entities.Device;
import datatrackerserver.repositories.AccountRepository;
import datatrackerserver.repositories.DeviceRepository;
import datatrackerserver.security.SecurityManager;
import datatrackerstandards.AccountRegistrationStatus;
import datatrackerstandards.AccountValidationStatus;
import datatrackerstandards.settings.AccountSetting;

@Configuration
@EnableAutoConfiguration
public class AccountHandler {
	public static final long DEFAULT_QUOTA = (long) Math.pow(2, 30); //1GB
	public static final long DEFAULT_THRESHOLD = (long) (DEFAULT_QUOTA * .90); //90% of quota
	
	@Autowired
	private ApplicationContext appContext;
	
	public void setApplicationContext(ApplicationContext appContext) {
		this.appContext = appContext;
	}
	

	protected AccountHandler() {}

	//TODO !!!! add all registration/validation mappings and handler functions
	
	public AccountRegistrationStatus registerAccount(String phoneNumber, String password, String email) {
		AccountRepository accountRepo = appContext.getBean(AccountRepository.class);
		Account newAccount = accountRepo.findOne(phoneNumber);
		Account accountByEmail = accountRepo.findByEmail(email);
		if(newAccount != null) {
			System.out.println("Account already exists");
			return AccountRegistrationStatus.ACCOUNT_ALREADY_EXISTS;
		}
		//turned off for demo for ease of account creation
		/*else if(accountByEmail != null) {
			System.out.println("Email already exists");
			return AccountRegistrationStatus.EMAIL_ALREADY_EXISTS;
		}*/
		else {
			newAccount = new Account(phoneNumber, password, email);
			newAccount.setValidationCode(SecurityManager.generateRandomCode());
			appContext.getBean(EmailManager.class).sendEmailConfirmationRequest(
					email, phoneNumber, newAccount.getValidationCode());
			accountRepo.save(newAccount);
			
			//create the corresponding device for this account
			appContext.getBean(DeviceHandler.class).registerDevice(phoneNumber, newAccount);
		}
		
		//debug
		Iterable<Account> accounts = accountRepo.findAll();
		for(Account account : accounts) {
			System.out.println(account.toString());
		}
		//end debug

		return AccountRegistrationStatus.SUCCESS; //success
	}

	public Account validateAccountAndPassword(String phoneNumber, String password) {
		AccountRepository accountRepo = appContext.getBean(AccountRepository.class);
		Account account = accountRepo.findOne(phoneNumber);
		
		if(account == null) {
			return null;
		}

		if(!password.equals(account.getPassword())) {
			return null;
		}

		return account;
	}

	public Account getAccountSettings(String phoneNumber) {
		AccountRepository accountRepo = appContext.getBean(AccountRepository.class);
		return accountRepo.findOne(phoneNumber);
	}

	public boolean setAccountSetting(String phoneNumber, String password,
			AccountSetting setting, String value) {
		boolean success = false;
		AccountRepository accountRepo = appContext.getBean(AccountRepository.class);
		Account account = validateAccountAndPassword(phoneNumber, password);
		
		if(account == null) {
			return false;
		}

		switch(setting) {
			case QUOTA:
				int quota = 0;

				try {
					quota = Integer.valueOf(value);
				}
				catch(NumberFormatException nfe) {
					nfe.printStackTrace();
					System.out.println("Invalid quota string: " + value);
					break;
				}
				
				account.setQuota(quota);
				success = true;
				break;
			case THRESHOLD:
				int threshold = 0;
				try {
					threshold = Integer.valueOf(value);
				}
				catch(NumberFormatException nfe) {
					nfe.printStackTrace();
					System.out.println("Invalid threshold string: " + value);
					break;
				}
				
				account.setThreshold(threshold);
				success = true;
				break;
		case BILLING_CYCLE:
			int cycleLength = 0;
			try {
				cycleLength = Integer.valueOf(value);
			}
			catch(NumberFormatException nfe) {
				nfe.printStackTrace();
				System.out.println("Invalid billing cycle string: " + value);
				break;
			}
			
			account.setBillingCycleStart(cycleLength);
			success = true;
			break;
		default:
			break;
		}
		
		if(success) {
			accountRepo.save(account);
		}

		return success;
	}
	
	public boolean validateEmail(String phoneNumber, String code) {
		AccountRepository accountRepo = appContext.getBean(AccountRepository.class);
		Account newAccount = accountRepo.findOne(phoneNumber);
		
		if(newAccount.getValidationCode().equals(code)) {
			newAccount.setEmailValidated(true);
			accountRepo.save(newAccount);
			return true;
		}

		return false;
	}

	public AccountValidationStatus validAccount(String phoneNumber, String password) {
		AccountRepository accountRepo = appContext.getBean(AccountRepository.class);
		Account account = accountRepo.findOne(phoneNumber);
		
		if(account == null) {
			return AccountValidationStatus.ACCOUNT_NOT_FOUND;
		}
		else if(!account.getPassword().equals(password)) {
			return AccountValidationStatus.INCORRECT_PHONE_NUMBER_OR_PASSWORD;
		}
		else {
			return AccountValidationStatus.VALIDATED;
		}
	}


	public boolean removeDevice(String phoneNumber, String accountPhoneNumber) {
		AccountRepository accountRepo = appContext.getBean(AccountRepository.class);
		Account account = accountRepo.findOne(accountPhoneNumber);
		DeviceRepository deviceRepo = appContext.getBean(DeviceRepository.class);
		Device device = deviceRepo.findOne(phoneNumber);
		
		boolean deviceRemoved = false;

		if(device.getAccount() != null && device.getAccount().equals(account)) {
			device.setAccount(null);
			deviceRepo.save(device);
			deviceRemoved = true;
		}
		
		return deviceRemoved;
	}
}
