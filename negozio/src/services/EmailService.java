package services;

import java.io.File;
import java.io.IOException;
import java.util.Properties;
import jakarta.mail.*;
import jakarta.mail.internet.*;

public class EmailService {

	public static void inviaMessaggioAlert(String host, String port, String username, String password,
			String adminEmail, String prodotto, int giacenzaAttuale, int sogliaMinima) {

		// 1. Configurazione delle proprietà SMTP
		Properties props = new Properties();
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.host", host);
		props.put("mail.smtp.port", port);

		Session session = Session.getInstance(props, new Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(username, password);
			}
		});

		try {

			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress(username));

			message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(adminEmail));

			message.setSubject("URGENTE: AVVISO SCORTA MINIMA - " + prodotto);

			String emailContent = String.format(
					"Attenzione Amministratore,\n\n" + "Il prodotto '%s' ha raggiunto un livello critico.\n"
							+ "Dettagli:\n" + "  - Giacenza Attuale: %d\n" + "  - Soglia Minima: %d\n\n"
							+ "Si prega di procedere immediatamente con il riordino per evitare l'esaurimento.",
					prodotto, giacenzaAttuale, sogliaMinima);

			message.setText(emailContent);

			Transport.send(message);

		} catch (MessagingException e) {

			System.err.println("ERRORE CRITICO: Impossibile inviare l'email. Verifica le configurazioni SMTP.");
			e.printStackTrace();
		}
	}

	// --- Metodo per invio mail con allegato (Fattura PDF) ---
	public static void inviaFatturazione(String host, String port, String username, String password,
			String destinatario, String oggetto, String corpoTesto, String allegatoPath) throws MessagingException {

		// 1. Configurazione delle proprietà SMTP
		Properties props = new Properties();
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.host", host);
		props.put("mail.smtp.port", port);

		Session session = Session.getInstance(props, new Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(username, password);
			}
		});

		MimeMessage message = new MimeMessage(session);
		message.setFrom(new InternetAddress(username));
		message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destinatario));
		message.setSubject(oggetto);

		MimeMultipart multipart = new MimeMultipart();

		MimeBodyPart messageBodyPart = new MimeBodyPart();
		messageBodyPart.setText(corpoTesto);
		multipart.addBodyPart(messageBodyPart);

		File file = new File(allegatoPath);
		if (file.exists()) {
			MimeBodyPart attachmentPart = new MimeBodyPart();
			try {
				attachmentPart.attachFile(file);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (MessagingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			attachmentPart.setFileName(file.getName());
			multipart.addBodyPart(attachmentPart);
		} else {
			System.err.println("ATTENZIONE: File allegato non trovato al percorso: " + allegatoPath);

		}

		message.setContent(multipart);
		Transport.send(message);
	}
}
