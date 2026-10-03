package com.bancoxyz.bffmobile.controller;

import com.bancoxyz.bffmobile.dto.MobileSummaryDto;
import com.bancoxyz.bffmobile.service.MobileResumenService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/mobile")
public class MobileResumenController {
    private final MobileResumenService service;

    public MobileResumenController(MobileResumenService service) {
        this.service = service;
    }

    @GetMapping("/resumen")
    public MobileSummaryDto resumen() {
        return service.resumen();
    }
}
