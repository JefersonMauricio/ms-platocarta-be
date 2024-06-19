package app.infrastructure.email;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender emailSender;

    public EmailService(JavaMailSender emailSender) {
        this.emailSender = emailSender;
    }

    public void sendSimpleMessage(String from, String to, String subject, String text) {        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(to);
        message.setSubject("Reserva registrada con exito");
        message.setText("Hemos recibo tu reserva con exito, en breve te enviaremos la confirmacion. Gracias por preferirnos.");
        emailSender.send(message);
    }
}
