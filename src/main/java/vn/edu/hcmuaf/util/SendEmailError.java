package vn.edu.hcmuaf.util;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

public class SendEmailError {

    public static void sendErrorEmail(String step, String message) {
        boolean test = false;
        String subject = "Thông báo lỗi Data WareHouse Kết Quả Xổ Số";
        String text = "<div style=\"background-color:#f2f2f2;padding:10px; font-size: 22px\">\n" +
                "  <p> Hệ thống đang gặp lỗi ở bước: <span style=\"font-weight: bold\">"+step+"</span></p>\n" +
                "  <p>Lỗi của hệ thống gặp phải là: <span style=\"font-weight: bold; color:red;\">"+message+"</span></p>\n" +
                "</div>";

        String toEmail = "Petshop.LTW10@gmail.com";
        String fromEmail = "Petshop.LTW10@gmail.com";
        String password ="sbwnlplqsqwujsjl";

        try {
            // your host email smtp server details
            Properties pr = new Properties();
            pr.setProperty("mail.smtp.host", "smtp.gmail.com");
            pr.setProperty("mail.smtp.port", "587");
            pr.setProperty("mail.smtp.auth", "true");
            pr.setProperty("mail.smtp.starttls.enable", "true");
            pr.put("mail.smtp.socketFactory.port", "587");
            pr.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");

            //get session to authenticate the host email address and password
            Session session = Session.getInstance(pr, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(fromEmail, password);
                }
            });

            //set email message details
            Message mess = new MimeMessage(session);

            //set from email address
            mess.setFrom(new InternetAddress(fromEmail));
            //set to email address or destination email address
            mess.setRecipient(Message.RecipientType.TO, new InternetAddress(toEmail));

            //set email subject
            mess.setSubject(subject);

            //set message text
            mess.setContent(text, "text/html; charset=UTF-8");
            //send the message
            Transport.send(mess);
            test=true;
            System.out.println("đã gửi mail");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        sendErrorEmail("crawl","This is a test error message.");
    }
}

