package com.halfmile.excelexport.service.impl;

import com.halfmile.excelexport.dto.res.PersonResponse;
import com.halfmile.excelexport.repository.PersonRepository;
import com.halfmile.excelexport.service.PersonService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PersonServiceImpl implements PersonService {

    private final PersonRepository repository;

    public PersonServiceImpl(PersonRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<PersonResponse> getBatch(long afterId, int limit) {
        return repository.findBatchAfter(afterId, limit).stream()
                .map(p -> new PersonResponse(p.id(), p.name(), p.email(), p.age(), p.country()))
                .toList();
    }
}