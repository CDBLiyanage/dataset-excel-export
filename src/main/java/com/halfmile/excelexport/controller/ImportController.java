package com.halfmile.excelexport.controller;

import com.halfmile.excelexport.dto.res.ImportResponse;
import com.halfmile.excelexport.service.ImportService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/api")
public class ImportController {

    private final ImportService importService;

    public ImportController(ImportService importService) {
        this.importService = importService;
    }

    @GetMapping("/health")
    public String health() {
        return "ok";
    }

    @PostMapping("/imports")
    public ImportResponse runImport() throws IOException {
        return importService.importDataset();
    }
}