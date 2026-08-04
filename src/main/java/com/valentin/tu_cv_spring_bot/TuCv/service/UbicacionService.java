package com.valentin.tu_cv_spring_bot.TuCv.service;

import java.util.List;
import java.util.Map;

import com.valentin.tu_cv_spring_bot.TuCv.mODEL.Ubicacion;

public interface UbicacionService {
    List<Ubicacion> list();

    void add(String nombre);

    void delete(Long id);

    Map<String, Map<Long, Integer>> getQuantitiesMap();

    void saveAllQuantities(Map<String, Map<Long, Integer>> porProducto);

    void adjust(String productName, String subCategoryName, Long ubicacionId, int cantidad);

    int getTotalStock(String productName, String subCategoryName);
}
