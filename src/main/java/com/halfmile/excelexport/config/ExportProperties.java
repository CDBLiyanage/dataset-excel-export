package com.halfmile.excelexport.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "export")
public record ExportProperties(int batchSize, int maxThreads, int maxConcurrentExports, int windowSize) {}