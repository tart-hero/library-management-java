package com.example.library;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "library_registrations")
public class LibraryRegistration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false, length = 20)
    private String gender;

    @Column(nullable = false)
    private LocalDate dob;

    @Column(nullable = false, length = 150)
    private String birthPlace;

    @Column(nullable = false, length = 12)
    private String cccd;

    @Column(nullable = false, length = 150)
    private String issuePlace;

    @Column(nullable = false, length = 150)
    private String specialization;

    @Column(nullable = false, length = 150)
    private String workplace;

    @Column(nullable = false, length = 255)
    private String address;

    @Column(nullable = false, length = 11)
    private String phone;

    @Column(nullable = false, length = 255)
    private String email;

    @Column(length = 255, unique = true)
    private String libraryUsername;

    @Column(length = 255)
    private String libraryPasswordHash;

    @Column(length = 120)
    private String notificationPasswordPlaintext;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private LibraryAccountStatus accountStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ActivationNotificationStatus activationNotificationStatus;

    private LocalDateTime accountActivatedAt;

    private LocalDate accountExpiresAt;

    private LocalDateTime activationNotificationPreparedAt;

    private LocalDateTime activationNotificationSentAt;

    @Column(nullable = false, length = 40, unique = true)
    private String storageCode;

    @Column(nullable = false, length = 120)
    private String storageLocation;

    @Column(nullable = false, length = 255)
    private String avatarFilename;

    @Column(nullable = false, length = 100)
    private String avatarContentType;

    @Lob
    @Column(nullable = false)
    private byte[] avatarData;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (accountStatus == null) {
            accountStatus = LibraryAccountStatus.PENDING_ACTIVATION;
        }
        if (activationNotificationStatus == null) {
            activationNotificationStatus = ActivationNotificationStatus.NOT_PREPARED;
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public LocalDate getDob() { return dob; }
    public void setDob(LocalDate dob) { this.dob = dob; }

    public String getBirthPlace() { return birthPlace; }
    public void setBirthPlace(String birthPlace) { this.birthPlace = birthPlace; }

    public String getCccd() { return cccd; }
    public void setCccd(String cccd) { this.cccd = cccd; }

    public String getIssuePlace() { return issuePlace; }
    public void setIssuePlace(String issuePlace) { this.issuePlace = issuePlace; }

    public String getSpecialization() { return specialization; }
    public void setSpecialization(String specialization) { this.specialization = specialization; }

    public String getWorkplace() { return workplace; }
    public void setWorkplace(String workplace) { this.workplace = workplace; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getLibraryUsername() { return libraryUsername; }
    public void setLibraryUsername(String libraryUsername) { this.libraryUsername = libraryUsername; }

    public String getLibraryPasswordHash() { return libraryPasswordHash; }
    public void setLibraryPasswordHash(String libraryPasswordHash) { this.libraryPasswordHash = libraryPasswordHash; }

    public String getNotificationPasswordPlaintext() { return notificationPasswordPlaintext; }
    public void setNotificationPasswordPlaintext(String notificationPasswordPlaintext) {
        this.notificationPasswordPlaintext = notificationPasswordPlaintext;
    }

    public LibraryAccountStatus getAccountStatus() { return accountStatus; }
    public void setAccountStatus(LibraryAccountStatus accountStatus) { this.accountStatus = accountStatus; }

    public ActivationNotificationStatus getActivationNotificationStatus() { return activationNotificationStatus; }
    public void setActivationNotificationStatus(ActivationNotificationStatus activationNotificationStatus) {
        this.activationNotificationStatus = activationNotificationStatus;
    }

    public LocalDateTime getAccountActivatedAt() { return accountActivatedAt; }
    public void setAccountActivatedAt(LocalDateTime accountActivatedAt) { this.accountActivatedAt = accountActivatedAt; }

    public LocalDate getAccountExpiresAt() { return accountExpiresAt; }
    public void setAccountExpiresAt(LocalDate accountExpiresAt) { this.accountExpiresAt = accountExpiresAt; }

    public LocalDateTime getActivationNotificationPreparedAt() { return activationNotificationPreparedAt; }
    public void setActivationNotificationPreparedAt(LocalDateTime activationNotificationPreparedAt) {
        this.activationNotificationPreparedAt = activationNotificationPreparedAt;
    }

    public LocalDateTime getActivationNotificationSentAt() { return activationNotificationSentAt; }
    public void setActivationNotificationSentAt(LocalDateTime activationNotificationSentAt) {
        this.activationNotificationSentAt = activationNotificationSentAt;
    }

    public String getStorageCode() { return storageCode; }
    public void setStorageCode(String storageCode) { this.storageCode = storageCode; }

    public String getStorageLocation() { return storageLocation; }
    public void setStorageLocation(String storageLocation) { this.storageLocation = storageLocation; }

    public String getAvatarFilename() { return avatarFilename; }
    public void setAvatarFilename(String avatarFilename) { this.avatarFilename = avatarFilename; }

    public String getAvatarContentType() { return avatarContentType; }
    public void setAvatarContentType(String avatarContentType) { this.avatarContentType = avatarContentType; }

    public byte[] getAvatarData() { return avatarData; }
    public void setAvatarData(byte[] avatarData) { this.avatarData = avatarData; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getGenderLabel() {
        return switch (gender) {
            case "male" -> "Nam";
            case "female" -> "Nu";
            default -> "Khac";
        };
    }

    public String getAccountStatusLabel() {
        if (accountStatus == null) {
            return "Chua kich hoat";
        }
        return switch (accountStatus) {
            case ACTIVATED -> "Da kich hoat";
            case PENDING_ACTIVATION -> "Chua kich hoat";
        };
    }

    public String getActivationNotificationStatusLabel() {
        if (activationNotificationStatus == null) {
            return "Chua soan thong bao";
        }
        return switch (activationNotificationStatus) {
            case READY_TO_SEND -> "Cho gui thong bao";
            case NOTIFIED -> "Da thong bao";
            case NOT_PREPARED -> "Chua soan thong bao";
        };
    }

    public boolean hasPendingNotificationPassword() {
        return notificationPasswordPlaintext != null && !notificationPasswordPlaintext.isBlank();
    }

    public boolean isNotificationReadyToSend() {
        return activationNotificationStatus == ActivationNotificationStatus.READY_TO_SEND && hasPendingNotificationPassword();
    }
}
