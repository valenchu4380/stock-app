package com.valentin.tu_cv_spring_bot.TuCv.service;

import java.util.List;
import com.valentin.tu_cv_spring_bot.TuCv.mODEL.Movement;

public interface MovementService {
    void save(Movement movement);
    List<Movement> getAllPaged(int page, int size);
    int getTotalPages(int size);
    int countAll();
}
