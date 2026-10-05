# dataset-excel-export
Spring Boot project that imports a ~500k-row CSV into MySQL and exports it to Excel efficiently, using keyset pagination, batch processing, and Apache POI's streaming SXSSFWorkbook. Benchmarks single-worker vs. controlled multithreading.
