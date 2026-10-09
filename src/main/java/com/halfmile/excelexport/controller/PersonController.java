package com.halfmile.excelexport.controller;

import com.halfmile.excelexport.dto.res.PersonResponse;
import com.halfmile.excelexport.service.PersonService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class PersonController {

    private final PersonService personService;

    public PersonController(PersonService personService) {
        this.personService = personService;
    }

    @GetMapping("/records")
    public List<PersonResponse> getRecords(@RequestParam(defaultValue = "0") long afterId,
                                           @RequestParam(defaultValue = "5") int limit) {
        return personService.getBatch(afterId, Math.min(limit, 10_000));
    }
}