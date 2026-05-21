package com.example.library.backend.registration.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.library.backend.registration.model.ActivationNotificationStatus;
import com.example.library.backend.registration.model.LibraryAccountStatus;
import com.example.library.backend.registration.repository.LibraryRegistrationRepository;

@Controller
public class DashboardController {

    private final LibraryRegistrationRepository repository;

    public DashboardController(LibraryRegistrationRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {

        long totalRegistrations = repository.count();

        long activatedCount =
                repository.countByAccountStatus(
                        LibraryAccountStatus.ACTIVATED);

        long pendingCount =
                repository.countByAccountStatus(
                        LibraryAccountStatus.PENDING_ACTIVATION);

        long unsentMailCount =
                repository.countByActivationNotificationStatus(
                        ActivationNotificationStatus.READY_TO_SEND);

        List<Object[]> chartData =
                repository.countActivatedByMonth();

        List<String> months = new ArrayList<>();
        List<Long> counts = new ArrayList<>();

        for (Object[] row : chartData) {

            Integer month = (Integer) row[0];
            Long count = (Long) row[1];

            months.add("T" + month);
            counts.add(count);
        }

        model.addAttribute("totalRegistrations", totalRegistrations);
        model.addAttribute("activatedCount", activatedCount);
        model.addAttribute("pendingCount", pendingCount);
        model.addAttribute("unsentMailCount", unsentMailCount);

        model.addAttribute("months", months);
        model.addAttribute("counts", counts);

        return "dashboard";
    }
}