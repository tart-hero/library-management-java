package com.example.library;

import java.io.IOException;

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
            LibraryRegistration registration = registerService.saveRegistration(form);
            return "redirect:/register/success/" + registration.getId();
        } catch (IOException ex) {
            bindingResult.rejectValue("avatar", "avatar.upload", "Khong the luu anh dang ky. Vui long thu lai.");
            model.addAttribute("registerForm", form);
            return "register";
        }
    }

    @GetMapping("/register/success/{id}")
    public String showSuccess(@PathVariable Long id, Model model) {
        model.addAttribute("registration", registerService.getRegistration(id));
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
                    "Tai khoan thu vien da duoc kich hoat. Thong bao kich hoat da san sang de gui.");
        } catch (IllegalArgumentException | IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/registrations/" + id;
    }

    @PostMapping("/registrations/{id}/confirm-notification")
    public String confirmNotificationSent(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            registerService.confirmActivationNotificationSent(id);
            redirectAttributes.addFlashAttribute(
                    "statusMessage",
                    "Da ghi nhan viec thong bao kich hoat tai khoan thu vien cho ban doc.");
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
