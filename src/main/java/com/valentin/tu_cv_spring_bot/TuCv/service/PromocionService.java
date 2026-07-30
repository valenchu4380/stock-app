package com.valentin.tu_cv_spring_bot.TuCv.service;

import com.valentin.tu_cv_spring_bot.TuCv.mODEL.Promocion;

import java.util.List;

public interface PromocionService {
    List<Promocion> listar();
    Promocion obtener(Long id);
    List<Promocion> activas();
    void guardar(Promocion promocion);
    void actualizar(Promocion promocion);
    void eliminar(Long id);
    boolean esAplicable(Promocion promo, String category, String subCategory);
}
