package com.example.library.backend.registration.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.example.library.LibraryApplication;
import com.example.library.backend.registration.model.ActivationNotificationStatus;
import com.example.library.backend.registration.model.LibraryAccountStatus;
import com.example.library.backend.registration.model.LibraryRegistration;
import com.example.library.backend.registration.repository.LibraryRegistrationRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(classes = LibraryApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RegisterControllerWebTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private LibraryRegistrationRepository registrationRepository;

    @BeforeEach
    void cleanDatabase() {
        registrationRepository.deleteAll();
    }

    @Test
    void showRegisterFormReturnsRegisterView() throws Exception {
        mockMvc.perform(get("/register"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attributeExists("registerForm"));
    }

    @Test
    void submitRegisterFormSavesRegistration() throws Exception {
        MockMultipartFile avatar = new MockMultipartFile(
                "avatar",
                "avatar.png",
                "image/png",
                "fake-image".getBytes());

        mockMvc.perform(multipart("/register")
                        .file(avatar)
                        .param("name", "Nguyen Van A")
                        .param("gender", "male")
                        .param("dob", "2000-01-01")
                        .param("birthPlace", "Ha Noi")
                        .param("cccd", "012345678901")
                        .param("issuePlace", "Ha Noi")
                        .param("specialization", "CNTT")
                        .param("workplace", "HUST")
                        .param("address", "Ha Noi")
                        .param("phone", "0987654321")
                        .param("email", "vana@example.com"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/register/success/*"));

        assertThat(registrationRepository.count()).isEqualTo(1);
        LibraryRegistration saved = registrationRepository.findAll().get(0);
        assertThat(saved.getName()).isEqualTo("Nguyen Van A");
        assertThat(saved.getEmail()).isEqualTo("vana@example.com");
        assertThat(saved.getCccd()).isEqualTo("012345678901");
        assertThat(saved.getAvatarFilename()).isEqualTo("avatar.png");
        assertThat(saved.getAvatarData()).isNotEmpty();
    }

    @Test
    void registerSuccessPageRendersSavedRegistration() throws Exception {
        LibraryRegistration saved = registrationRepository.save(createRegistration(
                "Do Anh Thu",
                "thu@example.com",
                "555555555555",
                "HS-20260402-A000",
                LocalDateTime.of(2026, 4, 2, 7, 45)));

        mockMvc.perform(get("/register/success/{id}", saved.getId()))
                .andExpect(status().isOk())
                .andExpect(view().name("register-success"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Ho so da duoc luu thanh cong")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Do Anh Thu")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Chua kich hoat")));
    }

    @Test
    void submitRegisterFormShowsValidationErrorsWhenDataIsInvalid() throws Exception {
        mockMvc.perform(multipart("/register")
                        .param("gender", "male")
                        .param("dob", "2000-01-01")
                        .param("birthPlace", "Ha Noi")
                        .param("cccd", "123")
                        .param("issuePlace", "Ha Noi")
                        .param("specialization", "CNTT")
                        .param("workplace", "HUST")
                        .param("address", "Ha Noi")
                        .param("phone", "09")
                        .param("email", "sai-dinh-dang"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attributeHasFieldErrors("registerForm", "name", "cccd", "phone", "email", "avatar"));

        assertThat(registrationRepository.count()).isZero();
    }

    @Test
    void registrationsPageSupportsSearchAndSorting() throws Exception {
        registrationRepository.save(createRegistration(
                "Tran Thi Lan",
                "lan@example.com",
                "123456789012",
                "HS-20260402-A001",
                LocalDateTime.of(2026, 4, 2, 8, 30)));
        registrationRepository.save(createRegistration(
                "Nguyen Van Minh",
                "minh@example.com",
                "999999999999",
                "HS-20260402-A002",
                LocalDateTime.of(2026, 4, 2, 9, 30)));

        mockMvc.perform(get("/registrations").param("q", "Lan"))
                .andExpect(status().isOk())
                .andExpect(view().name("registrations"))
                .andExpect(model().attributeExists("registrations"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Tran Thi Lan")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("HS-20260402-A001")))
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("Nguyen Van Minh"))));
    }

    @Test
    void registrationDetailShowsArchivedRecord() throws Exception {
        LibraryRegistration saved = registrationRepository.save(createRegistration(
                "Le Thu Ha",
                "ha@example.com",
                "456789012345",
                "HS-20260402-A003",
                LocalDateTime.of(2026, 4, 2, 10, 15)));

        mockMvc.perform(get("/registrations/{id}", saved.getId()))
                .andExpect(status().isOk())
                .andExpect(view().name("registration-detail"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Le Thu Ha")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("HS-20260402-A003")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Bo phan Luu hanh")));
    }

    @Test
    void activateAccountPreparesActivationNotification() throws Exception {
        LibraryRegistration saved = registrationRepository.save(createRegistration(
                "Pham Thu Trang",
                "trang@example.com",
                "777777777777",
                "HS-20260402-A004",
                LocalDateTime.of(2026, 4, 2, 11, 0)));

        mockMvc.perform(post("/registrations/{id}/activate-account", saved.getId()))
                .andExpect(status().is3xxRedirection());

        LibraryRegistration updated = registrationRepository.findById(saved.getId()).orElseThrow();
        assertThat(updated.getAccountStatus()).isEqualTo(LibraryAccountStatus.ACTIVATED);
        assertThat(updated.getActivationNotificationStatus()).isEqualTo(ActivationNotificationStatus.READY_TO_SEND);
        assertThat(updated.getLibraryUsername()).isEqualTo("trang@example.com");
        assertThat(updated.getLibraryPasswordHash()).isNotBlank();
        assertThat(updated.getNotificationPasswordPlaintext()).isNotBlank();
        assertThat(updated.getAccountActivatedAt()).isNotNull();
        assertThat(updated.getActivationNotificationPreparedAt()).isNotNull();

        mockMvc.perform(get("/registrations/{id}", saved.getId()))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Thong bao kich hoat san sang gui")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("trang@example.com")));
    }

    @Test
    void confirmNotificationMarksActivationAsCompleted() throws Exception {
        LibraryRegistration saved = createRegistration(
                "Vo Minh Chau",
                "chau@example.com",
                "888888888888",
                "HS-20260402-A005",
                LocalDateTime.of(2026, 4, 2, 12, 0));
        saved.setAccountStatus(LibraryAccountStatus.ACTIVATED);
        saved.setLibraryUsername("chau@example.com");
        saved.setLibraryPasswordHash("hashed-password");
        saved.setNotificationPasswordPlaintext("Secret1234");
        saved.setActivationNotificationStatus(ActivationNotificationStatus.READY_TO_SEND);
        saved.setActivationNotificationPreparedAt(LocalDateTime.of(2026, 4, 2, 12, 10));
        saved = registrationRepository.save(saved);

        mockMvc.perform(post("/registrations/{id}/confirm-notification", saved.getId()))
                .andExpect(status().is3xxRedirection());

        LibraryRegistration updated = registrationRepository.findById(saved.getId()).orElseThrow();
        assertThat(updated.getActivationNotificationStatus()).isEqualTo(ActivationNotificationStatus.NOTIFIED);
        assertThat(updated.getActivationNotificationSentAt()).isNotNull();
        assertThat(updated.getNotificationPasswordPlaintext()).isNull();
    }

    private LibraryRegistration createRegistration(
            String name,
            String email,
            String cccd,
            String storageCode,
            LocalDateTime createdAt) {
        LibraryRegistration registration = new LibraryRegistration();
        registration.setName(name);
        registration.setGender("female");
        registration.setDob(LocalDate.of(1999, 5, 20));
        registration.setBirthPlace("Ha Noi");
        registration.setCccd(cccd);
        registration.setIssuePlace("Ha Noi");
        registration.setSpecialization("CNTT");
        registration.setWorkplace("HUST");
        registration.setAddress("Ha Noi");
        registration.setPhone("0987654321");
        registration.setEmail(email);
        registration.setAccountStatus(LibraryAccountStatus.PENDING_ACTIVATION);
        registration.setActivationNotificationStatus(ActivationNotificationStatus.NOT_PREPARED);
        registration.setStorageCode(storageCode);
        registration.setStorageLocation("Bo phan Luu hanh");
        registration.setAvatarFilename("avatar.png");
        registration.setAvatarContentType("image/png");
        registration.setAvatarData("fake-image".getBytes());
        registration.setCreatedAt(createdAt);
        return registration;
    }
}
