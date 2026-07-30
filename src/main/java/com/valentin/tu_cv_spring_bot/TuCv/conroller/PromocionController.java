package com.valentin.tu_cv_spring_bot.TuCv.conroller;

import com.valentin.tu_cv_spring_bot.TuCv.mODEL.ProductCategory;
import com.valentin.tu_cv_spring_bot.TuCv.mODEL.Promocion;
import com.valentin.tu_cv_spring_bot.TuCv.mODEL.SubCategory;
import com.valentin.tu_cv_spring_bot.TuCv.service.PromocionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/productos/promos")
@RequiredArgsConstructor
public class PromocionController {

    private final PromocionService promocionService;

    @GetMapping
    public String listar(Model model) {
        List<Promocion> promos = promocionService.listar();
        model.addAttribute("promos", promos);
        model.addAttribute("isEmpty", promos.isEmpty());
        return "promos";
    }

    @GetMapping("/nueva")
    public String formNueva(Model model) {
        model.addAttribute("promo", new Promocion());
        model.addAttribute("Categorys", ProductCategory.values());
        model.addAttribute("SubCategorys", SubCategory.values());
        return "promo-form";
    }

    @PostMapping("/guardar")
    public String guardar(@ModelAttribute Promocion promo, RedirectAttributes ra) {
        try {
            promocionService.guardar(promo);
            ra.addFlashAttribute("mensaje", "Promocion creada correctamente");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Error al crear: " + e.getMessage());
        }
        return "redirect:/productos/promos";
    }

    @GetMapping("/editar/{id}")
    public String formEditar(@PathVariable Long id, Model model, RedirectAttributes ra) {
        Promocion promo = promocionService.obtener(id);
        if (promo == null) {
            ra.addFlashAttribute("error", "Promocion no encontrada");
            return "redirect:/productos/promos";
        }
        model.addAttribute("promo", promo);
        model.addAttribute("Categorys", ProductCategory.values());
        model.addAttribute("SubCategorys", SubCategory.values());
        return "promo-form";
    }

    @PostMapping("/actualizar")
    public String actualizar(@ModelAttribute Promocion promo, RedirectAttributes ra) {
        try {
            promocionService.actualizar(promo);
            ra.addFlashAttribute("mensaje", "Promocion actualizada correctamente");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Error al actualizar: " + e.getMessage());
        }
        return "redirect:/productos/promos";
    }

    @PostMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Long id, RedirectAttributes ra) {
        try {
            promocionService.eliminar(id);
            ra.addFlashAttribute("mensaje", "Promocion eliminada");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Error al eliminar: " + e.getMessage());
        }
        return "redirect:/productos/promos";
    }

    @PostMapping("/toggle/{id}")
    public String toggle(@PathVariable Long id, RedirectAttributes ra) {
        try {
            Promocion promo = promocionService.obtener(id);
            if (promo != null) {
                promo.setActiva(!promo.isActiva());
                promocionService.actualizar(promo);
                ra.addFlashAttribute("mensaje", "Promocion " + (promo.isActiva() ? "activada" : "desactivada"));
            }
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Error: " + e.getMessage());
        }
        return "redirect:/productos/promos";
    }

    @GetMapping("/api/activas")
    @ResponseBody
    public List<Promocion> apiActivas() {
        return promocionService.activas();
    }
}
