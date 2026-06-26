package com.valentin.tu_cv_spring_bot.TuCv.conroller;

import com.valentin.tu_cv_spring_bot.TuCv.Exception.InvalidProductException;
import com.valentin.tu_cv_spring_bot.TuCv.mODEL.Movement;
import com.valentin.tu_cv_spring_bot.TuCv.mODEL.Product;
import com.valentin.tu_cv_spring_bot.TuCv.mODEL.ProductCategory;
import com.valentin.tu_cv_spring_bot.TuCv.mODEL.SubCategory;
import com.valentin.tu_cv_spring_bot.TuCv.service.MovementService;
import com.valentin.tu_cv_spring_bot.TuCv.service.ProductService;

import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.ui.Model;

@Controller
@RequestMapping("/productos")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final MovementService movementService;

    @GetMapping
    public String index(
            @RequestParam(defaultValue = "0")    int page,
            @RequestParam(defaultValue = "")     String name,
            @RequestParam(defaultValue = "")     String category,
            @RequestParam(defaultValue = "")     String subCategory,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc")  String sortDir,
            @RequestParam(defaultValue = "false") boolean stockBajo,
            Model model) {

        int size = 15;
        try {
            List<Product> listaProductos = productService.getAllPaged(page, size, name, category, subCategory, sortBy, sortDir, stockBajo);
            int totalPages     = productService.getTotalPages(size, name, category, subCategory, stockBajo);
            int totalRegistros = productService.countFiltered(name, category, subCategory, stockBajo);
            int totalStock     = productService.sumStock(name, category, subCategory, stockBajo);
            double inventario  = productService.sumInventario(name, category, subCategory, stockBajo);
            int sinStock       = productService.countSinStock(name, category, subCategory, stockBajo);
            int stockBajoCount = productService.countStockBajo(name, category, subCategory);

            model.addAttribute("productos",      listaProductos);
            model.addAttribute("totalStock",     totalStock);
            model.addAttribute("inventario",     inventario);
            model.addAttribute("stockNull",      sinStock);
            model.addAttribute("stockBajoCount", stockBajoCount);
            model.addAttribute("paginaActual",   page);
            model.addAttribute("totalPaginas",   totalPages);
            model.addAttribute("totalRegistros", totalRegistros);
            model.addAttribute("filtroName",     name);
            model.addAttribute("filtroCategory", category);
            model.addAttribute("filtroSub",      subCategory);
            model.addAttribute("sortBy",         sortBy);
            model.addAttribute("sortDir",        sortDir);
            model.addAttribute("sortDirNext", "asc".equals(sortDir) ? "desc" : "asc");
            model.addAttribute("stockBajo",      stockBajo);
        } catch (InvalidProductException e) {
            model.addAttribute("productos",      java.util.Collections.emptyList());
            model.addAttribute("totalStock",     0);
            model.addAttribute("inventario",     0.0);
            model.addAttribute("stockNull",      0);
            model.addAttribute("stockBajoCount", 0);
            model.addAttribute("paginaActual",   0);
            model.addAttribute("totalPaginas",   0);
            model.addAttribute("totalRegistros", 0);
            model.addAttribute("filtroName",     "");
            model.addAttribute("filtroCategory", "");
            model.addAttribute("filtroSub",      "");
            model.addAttribute("sortBy",         "name");
            model.addAttribute("sortDir",        "asc");
            model.addAttribute("sortDirNext",    "desc");
            model.addAttribute("stockBajo",      false);
        }
        model.addAttribute("Categorys",    ProductCategory.values());
        model.addAttribute("SubCategorys", SubCategory.values());
        return "index";
    }

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
            Movement m = new Movement();
            m.setProductName(product.getName());
            m.setProductSubCategory(product.getSubCategory().name());
            m.setAction("CREATE");
            m.setNewPrice(product.getPrice());
            m.setNewStock(product.getStock());
            m.setTimestamp(LocalDateTime.now());
            movementService.save(m);
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
            Product oldProduct = productService.getByname(oldName).orElse(null);

            productService.update(product, oldName, oldSubCat);

            if (oldProduct != null) {
                Movement m = new Movement();
                m.setProductName(product.getName());
                m.setProductSubCategory(product.getSubCategory().name());
                m.setAction("UPDATE");
                m.setOldPrice(oldProduct.getPrice());
                m.setNewPrice(product.getPrice());
                m.setOldStock(oldProduct.getStock());
                m.setNewStock(product.getStock());
                m.setTimestamp(LocalDateTime.now());
                movementService.save(m);
            }

            ra.addFlashAttribute("mensaje", "Producto modificado correctamente");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Error al actualizar: " + e.getMessage());
        }
        return "redirect:/productos";
    }

    @PostMapping("/eliminar")
    public String eliminar(@RequestParam String name,
                           @RequestParam String subCategory,
                           RedirectAttributes ra) {
        try {
            Product oldProduct = productService.getByname(name).orElse(null);
            SubCategory sub = SubCategory.valueOf(subCategory.trim());
            productService.delete(name.trim(), sub);

            if (oldProduct != null) {
                Movement m = new Movement();
                m.setProductName(name);
                m.setProductSubCategory(sub.name());
                m.setAction("DELETE");
                m.setOldPrice(oldProduct.getPrice());
                m.setOldStock(oldProduct.getStock());
                m.setTimestamp(LocalDateTime.now());
                movementService.save(m);
            }

            ra.addFlashAttribute("mensaje", "Producto eliminado correctamente");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Error al eliminar: " + e.getMessage());
        }
        return "redirect:/productos";
    }

    @PostMapping("/actualizar-precios-sub")
    public String actualizarPreciosPorSubCategoria(
            @RequestParam String subCategory,
            @RequestParam double porcentaje,
            RedirectAttributes ra) {
        try {
            SubCategory sub = SubCategory.valueOf(subCategory);
            List<Product> affected = productService.findBynameAndSubCategoryForUpdate(null, sub);

            productService.actualizarpricesPorSubCategoria(sub, porcentaje);

            for (Product p : affected) {
                Movement m = new Movement();
                m.setProductName(p.getName());
                m.setProductSubCategory(sub.name());
                m.setAction("PRICE_UPDATE");
                m.setOldPrice(p.getPrice());
                m.setNewPrice(p.getPrice() * (1.0 + porcentaje / 100.0));
                m.setTimestamp(LocalDateTime.now());
                movementService.save(m);
            }

            ra.addFlashAttribute("mensaje", "Precios actualizados para " + sub + " (" + porcentaje + "%)");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Error: " + e.getMessage());
        }
        return "redirect:/productos";
    }

    @GetMapping("/movimientos")
    public String movimientos(
            @RequestParam(defaultValue = "0") int page,
            Model model) {
        int size = 20;
        List<Movement> movimientos = movementService.getAllPaged(page, size);
        int totalPages = movementService.getTotalPages(size);
        int totalRegistros = movementService.countAll();

        model.addAttribute("movimientos",   movimientos);
        model.addAttribute("paginaActual",  page);
        model.addAttribute("totalPaginas",  totalPages);
        model.addAttribute("totalRegistros", totalRegistros);
        return "movements";
    }

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
