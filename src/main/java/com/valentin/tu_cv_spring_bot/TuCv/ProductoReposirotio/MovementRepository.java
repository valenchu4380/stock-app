package com.valentin.tu_cv_spring_bot.TuCv.ProductoReposirotio;

import java.util.List;
import com.valentin.tu_cv_spring_bot.TuCv.mODEL.Movement;

public interface MovementRepository {
    void save(Movement movement);
    List<Movement> findAllPaged(int offset, int limit);
    int countAll();
}
