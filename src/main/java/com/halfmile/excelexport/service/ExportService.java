package com.halfmile.excelexport.service;

import com.halfmile.excelexport.dto.res.ExportResponse;
import java.io.IOException;

public interface ExportService {
    ExportResponse exportToExcel() throws IOException;
}