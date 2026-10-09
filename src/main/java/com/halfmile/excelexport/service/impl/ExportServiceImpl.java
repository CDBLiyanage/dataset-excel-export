package com.halfmile.excelexport.service.impl;

import com.halfmile.excelexport.config.ExportProperties;
import com.halfmile.excelexport.dto.res.ExportResponse;
import com.halfmile.excelexport.model.Person;
import com.halfmile.excelexport.repository.PersonRepository;
import com.halfmile.excelexport.service.ExportService;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;

@Service
public class ExportServiceImpl implements ExportService {

    private static final String[] HEADERS = {"ID", "Name", "Email", "Age", "Country"};

    private final PersonRepository repository;
    private final ExportProperties props;

    public ExportServiceImpl(PersonRepository repository, ExportProperties props) {
        this.repository = repository;
        this.props = props;
    }

    @Override
    public ExportResponse exportToExcel() throws IOException {
        long start = System.currentTimeMillis();

        Path dir = Path.of("output");
        Files.createDirectories(dir);
        Path tmpFile = dir.resolve("export.xlsx.tmp");
        Path finalFile = dir.resolve("export.xlsx");

        long rows = 0;
        SXSSFWorkbook workbook = new SXSSFWorkbook(props.windowSize());
        try {
            Sheet sheet = workbook.createSheet("Dataset");

            Row header = sheet.createRow(0);
            for (int i = 0; i < HEADERS.length; i++) {
                header.createCell(i).setCellValue(HEADERS[i]);
            }

            int rowIndex = 1;
            long lastId = 0;
            while (true) {
                List<Person> batch = repository.findBatchAfter(lastId, props.batchSize());
                if (batch.isEmpty()) break;

                for (Person p : batch) {
                    Row row = sheet.createRow(rowIndex++);
                    row.createCell(0).setCellValue(p.id());
                    row.createCell(1).setCellValue(p.name());
                    row.createCell(2).setCellValue(p.email());
                    if (p.age() != null) {
                        row.createCell(3).setCellValue(p.age());
                    }
                    row.createCell(4).setCellValue(p.country());
                }
                lastId = batch.get(batch.size() - 1).id();
                rows += batch.size();
            }

            try (OutputStream out = Files.newOutputStream(tmpFile)) {
                workbook.write(out);
            }
            // only rename once the file is complete
            Files.move(tmpFile, finalFile, StandardCopyOption.REPLACE_EXISTING);
        } finally {
            workbook.dispose(); // deletes POI's temp files
            workbook.close();
        }

        return new ExportResponse(finalFile.toString(), rows, props.batchSize(),
                System.currentTimeMillis() - start);
    }
}