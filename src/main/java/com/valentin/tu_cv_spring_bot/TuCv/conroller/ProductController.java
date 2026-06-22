/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.valentin.tu_cv_spring_bot.TuCv.conroller;

import com.valentin.tu_cv_spring_bot.TuCv.Exception.InvalidProductException;
import com.valentin.tu_cv_spring_bot.TuCv.mODEL.Product;
import com.valentin.tu_cv_spring_bot.TuCv.mODEL.ProductCategory;
import com.valentin.tu_cv_spring_bot.TuCv.mODEL.SubCategory;
import com.valentin.tu_cv_spring_bot.TuCv.service.ProductService;

import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
public String index(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "") String name,
        @RequestParam(defaultValue = "") String category,
        @RequestParam(defaultValue = "") String subCategory,
        Model model) {

    int size = 15;
    try {
        // 1. Conversión de String a Enum (Manejo seguro)
        ProductCategory cat = (category == null || category.isEmpty()) ? null : ProductCategory.valueOf(category);
        SubCategory sub = (subCategory == null || subCategory.isEmpty()) ? null : SubCategory.valueOf(subCategory);

        // 2. Preparar paginación (ahora usamos Pageable)
        Pageable pageable = PageRequest.of(page, size);

        // 3. Llamar al servicio usando los nuevos tipos (Page, Enum, Pageable)
        Page<Product> paginaProductos = productService.getAllPaged(name, cat, sub, pageable);

        // 4. Pasar los resultados al modelo
        model.addAttribute("productos", paginaProductos.getContent());
        model.addAttribute("totalPaginas", paginaProductos.getTotalPages());
        model.addAttribute("paginaActual", page);
        
        // Mantén tus otros métodos igual, siempre que acepten los nuevos parámetros
        model.addAttribute("totalStock", productService.getStockTotal());
        model.addAttribute("inventario", productService.getInventarioTotal());
        model.addAttribute("stockNull", productService.getSinStockCount());
        model.addAttribute("totalRegistros", paginaProductos.getTotalElements());

        model.addAttribute("filtroName", name);
        model.addAttribute("filtroCategory", category);
        model.addAttribute("filtroSub", subCategory);

    } catch (Exception e) {
        // Limpieza en caso de error
        model.addAttribute("productos", java.util.Collections.emptyList());
        // ... (resto de atributos en 0 o vacíos)
    }
    
    model.addAttribute("Categorys", ProductCategory.values());
    model.addAttribute("SubCategorys", SubCategory.values());
    return "index";
}

    // ── Formulario agregar ──────────────────────────────
@GetMapping("/nuevo")
public String formNuevo(Model model) {
    model.addAttribute("product", new Product());
    model.addAttribute("Categorys", ProductCategory.values());
    model.addAttribute("SubCategorys", SubCategory.values());
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
    model.addAttribute("SubCategorys", SubCategory.values());
    return "form";
}


@PostMapping("/editar")
public String actualizar(@ModelAttribute Product product,
                         @RequestParam String oldName,
                         @RequestParam String oldSubCategory, 
                         RedirectAttributes ra) {
    try {
        SubCategory oldSubCat = SubCategory.valueOf(oldSubCategory);
        
        productService.update(product, oldName, oldSubCat);
        
        ra.addFlashAttribute("mensaje", "Producto modificado correctamente");
    } catch (Exception e) {
        ra.addFlashAttribute("error", "Error al actualizar: " + e.getMessage());
    }
    return "redirect:/productos";
}

    // ── Eliminar ────────────────────────────────────────
@PostMapping("/eliminar/{name}/{subCategory}")
public String eliminar(@PathVariable String name, 
                       @PathVariable("subCategory") String subCategoryStr,
                       RedirectAttributes ra) {
    

    try {
        SubCategory subCategory = SubCategory.valueOf(subCategoryStr);
        productService.delete(name, subCategory);
        ra.addFlashAttribute("mensaje", "Borrado con éxito");
    } catch (Exception e) {
        System.out.println("DEBUG: ERROR EN EL BORRADO: " + e.getMessage());
        ra.addFlashAttribute("error", "Error: " + e.getMessage());
    }
    return "redirect:/productos";
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
                });

        model.addAttribute("Categorys", ProductCategory.values());
        return "index";
    }

    




}