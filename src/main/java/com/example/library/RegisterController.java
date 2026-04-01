package com.example.library;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
public class RegisterController {

    @GetMapping("/register")
    public String showForm() {
        return "register";
    }

    @PostMapping("/register")
@ResponseBody
public String submitForm(@ModelAttribute RegisterForm form) {

    return "Đăng ký thành công: " + form.getName()
            + " | Email: " + form.getEmail()
            + " | CCCD: " + form.getCccd();
}
}