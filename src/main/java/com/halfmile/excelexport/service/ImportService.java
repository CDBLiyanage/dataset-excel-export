package com.halfmile.excelexport.service;

import com.halfmile.excelexport.dto.res.ImportResponse;

import java.io.IOException;

public interface ImportService {
    ImportResponse importDataset() throws IOException;
}