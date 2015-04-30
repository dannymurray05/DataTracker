package datatrackerserver.email;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

@Component
public class EmailManager {
	private final JavaMailSender mailSender;

	@Autowired
	public EmailManager(JavaMailSender mailSender) {
		this.mailSender = mailSender;
	}
	
	public boolean sendEmailConfirmationRequest(final String recipient, final String accountPhoneNumber,
			final String confirmationCode) {
		String confirmationURL = "http://192.168.1.2:8080/validate_email?phoneNumber=" + accountPhoneNumber
				+ "&code=" + confirmationCode;
		String subject = "DataTracker - confirm email request";
		String message = "To confirm your email please click the link below:\n\n"
				+ confirmationURL
				+ "\n\nThank you - DataTracker";
		return sendEmail(recipient, subject, message);
	}

	public boolean sendDeviceConfirmationRequest(final String recipient, final String accountPhoneNumber,
			final String devicePhoneNumber, final String confirmationCode) {
		String confirmationURL = "http://192.168.1.2:8080/validate_device?phoneNumber=" + accountPhoneNumber
				+ "&devicePhoneNumber=" + devicePhoneNumber
				+ "&code=" + confirmationCode;
		String subject = "DataTracker - confirm device request (" + devicePhoneNumber + ")";
		String message = "The device with phone number " + devicePhoneNumber + " has requested "
				+ "to be part of your DataTracker account. To confirm please click the link below:\n\n"
				+ confirmationURL
				+ "\n\nThank you - DataTracker";
		return sendEmail(recipient, subject, message);
	}

	public boolean sendEmail(final String recipient, final String subjectStr, final String messageStr) {
		MimeMessage message = mailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(message);
		try {
			helper.setTo(recipient);
			helper.setSubject(subjectStr);
			helper.setText(messageStr);
		} catch (MessagingException e) {
			e.printStackTrace();
			return false;
		}
		
		try {
			mailSender.send(message);
		} catch (MailException e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}
}
