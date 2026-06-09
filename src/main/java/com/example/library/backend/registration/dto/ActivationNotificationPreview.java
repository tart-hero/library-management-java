package com.example.library.backend.registration.dto;

public record ActivationNotificationPreview(
        String recipientEmail,
        String subject,
        String body) {
}
