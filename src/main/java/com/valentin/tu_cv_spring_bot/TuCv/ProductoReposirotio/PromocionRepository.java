package com.valentin.tu_cv_spring_bot.TuCv.ProductoReposirotio;

import com.valentin.tu_cv_spring_bot.TuCv.mODEL.Promocion;

import java.util.List;

public interface PromocionRepository {
    List<Promocion> findAll();
    Promocion findById(Long id);
    List<Promocion> findActivas();
    void save(Promocion promocion);
    void update(Promocion promocion);
    void delete(Long id);
}
