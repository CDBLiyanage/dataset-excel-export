package com.halfmile.excelexport.controller;

import com.halfmile.excelexport.dto.res.ExportResponse;
import com.halfmile.excelexport.service.ExportService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/api")
public class ExportController {

    private final ExportService exportService;

    public ExportController(ExportService exportService) {
        this.exportService = exportService;
    }

    @PostMapping("/exports")
    public ExportResponse export() throws IOException {
        return exportService.exportToExcel();
    }
}