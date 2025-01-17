package me.cho.snackball.mail;

import jakarta.mail.*;
import jakarta.mail.internet.*;
import java.util.Properties;

public class SmtpTest {
    public static void main(String[] args) {
        String host = "smtp.gmail.com";
        int port = 587;
        String username = "email4running@gmail.com";
        String password = "xrrmxsolotnnxuaj"; // 애플리케이션 비밀번호

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", port);

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse("test-recipient@example.com"));
            message.setSubject("SMTP 테스트");
            message.setText("이것은 SMTP 테스트 메일입니다.");
            Transport.send(message);
            System.out.println("이메일 전송 성공");
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
}
