package com.valentin.tu_cv_spring_bot.TuCv.service;

import com.valentin.tu_cv_spring_bot.TuCv.mODEL.Orden;
import java.util.List;

public interface OrdenService {
    Orden crear(String itemsJson, double total);
    List<Orden> listar();
    void completar(Long id);
    void cancelar(Long id);
}
