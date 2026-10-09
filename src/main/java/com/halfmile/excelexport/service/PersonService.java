package com.halfmile.excelexport.service;

import com.halfmile.excelexport.dto.res.PersonResponse;
import java.util.List;

public interface PersonService {
    List<PersonResponse> getBatch(long afterId, int limit);
}