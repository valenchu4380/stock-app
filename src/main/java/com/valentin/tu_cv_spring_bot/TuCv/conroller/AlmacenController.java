package com.valentin.tu_cv_spring_bot.TuCv.conroller;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.valentin.tu_cv_spring_bot.TuCv.mODEL.Product;
import com.valentin.tu_cv_spring_bot.TuCv.mODEL.Ubicacion;
import com.valentin.tu_cv_spring_bot.TuCv.service.ProductService;
import com.valentin.tu_cv_spring_bot.TuCv.service.UbicacionService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/productos/almacen")
@RequiredArgsConstructor
public class AlmacenController {

    private final ProductService productService;
    private final UbicacionService ubicacionService;

    @GetMapping
    public String almacen(Model model) {
        List<Product> productos;
        try {
            productos = productService.getAllFiltered("", "", "", "");
        } catch (Exception e) {
            model.addAttribute("error", "Error al cargar productos: " + e.getMessage());
            model.addAttribute("productos", java.util.Collections.emptyList());
            model.addAttribute("ubicaciones", ubicacionService.list());
            return "almacen";
        }
        List<Ubicacion> ubicaciones = ubicacionService.list();
        Map<String, Map<Long, Integer>> qtyMap = ubicacionService.getQuantitiesMap();

        Long primerUbicacion = ubicaciones.isEmpty() ? null : ubicaciones.get(0).getId();
        for (Product p : productos) {
            String key = p.getName() + "|" + (p.getSubCategory() != null ? p.getSubCategory().name() : "");
            Map<Long, Integer> m = qtyMap.get(key);
            if ((m == null || m.isEmpty()) && p.getStock() > 0 && primerUbicacion != null) {
                Map<Long, Integer> seed = new LinkedHashMap<>();
                seed.put(primerUbicacion, p.getStock());
                qtyMap.put(key, seed);
            }
        }

        Map<String, Integer> totalMap = new HashMap<>();
        for (Product p : productos) {
            String key = p.getName() + "|" + (p.getSubCategory() != null ? p.getSubCategory().name() : "");
            Map<Long, Integer> m = qtyMap.get(key);
            int total = 0;
            if (m != null) {
                for (Integer c : m.values()) total += (c != null ? c : 0);
            }
            totalMap.put(key, total);
        }

        model.addAttribute("productos", productos);
        model.addAttribute("ubicaciones", ubicaciones);
        model.addAttribute("qtyMap", qtyMap);
        model.addAttribute("totalMap", totalMap);
        return "almacen";
    }

    @PostMapping("/guardar")
    public String guardar(@RequestParam Map<String, String> params, RedirectAttributes ra) {
        Map<String, Map<Long, Integer>> porProducto = new LinkedHashMap<>();
        for (Map.Entry<String, String> e : params.entrySet()) {
            if (!e.getKey().startsWith("cant|")) continue;
            String[] parts = e.getKey().split("\\|", 4);
            if (parts.length != 4) continue;
            String key = parts[1] + "|" + parts[2];
            int cantidad = 0;
            try {
                cantidad = Integer.parseInt(e.getValue());
            } catch (NumberFormatException ex) {
                cantidad = 0;
            }
            if (cantidad < 0) cantidad = 0;
            Long ubicacionId = Long.parseLong(parts[3]);
            porProducto.computeIfAbsent(key, k -> new HashMap<>()).put(ubicacionId, cantidad);
        }

        int actualizados = porProducto.size();
        try {
            ubicacionService.saveAllQuantities(porProducto);
            ra.addFlashAttribute("mensaje", "Almacén actualizado (" + actualizados + " productos)");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Error al guardar el almacén: " + e.getMessage());
        }
        return "redirect:/productos/almacen";
    }

    @PostMapping("/ubicacion/agregar")
    public String agregarUbicacion(@RequestParam String nombre, RedirectAttributes ra) {
        String nombreLimpio = nombre == null ? "" : nombre.trim();
        if (nombreLimpio.isEmpty()) {
            ra.addFlashAttribute("error", "Ingresá un nombre para la ubicación");
        } else {
            ubicacionService.add(nombreLimpio);
            ra.addFlashAttribute("mensaje", "Ubicación '" + nombreLimpio + "' agregada");
        }
        return "redirect:/productos/almacen";
    }

    @PostMapping("/ubicacion/eliminar")
    public String eliminarUbicacion(@RequestParam Long id, RedirectAttributes ra) {
        ubicacionService.delete(id);
        ra.addFlashAttribute("mensaje", "Ubicación eliminada y stock recalculado");
        return "redirect:/productos/almacen";
    }
}
