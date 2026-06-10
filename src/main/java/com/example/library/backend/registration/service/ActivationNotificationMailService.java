package com.example.library.backend.registration.service;

import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.mail.MailProperties;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class ActivationNotificationMailService {

    private final ObjectProvider<JavaMailSender> mailSenderProvider;
    private final MailProperties mailProperties;
    private final String configuredFrom;

    public ActivationNotificationMailService(
            ObjectProvider<JavaMailSender> mailSenderProvider,
            MailProperties mailProperties,
            @Value("${library.mail.from:}") String configuredFrom) {
        this.mailSenderProvider = mailSenderProvider;
        this.mailProperties = mailProperties;
        this.configuredFrom = configuredFrom;
    }

    public void sendActivationNotification(String recipientEmail, String subject, String body) {
        if (!StringUtils.hasText(mailProperties.getHost())) {
            throw new IllegalStateException(
                    "SMTP chưa được cấu hình. Vui lòng thiết lập SMTP_HOST, SMTP_USERNAME và SMTP_PASSWORD.");
        }

        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
        if (mailSender == null) {
            throw new IllegalStateException("Không thể tạo bộ gửi email SMTP cho cấu hình hiện tại.");
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, StandardCharsets.UTF_8.name());
            helper.setTo(recipientEmail);
            helper.setSubject(subject);
            helper.setText(body, false);

            String from = resolveFromAddress();
            if (StringUtils.hasText(from)) {
                helper.setFrom(from);
            }

            mailSender.send(message);
        } catch (MessagingException | MailException ex) {
            throw new IllegalStateException(
                    "Không gửi được email kích hoạt qua SMTP. Vui lòng kiểm tra host, port, tài khoản và mật khẩu SMTP.",
                    ex);
        }
    }

    private String resolveFromAddress() {
        if (StringUtils.hasText(configuredFrom)) {
            return configuredFrom.trim();
        }
        if (StringUtils.hasText(mailProperties.getUsername())) {
            return mailProperties.getUsername().trim();
        }
        return "";
    }
}
