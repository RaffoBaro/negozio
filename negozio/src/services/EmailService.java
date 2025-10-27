package services;

import java.io.File;
import java.io.IOException;
import java.util.Properties;
import jakarta.mail.*;
import jakarta.mail.internet.*;

public class EmailService {

    public static void inviaMessaggioAlert(
        String host, String port, String username, String password,
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
                "Attenzione Amministratore,\n\n" +
                "Il prodotto '%s' ha raggiunto un livello critico.\n" +
                "Dettagli:\n" +
                "  - Giacenza Attuale: %d\n" +
                "  - Soglia Minima: %d\n\n" +
                "Si prega di procedere immediatamente con il riordino per evitare l'esaurimento.",
                prodotto, giacenzaAttuale, sogliaMinima
            );
            
            message.setText(emailContent);

           
            Transport.send(message);

        } catch (MessagingException e) {
           
            System.err.println("ERRORE CRITICO: Impossibile inviare l'email. Verifica le configurazioni SMTP.");
            e.printStackTrace();
        }
    }
    
    
    
 // --- NUOVO METODO: Per invio con allegato (Fattura PDF) ---
    public static void sendEmailWithAttachment(
        String host, String port, String username, String password,
        String destinatario, String oggetto, String corpoTesto, String allegatoPath
    ) throws MessagingException { // Rimuoviamo il try/catch per far propagare l'errore al Batch
        
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

        // NOTA: Invece di usare un try/catch interno, lanciamo l'eccezione
        // per permettere al batch di fare il rollback in caso di fallimento dell'invio.
        
        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress(username));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destinatario));
        message.setSubject(oggetto);

        // 2. Creazione del corpo del messaggio (Multipart)
        MimeMultipart multipart = new MimeMultipart();

        // A) Parte 1: Il corpo del testo
        MimeBodyPart messageBodyPart = new MimeBodyPart();
        messageBodyPart.setText(corpoTesto);
        multipart.addBodyPart(messageBodyPart);

        // B) Parte 2: L'allegato (il PDF)
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
			} // Metodo per allegare file da path
            attachmentPart.setFileName(file.getName()); // Usa il nome del file temporaneo
            multipart.addBodyPart(attachmentPart);
        } else {
            System.err.println("ATTENZIONE: File allegato non trovato al percorso: " + allegatoPath);
            // Non interrompe l'invio se è solo un warning
        }

        // 3. Imposta il contenuto multipart sul messaggio e invia
        message.setContent(multipart);
        Transport.send(message);
    }
}
