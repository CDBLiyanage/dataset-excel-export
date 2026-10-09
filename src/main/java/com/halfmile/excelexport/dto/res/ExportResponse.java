package com.halfmile.excelexport.dto.res;

public record ExportResponse(String file, long rows, int batchSize, long durationMs) {}