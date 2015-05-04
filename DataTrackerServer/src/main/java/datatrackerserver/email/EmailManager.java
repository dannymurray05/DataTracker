package datatrackerserver.email;

import java.util.List;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import datatrackerstandards.DataTrackerConstants;
import datatrackerstandards.RequestType;

@Component
public class EmailManager {
	private final JavaMailSender mailSender;

	@Autowired
	public EmailManager(JavaMailSender mailSender) {
		this.mailSender = mailSender;
	}
	
	public boolean sendEmailConfirmationRequest(final String recipient, final String accountPhoneNumber,
			final String confirmationCode) {
		String confirmationURL = createURL(RequestType.VALIDATE_EMAIL, accountPhoneNumber, confirmationCode);
		String subject = "DataTracker - confirm email request";
		String message = "To confirm your email please click the link below:\n\n"
				+ confirmationURL
				+ "\n\nThank you - DataTracker";
		return sendEmail(recipient, subject, message);
	}

	public boolean sendDeviceConfirmationRequest(final String recipient, final String accountPhoneNumber,
			final String devicePhoneNumber, final String confirmationCode) {
		String confirmationURL = createURL(RequestType.VALIDATE_DEVICE,
				devicePhoneNumber, accountPhoneNumber, confirmationCode);
		String subject = "DataTracker - confirm device request (" + devicePhoneNumber + ")";
		String message = "The device with phone number " + devicePhoneNumber + " has requested "
				+ "to be part of your DataTracker account. To confirm please click the link below:\n\n"
				+ confirmationURL
				+ "\n\nThank you - DataTracker";
		return sendEmail(recipient, subject, message);
	}

	private String createURL(RequestType requestType, String... params) {
		StringBuilder confirmationURL = new StringBuilder(DataTrackerConstants.SERVER_ADDRESS);
		confirmationURL.append(requestType.getMapping()).append("?");
		List<String> paramKeys = requestType.getParamKeys();
		for(int i = 0; i < paramKeys.size(); i++) {
			confirmationURL.append(paramKeys.get(i));
			confirmationURL.append("=");
			confirmationURL.append(params[i]);
			if(i != paramKeys.size() - 1) {
				confirmationURL.append("&");
			}
		}
		return confirmationURL.toString();
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
