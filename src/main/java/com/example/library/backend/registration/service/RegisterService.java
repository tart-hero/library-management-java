package com.example.library.backend.registration.service;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import com.example.library.backend.registration.dto.ActivationNotificationPreview;
import com.example.library.backend.registration.dto.RegisterForm;
import com.example.library.backend.registration.model.ActivationNotificationStatus;
import com.example.library.backend.registration.model.LibraryAccountStatus;
import com.example.library.backend.registration.model.LibraryRegistration;
import com.example.library.backend.registration.repository.LibraryRegistrationRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class RegisterService {

    private static final String STORAGE_LOCATION = "Bo phan Luu hanh";
    private static final DateTimeFormatter STORAGE_CODE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final char[] TEMP_PASSWORD_CHARS =
            "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789".toCharArray();
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final LibraryRegistrationRepository registrationRepository;

    public RegisterService(LibraryRegistrationRepository registrationRepository) {
        this.registrationRepository = registrationRepository;
    }

    @Transactional
    public LibraryRegistration saveRegistration(RegisterForm form) throws IOException {
        LibraryRegistration registration = new LibraryRegistration();
        LocalDateTime createdAt = LocalDateTime.now();
        registration.setName(form.getName().trim());
        registration.setGender(form.getGender());
        registration.setDob(form.getDob());
        registration.setBirthPlace(form.getBirthPlace().trim());
        registration.setCccd(form.getCccd().trim());
        registration.setIssuePlace(form.getIssuePlace().trim());
        registration.setSpecialization(form.getSpecialization().trim());
        registration.setWorkplace(form.getWorkplace().trim());
        registration.setAddress(form.getAddress().trim());
        registration.setPhone(form.getPhone().trim());
        registration.setEmail(form.getEmail().trim());
        registration.setAccountStatus(LibraryAccountStatus.PENDING_ACTIVATION);
        registration.setActivationNotificationStatus(ActivationNotificationStatus.NOT_PREPARED);
        registration.setCreatedAt(createdAt);
        registration.setStorageCode(generateStorageCode(createdAt));
        registration.setStorageLocation(STORAGE_LOCATION);
        registration.setAvatarFilename(form.getAvatar().getOriginalFilename());
        registration.setAvatarContentType(form.getAvatar().getContentType());
        registration.setAvatarData(form.getAvatar().getBytes());
        return registrationRepository.save(registration);
    }

    @Transactional(readOnly = true)
    public LibraryRegistration getRegistration(Long id) {
        return registrationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Khong tim thay ho so dang ky voi id " + id));
    }

    @Transactional(readOnly = true)
    public List<LibraryRegistration> findRegistrations(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return registrationRepository.findAllByOrderByCreatedAtDescIdDesc();
        }
        return registrationRepository.searchArchive(keyword.trim());
    }

    @Transactional
    public LibraryRegistration activateLibraryAccount(Long id) {
        LibraryRegistration registration = getRegistration(id);
        String libraryUsername = normalizeUsername(registration.getEmail());

        if (registrationRepository.existsByLibraryUsernameIgnoreCaseAndIdNot(libraryUsername, registration.getId())) {
            throw new IllegalStateException("Email nay da duoc dung cho mot tai khoan thu vien khac.");
        }

        LocalDateTime now = LocalDateTime.now();
        String temporaryPassword = generateTemporaryPassword();

        registration.setLibraryUsername(libraryUsername);
        registration.setLibraryPasswordHash(hashPassword(temporaryPassword));
        registration.setNotificationPasswordPlaintext(temporaryPassword);
        registration.setAccountStatus(LibraryAccountStatus.ACTIVATED);
        if (registration.getAccountActivatedAt() == null) {
            registration.setAccountActivatedAt(now);
        }
        if (registration.getAccountExpiresAt() == null) {
            registration.setAccountExpiresAt(LocalDate.now().plusYears(1));
        }
        registration.setActivationNotificationStatus(ActivationNotificationStatus.READY_TO_SEND);
        registration.setActivationNotificationPreparedAt(now);
        registration.setActivationNotificationSentAt(null);

        return registrationRepository.save(registration);
    }

    @Transactional
    public LibraryRegistration confirmActivationNotificationSent(Long id) {
        LibraryRegistration registration = getRegistration(id);
        if (!registration.isNotificationReadyToSend()) {
            throw new IllegalStateException("Ho so nay chua co thong bao kich hoat san sang de gui.");
        }

        registration.setActivationNotificationStatus(ActivationNotificationStatus.NOTIFIED);
        registration.setActivationNotificationSentAt(LocalDateTime.now());
        registration.setNotificationPasswordPlaintext(null);
        return registrationRepository.save(registration);
    }

    @Transactional(readOnly = true)
    public ActivationNotificationPreview buildActivationNotificationPreview(LibraryRegistration registration) {
        if (registration == null || !registration.isNotificationReadyToSend()) {
            return null;
        }

        String subject = "Thong bao kich hoat tai khoan thu vien";
        String body = """
                Kinh gui %s,

                Tai khoan thu vien cua ban da duoc kich hoat.

                Ten dang nhap: %s
                Mat khau tam thoi: %s
                Han su dung tai khoan: %s
                Ma ho so: %s

                Vui long doi mat khau sau lan dang nhap dau tien.

                Tran trong,
                Bo phan Luu hanh Thu vien
                """.formatted(
                registration.getName(),
                registration.getLibraryUsername(),
                registration.getNotificationPasswordPlaintext(),
                DateTimeFormatter.ofPattern("dd/MM/yyyy").format(registration.getAccountExpiresAt()),
                registration.getStorageCode());

        String mailtoLink = "mailto:" + urlEncode(registration.getEmail())
                + "?subject=" + urlEncode(subject)
                + "&body=" + urlEncode(body);

        return new ActivationNotificationPreview(subject, body, mailtoLink);
    }

    private String generateStorageCode(LocalDateTime createdAt) {
        String suffix = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        return "HS-" + createdAt.format(STORAGE_CODE_TIME_FORMAT) + "-" + suffix;
    }

    private String normalizeUsername(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private String generateTemporaryPassword() {
        StringBuilder password = new StringBuilder(10);
        for (int i = 0; i < 10; i++) {
            password.append(TEMP_PASSWORD_CHARS[SECURE_RANDOM.nextInt(TEMP_PASSWORD_CHARS.length)]);
        }
        return password.toString();
    }

    private String hashPassword(String rawPassword) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            byte[] digest = messageDigest.digest(rawPassword.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(digest);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("Khong the ma hoa mat khau tam thoi.", ex);
        }
    }

    private String urlEncode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
