package com.phatpl.metube._utils;

import org.springframework.mail.SimpleMailMessage;

import java.util.Random;

public class MailUtil {
    public static int genCode() {
        Random random = new Random();
        return 1000 + Math.abs(random.nextInt()) % 1000;
    }

    public static SimpleMailMessage genMail(String userEmail, int code) {
        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setSubject("CODE");
        mail.setFrom("lephiphatphat@gmail.com");
        mail.setText("Your code = " + String.valueOf(code));
        mail.setTo(userEmail);
        return mail;
    }
}
