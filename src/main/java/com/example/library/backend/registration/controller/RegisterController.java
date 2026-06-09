package com.example.library.backend.registration.controller;

import java.io.IOException;

import com.example.library.backend.registration.dto.ActivationNotificationPreview;
import com.example.library.backend.registration.dto.RegisterForm;
import com.example.library.backend.registration.model.LibraryRegistration;
import com.example.library.backend.registration.service.RegisterService;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;

@Controller
public class RegisterController {

    private static final long MAX_AVATAR_SIZE = 5 * 1024 * 1024;

    private final RegisterService registerService;

    public RegisterController(RegisterService registerService) {
        this.registerService = registerService;
    }

    @GetMapping("/register")
    public String showForm(Model model) {
        if (!model.containsAttribute("registerForm")) {
            model.addAttribute("registerForm", new RegisterForm());
        }
        return "register";
    }

    @PostMapping("/register")
    public String submitForm(
            @Valid @ModelAttribute("registerForm") RegisterForm form,
            BindingResult bindingResult,
            Model model) {

        validateAvatar(form, bindingResult);
        if (bindingResult.hasErrors()) {
            model.addAttribute("registerForm", form);
            return "register";
        }

        try {
            registerService.saveRegistration(form);
            return "redirect:/register/success";
        } catch (IOException ex) {
            bindingResult.rejectValue("avatar", "avatar.upload", "Khong the luu anh dang ky. Vui long thu lai.");
            model.addAttribute("registerForm", form);
            return "register";
        }
    }

    @GetMapping("/register/success")
    public String showSuccess() {
        return "register-success";
    }

    @GetMapping("/registrations")
    public String showArchive(@RequestParam(name = "q", required = false) String keyword, Model model) {
        model.addAttribute("searchTerm", keyword == null ? "" : keyword);
        model.addAttribute("registrations", registerService.findRegistrations(keyword));
        return "registrations";
    }

    @GetMapping("/registrations/{id}")
    public String showRegistrationDetail(@PathVariable Long id, Model model) {
        LibraryRegistration registration = registerService.getRegistration(id);
        model.addAttribute("registration", registration);
        ActivationNotificationPreview notificationPreview = registerService.buildActivationNotificationPreview(registration);
        if (notificationPreview != null) {
            model.addAttribute("activationPreview", notificationPreview);
        }
        return "registration-detail";
    }

    @PostMapping("/registrations/{id}/activate-account")
    public String activateLibraryAccount(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            registerService.activateLibraryAccount(id);
            redirectAttributes.addFlashAttribute(
                    "statusMessage",
                    "Tài khoản thư viện đã được kích hoạt. Thông báo kích hoạt đã sẵn sàng để gửi qua SMTP.");
        } catch (IllegalArgumentException | IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/registrations/" + id;
    }

    @PostMapping("/registrations/{id}/send-notification")
    public String sendNotification(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            registerService.sendActivationNotification(id);
            redirectAttributes.addFlashAttribute(
                    "statusMessage",
                    "Email kích hoạt tài khoản thư viện đã được gửi thành công.");
        } catch (IllegalArgumentException | IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/registrations/" + id;
    }

    @GetMapping("/register/avatar/{id}")
    @ResponseBody
    public ResponseEntity<byte[]> showAvatar(@PathVariable Long id) {
        LibraryRegistration registration = registerService.getRegistration(id);
        if (registration.getAvatarData() == null || registration.getAvatarData().length == 0) {
            return ResponseEntity.notFound().build();
        }

        MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;
        if (registration.getAvatarContentType() != null && !registration.getAvatarContentType().isBlank()) {
            mediaType = MediaType.parseMediaType(registration.getAvatarContentType());
        }

        return ResponseEntity.ok()
                .contentType(mediaType)
                .body(registration.getAvatarData());
    }

    private void validateAvatar(RegisterForm form, BindingResult bindingResult) {
        if (form.getAvatar() == null || form.getAvatar().isEmpty()) {
            bindingResult.rejectValue("avatar", "avatar.required", "Vui long chon anh nguoi dang ky.");
            return;
        }

        if (form.getAvatar().getSize() > MAX_AVATAR_SIZE) {
            bindingResult.rejectValue("avatar", "avatar.size", "Anh dang ky khong duoc vuot qua 5MB.");
        }

        String contentType = form.getAvatar().getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            bindingResult.rejectValue("avatar", "avatar.type", "Anh dang ky phai la tep hinh anh.");
        }
    }
}
