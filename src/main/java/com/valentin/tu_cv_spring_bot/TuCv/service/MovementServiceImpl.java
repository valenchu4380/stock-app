package com.valentin.tu_cv_spring_bot.TuCv.service;

import com.valentin.tu_cv_spring_bot.TuCv.ProductoReposirotio.MovementRepository;
import com.valentin.tu_cv_spring_bot.TuCv.mODEL.Movement;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MovementServiceImpl implements MovementService {

    private final MovementRepository movementRepository;

    @Override
    public void save(Movement movement) {
        movementRepository.save(movement);
    }

    @Override
    public List<Movement> getAllPaged(int page, int size) {
        int offset = page * size;
        return movementRepository.findAllPaged(offset, size);
    }

    @Override
    public int getTotalPages(int size) {
        int total = movementRepository.countAll();
        return (int) Math.ceil((double) total / size);
    }

    @Override
    public int countAll() {
        return movementRepository.countAll();
    }
}
