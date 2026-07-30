package com.valentin.tu_cv_spring_bot.TuCv.service.impl;

import com.valentin.tu_cv_spring_bot.TuCv.ProductoReposirotio.PromocionRepository;
import com.valentin.tu_cv_spring_bot.TuCv.mODEL.Promocion;
import com.valentin.tu_cv_spring_bot.TuCv.service.PromocionService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PromocionServiceImpl implements PromocionService {

    private static final Logger log = LoggerFactory.getLogger(PromocionServiceImpl.class);
    private final PromocionRepository promocionRepository;

    @Override
    public List<Promocion> listar() {
        return promocionRepository.findAll();
    }

    @Override
    public Promocion obtener(Long id) {
        return promocionRepository.findById(id);
    }

    @Override
    public List<Promocion> activas() {
        return promocionRepository.findActivas();
    }

    @Override
    public void guardar(Promocion promocion) {
        promocionRepository.save(promocion);
    }

    @Override
    public void actualizar(Promocion promocion) {
        promocionRepository.update(promocion);
    }

    @Override
    public void eliminar(Long id) {
        promocionRepository.delete(id);
    }

    @Override
    public boolean esAplicable(Promocion promo, String category, String subCategory) {
        if (!promo.isActiva()) return false;
        if (promo.getFechaFin() != null && LocalDate.now().isAfter(promo.getFechaFin())) return false;
        if (promo.getFechaInicio() != null && LocalDate.now().isBefore(promo.getFechaInicio())) return false;
        if (!"PORCENTAJE".equals(promo.getTipoDescuento())) return false;
        if (promo.getTargetValor() == null || promo.getTargetValor().isBlank()) return false;

        String[] targets = promo.getTargetValor().split(",");
        for (String t : targets) {
            String target = t.trim();
            if ("CATEGORIA".equals(promo.getTipoTarget()) && target.equalsIgnoreCase(category)) return true;
            if ("SUBCATEGORIA".equals(promo.getTipoTarget()) && subCategory != null && target.equalsIgnoreCase(subCategory)) return true;
        }
        return false;
    }
}
