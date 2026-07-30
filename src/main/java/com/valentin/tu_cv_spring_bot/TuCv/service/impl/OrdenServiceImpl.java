package com.valentin.tu_cv_spring_bot.TuCv.service.impl;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import com.valentin.tu_cv_spring_bot.TuCv.ProductoReposirotio.OrdenRepository;
import com.valentin.tu_cv_spring_bot.TuCv.ProductoReposirotio.ProductRepository;
import com.valentin.tu_cv_spring_bot.TuCv.mODEL.Orden;
import com.valentin.tu_cv_spring_bot.TuCv.mODEL.OrdenItem;
import com.valentin.tu_cv_spring_bot.TuCv.mODEL.Promocion;
import com.valentin.tu_cv_spring_bot.TuCv.service.OrdenService;
import com.valentin.tu_cv_spring_bot.TuCv.service.PromocionService;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrdenServiceImpl implements OrdenService {

    private static final Logger log = LoggerFactory.getLogger(OrdenServiceImpl.class);

    private final OrdenRepository ordenRepository;
    private final ProductRepository productRepository;
    private final ObjectMapper objectMapper;
    private final PromocionService promocionService;

    @Override
    @Transactional
    public Orden crear(String itemsJson, double total) {
        List<OrdenItem> items = parseItems(itemsJson);
        List<Promocion> activas = promocionService.activas();
        double totalConDescuento = 0;
        for (OrdenItem item : items) {
            double precio = item.getPrice().doubleValue();
            for (Promocion promo : activas) {
                if (promocionService.esAplicable(promo, item.getCategory(), item.getSubCategory())) {
                    precio = precio * (1.0 - promo.getDescuento() / 100.0);
                    break;
                }
            }
            totalConDescuento += precio * item.getCantidad();
        }
        Orden o = new Orden();
        o.setItemsJson(itemsJson);
        o.setTotal(BigDecimal.valueOf(totalConDescuento));
        o.setEstado("PENDIENTE");
        ordenRepository.save(o);
        return o;
    }

    @Override
    public List<Orden> listar() {
        return ordenRepository.findAll();
    }

    @Override
    @Transactional
    public void completar(Long id) {
        Orden o = ordenRepository.findById(id);
        if (o == null) throw new RuntimeException("Orden no encontrada");

        List<OrdenItem> items = parseItems(o.getItemsJson());
        for (OrdenItem item : items) {
            productRepository.reduceStock(item.getName(), item.getSubCategory(), item.getCantidad());
        }

        ordenRepository.updateEstado(id, "COMPLETADA");
    }

    @Override
    public void cancelar(Long id) {
        ordenRepository.updateEstado(id, "CANCELADA");
    }

    private List<OrdenItem> parseItems(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<List<OrdenItem>>() {});
        } catch (Exception e) {
            log.error("Error parsing order items JSON: {}", json, e);
            return List.of();
        }
    }
}
