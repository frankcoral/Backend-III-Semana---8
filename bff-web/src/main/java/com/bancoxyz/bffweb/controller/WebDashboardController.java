package com.bancoxyz.bffweb.controller;

import com.bancoxyz.bffweb.dto.WebDashboardDto;
import com.bancoxyz.bffweb.service.WebDashboardService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/web")
public class WebDashboardController {
    private final WebDashboardService service;

    public WebDashboardController(WebDashboardService service) {
        this.service = service;
    }

    @GetMapping("/dashboard")
    public WebDashboardDto dashboard() {
        return service.dashboard();
    }
}
