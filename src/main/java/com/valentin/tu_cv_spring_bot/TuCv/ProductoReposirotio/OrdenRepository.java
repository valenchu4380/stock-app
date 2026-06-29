package com.valentin.tu_cv_spring_bot.TuCv.ProductoReposirotio;

import com.valentin.tu_cv_spring_bot.TuCv.mODEL.Orden;
import java.util.List;

public interface OrdenRepository {
    void save(Orden orden);
    List<Orden> findAll();
    Orden findById(Long id);
    void updateEstado(Long id, String estado);
}
