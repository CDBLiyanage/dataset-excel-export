package com.halfmile.excelexport.service.impl;

import com.halfmile.excelexport.dto.res.ImportResponse;
import com.halfmile.excelexport.model.Person;
import com.halfmile.excelexport.repository.PersonRepository;
import com.halfmile.excelexport.service.ImportService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Service
public class ImportServiceImpl implements ImportService {

    private static final int BATCH_SIZE = 5000;

    private final PersonRepository repository;
    private final String csvPath;

    public ImportServiceImpl(PersonRepository repository,
                             @Value("${import.csv-path}") String csvPath) {
        this.repository = repository;
        this.csvPath = csvPath;
    }

    @Override
    public ImportResponse importDataset() throws IOException {
        repository.truncate();
        long total = 0;
        List<Person> batch = new ArrayList<>(BATCH_SIZE);

        try (BufferedReader reader = Files.newBufferedReader(Path.of(csvPath))) {
            reader.readLine(); // skip header
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) continue;
                String[] f = line.split(",", -1);
                batch.add(new Person(
                        Long.parseLong(f[0].trim()),
                        f[1].trim(),
                        f[2].trim(),
                        f[3].isBlank() ? null : Integer.valueOf(f[3].trim()),
                        f[4].trim()));
                if (batch.size() == BATCH_SIZE) {
                    repository.batchInsert(batch);
                    total += batch.size();
                    batch.clear();
                }
            }
            if (!batch.isEmpty()) {
                repository.batchInsert(batch);
                total += batch.size();
            }
        }
        return new ImportResponse(total, repository.count());
    }
}