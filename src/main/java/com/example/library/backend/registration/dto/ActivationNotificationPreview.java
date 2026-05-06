package com.example.library.backend.registration.dto;

public record ActivationNotificationPreview(
        String subject,
        String body,
        String mailtoLink) {
}
