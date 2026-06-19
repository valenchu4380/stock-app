/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.valentin.tu_cv_spring_bot.TuCv.conroller;


import com.valentin.tu_cv_spring_bot.TuCv.Exception.InvalidProductException;
import com.valentin.tu_cv_spring_bot.TuCv.Exception.ProductNotFoundException;
import com.valentin.tu_cv_spring_bot.TuCv.mODEL.Product;
import com.valentin.tu_cv_spring_bot.TuCv.mODEL.ProductCategory;
import com.valentin.tu_cv_spring_bot.TuCv.service.ProductService;

import lombok.RequiredArgsConstructor;

import java.util.List;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.ui.Model;

/**
 *
 * @author User
 */
@Controller
@RequestMapping("/productos")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;



    // ── Pantalla principal ──────────────────────────────
@GetMapping
public String index(Model model) {
    try {
        java.util.List<Product> listaProductos = productService.getAll();
        int totalStock = listaProductos.stream().mapToInt(Product::getStock).sum();
        model.addAttribute("productos", listaProductos);
        model.addAttribute("totalStock", totalStock);
    } catch (InvalidProductException e) {
        // Lista vacía → mandamos lista vacía, no rompemos la página
        model.addAttribute("productos", java.util.Collections.emptyList());
        model.addAttribute("totalStock", 0);
    }
    model.addAttribute("Categorys", ProductCategory.values());
    return "index";
}

    // ── Formulario agregar ──────────────────────────────
    @GetMapping("/nuevo")
    public String formNuevo(Model model) {
        model.addAttribute("product", new Product());
        model.addAttribute("Categorys", ProductCategory.values());
        return "form";  
    }

    @PostMapping("/nuevo")
    public String guardar(@ModelAttribute Product product,
                          RedirectAttributes ra) {
        try {
            productService.save(product);
            ra.addFlashAttribute("mensaje", "Producto agregado correctamente");
        } catch (InvalidProductException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/productos";
    }

    @GetMapping("/editar/{name}")
    public String formEditar(@PathVariable String name, Model model) {
        productService.getByname(name).ifPresent(p ->
            model.addAttribute("product", p));
        model.addAttribute("Categorys", ProductCategory.values());
        return "form";
    }

@PostMapping("/editar")
public String actualizar(@ModelAttribute Product product,
                         @RequestParam String oldName,
                         RedirectAttributes ra) {
    try {
        productService.update(product, oldName);
        ra.addFlashAttribute("mensaje", "Producto modificado correctamente");
    } catch (ProductNotFoundException | InvalidProductException e) {
        ra.addFlashAttribute("error", e.getMessage());
    }
    return "redirect:/productos";
}

    // ── Eliminar ────────────────────────────────────────
    @PostMapping("/eliminar/{name}")
    public String eliminar(@PathVariable String name, RedirectAttributes ra) {
        try {
            productService.delete(name);
            ra.addFlashAttribute("mensaje", "Producto eliminado correctamente");
        } catch (ProductNotFoundException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/productos";
    }

    @GetMapping("/sugerencias")
@ResponseBody  // ← devuelve JSON, no una vista
public List<String> sugerencias(@RequestParam String q) {
    if (q == null || q.trim().length() < 1) {
        return java.util.Collections.emptyList();
    }
    try {
        return productService.getAll().stream()
            .map(Product::getName)
            .filter(name -> name.toLowerCase().contains(q.trim().toLowerCase()))
            .limit(5)
            .collect(java.util.stream.Collectors.toList());
    } catch (InvalidProductException e) {
        return java.util.Collections.emptyList();
    }
}

    // ── Buscar por name ───────────────────────────────
@GetMapping("/buscar")
public String buscar(@RequestParam String name, Model model) {

    String nameBuscado = name.trim().toLowerCase();

    productService.getByname(nameBuscado).ifPresentOrElse(
        p -> {
            java.util.List<Product> resultado = java.util.List.of(p);
            model.addAttribute("productos", resultado);
            model.addAttribute("totalStock", p.getStock());
        },
        () -> {
            model.addAttribute("productos", java.util.Collections.emptyList());
            model.addAttribute("totalStock", 0);
            model.addAttribute("error", "No se encontró el producto: " + name);
        }
    );

    model.addAttribute("Categorys", ProductCategory.values());
    return "index";
}
}