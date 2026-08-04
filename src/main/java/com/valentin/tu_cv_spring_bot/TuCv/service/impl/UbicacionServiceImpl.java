package com.valentin.tu_cv_spring_bot.TuCv.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.valentin.tu_cv_spring_bot.TuCv.ProductoReposirotio.UbicacionRepository;
import com.valentin.tu_cv_spring_bot.TuCv.mODEL.Ubicacion;
import com.valentin.tu_cv_spring_bot.TuCv.service.UbicacionService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UbicacionServiceImpl implements UbicacionService {

    private final UbicacionRepository ubicacionRepository;

    @Override
    public List<Ubicacion> list() {
        return ubicacionRepository.list();
    }

    @Override
    public void add(String nombre) {
        ubicacionRepository.add(nombre);
    }

    @Override
    public void delete(Long id) {
        ubicacionRepository.delete(id);
    }

    @Override
    public Map<String, Map<Long, Integer>> getQuantitiesMap() {
        return ubicacionRepository.getQuantitiesMap();
    }

    @Override
    public void saveAllQuantities(Map<String, Map<Long, Integer>> porProducto) {
        ubicacionRepository.saveAllQuantities(porProducto);
    }

    @Override
    public void adjust(String productName, String subCategoryName, Long ubicacionId, int cantidad) {
        ubicacionRepository.adjust(productName, subCategoryName, ubicacionId, cantidad);
    }

    @Override
    public int getTotalStock(String productName, String subCategoryName) {
        return ubicacionRepository.getTotalStock(productName, subCategoryName);
    }
}
