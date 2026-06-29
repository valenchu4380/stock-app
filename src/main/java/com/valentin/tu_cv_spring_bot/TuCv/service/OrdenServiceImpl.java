package com.valentin.tu_cv_spring_bot.TuCv.service;

import com.valentin.tu_cv_spring_bot.TuCv.ProductoReposirotio.OrdenRepository;
import com.valentin.tu_cv_spring_bot.TuCv.ProductoReposirotio.ProductRepository;
import com.valentin.tu_cv_spring_bot.TuCv.mODEL.Orden;
import com.valentin.tu_cv_spring_bot.TuCv.mODEL.OrdenItem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrdenServiceImpl implements OrdenService {

    private final OrdenRepository ordenRepository;
    private final ProductRepository productRepository;

    @Override
    public Orden crear(String itemsJson, double total) {
        Orden o = new Orden();
        o.setItemsJson(itemsJson);
        o.setTotal(BigDecimal.valueOf(total));
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
        List<OrdenItem> items = new ArrayList<>();
        json = json.trim();
        if (json.startsWith("[") && json.endsWith("]")) {
            json = json.substring(1, json.length() - 1).trim();
        }
        if (json.isEmpty()) return items;

        String[] parts = json.split("\\}\\s*,\\s*\\{");
        for (String part : parts) {
            items.add(parseItem(part));
        }
        return items;
    }

    private OrdenItem parseItem(String json) {
        OrdenItem item = new OrdenItem();
        item.setName(extractString(json, "name"));
        item.setSubCategory(extractString(json, "subCategory"));
        item.setCantidad(Integer.parseInt(extractNumber(json, "cantidad")));
        item.setPrice(new BigDecimal(extractNumber(json, "price")));
        return item;
    }

    private String extractString(String json, String key) {
        String search = "\"" + key + "\":\"";
        int start = json.indexOf(search);
        if (start == -1) return "";
        start += search.length();
        int end = json.indexOf("\"", start);
        return end > start ? json.substring(start, end) : "";
    }

    private String extractNumber(String json, String key) {
        String search = "\"" + key + "\":";
        int start = json.indexOf(search);
        if (start == -1) return "0";
        start += search.length();
        int end = json.indexOf(",", start);
        if (end == -1) end = json.indexOf("}", start);
        if (end == -1) end = json.length();
        return json.substring(start, end).trim();
    }
}
