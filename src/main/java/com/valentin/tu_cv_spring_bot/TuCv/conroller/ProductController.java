package com.valentin.tu_cv_spring_bot.TuCv.conroller;

import com.valentin.tu_cv_spring_bot.TuCv.Exception.InvalidProductException;
import com.valentin.tu_cv_spring_bot.TuCv.mODEL.Movement;
import com.valentin.tu_cv_spring_bot.TuCv.mODEL.Orden;
import com.valentin.tu_cv_spring_bot.TuCv.mODEL.Product;
import com.valentin.tu_cv_spring_bot.TuCv.mODEL.ProductCategory;
import com.valentin.tu_cv_spring_bot.TuCv.mODEL.SubCategory;
import com.valentin.tu_cv_spring_bot.TuCv.service.MovementService;
import com.valentin.tu_cv_spring_bot.TuCv.service.OrdenService;
import com.valentin.tu_cv_spring_bot.TuCv.service.ProductService;

import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
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

@Controller
@RequestMapping("/productos")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final MovementService movementService;
    private final OrdenService ordenService;

    @Value("${whatsapp.number:543854202134}")
    private String whatsappNumber;

    @GetMapping
    public String index(
            @RequestParam(defaultValue = "")     String name,
            @RequestParam(defaultValue = "")     String category,
            @RequestParam(defaultValue = "")     String subCategory,
            @RequestParam(defaultValue = "")     String linea,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc")  String sortDir,
            Model model) {

        int size = 15;
        try {
            List<Product> listaProductos = productService.getAllPaged(0, size, name, category, subCategory, linea, sortBy, sortDir, false);

            model.addAttribute("productos",      listaProductos);
            model.addAttribute("filtroName",     name);
            model.addAttribute("filtroCategory", category);
            model.addAttribute("filtroSub",      subCategory);
            model.addAttribute("filtroLinea",    linea);
            model.addAttribute("sortBy",         sortBy);
            model.addAttribute("sortDir",        sortDir);
            model.addAttribute("sortDirNext", "asc".equals(sortDir) ? "desc" : "asc");

        } catch (InvalidProductException e) {
           model.addAttribute("error", "Error al cargar productos");
        }
        model.addAttribute("Categorys",    ProductCategory.values());
        model.addAttribute("SubCategorys", SubCategory.values());
        model.addAttribute("whatsappNum", whatsappNumber);
        return "index";
    }



    @GetMapping("/mas")
    public String mas(
            @RequestParam(defaultValue = "0")    int page,
            @RequestParam(defaultValue = "")     String name,
            @RequestParam(defaultValue = "")     String category,
            @RequestParam(defaultValue = "")     String subCategory,
            @RequestParam(defaultValue = "")     String linea,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc")  String sortDir,
            @RequestParam(defaultValue = "false") boolean stockBajo,
            Model model) {
        int size = 15;
        try {
            List<Product> listaProductos = productService.getAllPaged(page, size, name, category, subCategory, linea, sortBy, sortDir, stockBajo);
            model.addAttribute("productos", listaProductos);
        } catch (InvalidProductException e) {
            model.addAttribute("productos", java.util.Collections.emptyList());
        }
        model.addAttribute("whatsappNum", whatsappNumber);
        return "fragments/producto-cards :: cardGrid";
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
        return "redirect:/productos/nuevo";
    }

    @GetMapping({"/editar/{name}/{subCategory}", "/editar/{name}"})
    public String formEditar(@PathVariable String name,
                             @PathVariable(required = false) String subCategory,
                             Model model) {
        if (subCategory != null && !subCategory.isBlank()) {
            try {
                SubCategory sc = SubCategory.valueOf(subCategory);
                productService.findBynameAndSubCategoryForUpdate(name, sc)
                    .stream().findFirst().ifPresent(p -> model.addAttribute("product", p));
            } catch (IllegalArgumentException e) {
                productService.getByname(name).ifPresent(p ->
                    model.addAttribute("product", p));
            }
        } else {
            productService.getByname(name).ifPresent(p ->model.addAttribute("product", p));
        }
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

    @PostMapping("/editar-masivo")
    @ResponseBody
    public Map<String, Object> editarMasivo(@RequestParam String items,
                                             @RequestParam(required = false) String price,
                                             @RequestParam(required = false) String costPrice,
                                             @RequestParam(required = false) String stock) {
        List<String> itemList = List.of(items.split(","));
        Double p = (price != null && !price.isBlank()) ? Double.parseDouble(price) : null;
        Double cp = (costPrice != null && !costPrice.isBlank()) ? Double.parseDouble(costPrice) : null;
        Integer s = (stock != null && !stock.isBlank()) ? Integer.parseInt(stock) : null;
        try {
            productService.batchUpdateFields(itemList, p, cp, s);
            return Map.of("success", true, "message", "Productos actualizados correctamente");
        } catch (Exception e) {
            return Map.of("success", false, "message", "Error: " + e.getMessage());
        }
    }

    @PostMapping("/ajustar-stock")
    @ResponseBody
    public Map<String, Object> ajustarStock(@RequestParam String name,
                                             @RequestParam String subCategory,
                                             @RequestParam int cantidad) {
        try {
            SubCategory sub = SubCategory.valueOf(subCategory.trim());
            // Read current stock for audit trail
            List<Product> matches = productService.findBynameAndSubCategoryForUpdate(name, sub);
            if (matches.isEmpty()) {
                return Map.of("success", false, "message", "Producto no encontrado");
            }
            Product p = matches.get(0);
            int oldStock = p.getStock();
            // Atomic SQL update — no race condition
            productService.adjustStock(name.trim(), sub, cantidad);
            int nuevoStock = Math.max(0, oldStock + cantidad);

            Movement m = new Movement();
            m.setProductName(p.getName());
            m.setProductSubCategory(sub.name());
            m.setAction(cantidad > 0 ? "STOCK_IN" : "STOCK_OUT");
            m.setOldStock(oldStock);
            m.setNewStock(nuevoStock);
            m.setOldPrice(p.getPrice());
            m.setNewPrice(p.getPrice());
            m.setTimestamp(LocalDateTime.now());
            movementService.save(m);

            return Map.of("success", true, "nuevoStock", nuevoStock);
        } catch (Exception e) {
            return Map.of("success", false, "message", "Error: " + e.getMessage());
        }
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

    @GetMapping("/dashboard")
    public String dashboard(
            @RequestParam(defaultValue = "") String name,
            @RequestParam(defaultValue = "") String category,
            @RequestParam(defaultValue = "") String subCategory,
            Model model) {
        try {
            // SQL aggregation — no Java loop over all products
            Map<String, Object> metrics = productService.dashboardMetrics(name, category, subCategory);
            List<Object[]> top20 = productService.top20Products(name, category, subCategory);
            List<Object[]> catProfit = productService.profitByCategory(name, category, subCategory);
            List<Object[]> lineaProfit = productService.profitByLinea(name, category, subCategory);
            int totalProductos = productService.countFiltered(name, category, subCategory, "", false);

            // Aggregate metrics
            double totalInversion = (double) metrics.getOrDefault("totalInversion", 0.0);
            double totalVenta = (double) metrics.getOrDefault("totalVenta", 0.0);
            double totalGanancia = (double) metrics.getOrDefault("totalGanancia", 0.0);
            int countConCosto = (int) metrics.getOrDefault("countConCosto", 0);
            double margenPromedio = (double) metrics.getOrDefault("margenPromedio", 0.0);

            // Top 20 chart
            List<String> labels = new ArrayList<>();
            List<Double> ganancias = new ArrayList<>();
            List<Double> costos = new ArrayList<>();
            List<Double> ventas = new ArrayList<>();
            List<Double> margenes = new ArrayList<>();
            for (Object[] row : top20) {
                labels.add((String) row[0]);
                ganancias.add((Double) row[1]);
                costos.add((Double) row[2]);
                ventas.add((Double) row[3]);
                margenes.add((Double) row[4]);
            }

            // Category profit
            List<String> catLabels = new ArrayList<>();
            List<Double> catGanancias = new ArrayList<>();
            for (Object[] row : catProfit) {
                catLabels.add((String) row[0]);
                catGanancias.add((Double) row[1]);
            }

            // Linea profit
            List<String> lineaLabels = new ArrayList<>();
            List<Double> lineaGanancias = new ArrayList<>();
            for (Object[] row : lineaProfit) {
                lineaLabels.add((String) row[0]);
                lineaGanancias.add((Double) row[1]);
            }

            // Best product from top 20
            String mejorProducto = top20.isEmpty() ? "\u2014" : (String) top20.get(0)[0];

            model.addAttribute("labels",       labels);
            model.addAttribute("ganancias",    ganancias);
            model.addAttribute("costos",       costos);
            model.addAttribute("ventas",       ventas);
            model.addAttribute("margenes",     margenes);
            model.addAttribute("catLabels",    catLabels);
            model.addAttribute("catGanancias", catGanancias);
            model.addAttribute("lineaLabels",  lineaLabels);
            model.addAttribute("lineaGanancias", lineaGanancias);
            model.addAttribute("totalInversion", totalInversion);
            model.addAttribute("totalVenta", totalVenta);
            model.addAttribute("totalGanancia", totalGanancia);
            model.addAttribute("margenPromedio", margenPromedio);
            model.addAttribute("totalProductos", totalProductos);
            model.addAttribute("mejorProducto", mejorProducto);
            model.addAttribute("filtroName",     name);
            model.addAttribute("filtroCategory", category);
            model.addAttribute("filtroSub",      subCategory);
            model.addAttribute("Categorys",      ProductCategory.values());
            model.addAttribute("SubCategorys",   SubCategory.values());
            model.addAttribute("productos",      productService.getAllFiltered(name, category, subCategory, ""));

        } catch (Exception e) {
            model.addAttribute("error", "Error al cargar dashboard: " + e.getMessage());
            model.addAttribute("Categorys",    ProductCategory.values());
            model.addAttribute("SubCategorys", SubCategory.values());
        }
        return "dashboard";
    }

    @GetMapping("/detalle/{name}/{subCategory}")
    public String detalle(@PathVariable String name,
                          @PathVariable String subCategory,
                          Model model) {
        try {
            SubCategory sub = SubCategory.valueOf(subCategory);
            List<Product> matches = productService.findBynameAndSubCategoryForUpdate(name, sub);
            if (!matches.isEmpty()) {
                Product p = matches.get(0);
                model.addAttribute("product", p);
                model.addAttribute("descripcion", generarDescripcion(p));
                model.addAttribute("whatsappNum", whatsappNumber);
                String msg = "Hola! Quiero comprar " + p.getName() + " de $" + String.format("%.2f", p.getPrice());
                model.addAttribute("whatsappUrl", "https://wa.me/" + whatsappNumber + "?text=" + java.net.URLEncoder.encode(msg, java.nio.charset.StandardCharsets.UTF_8));
                List<Product> relacionados = productService.findRelated(p, 4);
                model.addAttribute("relacionados", relacionados);
            } else {
                model.addAttribute("error", "Producto no encontrado");
            }
        } catch (Exception e) {
            model.addAttribute("error", "Error: " + e.getMessage());
        }
        return "detalle";
    }

    private String generarDescripcion(Product p) {
        String sub = p.getSubCategory() != null ? p.getSubCategory().name() : "";
        String cat = p.getCategory() != null ? p.getCategory().name() : "";

        String desc = switch (sub) {
            case "PERFUME" ->
                "Fragancia \"" + p.getName() + "\" de " + cat + ". " +
                "Ideal para regalar o para uso personal.";
            case "MAQUILLAJE" ->
                p.getName() + " — Maquillaje " + cat + ". " +
                "Perfecto para resaltar tu belleza.";
            case "CABELLO" ->
                p.getName() + " — Cuidado capilar " + cat + ". " +
                "Producto de calidad para el cabello.";
            case "CREMA" ->
                p.getName() + " — Crema " + cat + ". " +
                "Nutre e hidrata tu piel.";
            case "CUIDADO_DIARIO" ->
                p.getName() + " — Cuidado diario " + cat + ". " +
                "Ideal para tu rutina diaria.";
            case "TEXTIL" ->
                p.getName() + " — Textil " + cat + ". " +
                "Perfecto para el cuidado de tus prendas.";
            case "AEROSOL" ->
                p.getName() + " — Aerosol " + cat + ". " +
                "Frescura en cada uso.";
            case "DIFUSOR" ->
                p.getName() + " — Difusor " + cat + ". " +
                "Ambientá tu hogar con esta fragancia.";
            case "JABON" ->
                p.getName() + " — Jabón " + cat + ". " +
                "Suavidad y frescura para tu piel.";
            case "PERFUME_PISO" ->
                p.getName() + " — Perfume para piso " + cat + ". " +
                "Dejá tu hogar con un aroma increíble.";
            case "SAHUMERIO" ->
                p.getName() + " — Sahumerio " + cat + ". " +
                "Aromas naturales para tu espacio.";
            default ->
                p.getName() + " — Producto " + cat + ". " +
                (sub.isEmpty() ? "" : "Subcategoría: " + sub.replace("_", " ") + ". ");
        };
        return desc.trim();
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

    @PostMapping("/compras/crear")
    @ResponseBody
    public Map<String, Object> crearOrden(@RequestParam String itemsJson, @RequestParam double total) {
        try {
            Orden o = ordenService.crear(itemsJson, total);
            return Map.of("success", true, "id", o.getId());
        } catch (Exception e) {
            return Map.of("success", false, "message", e.getMessage());
        }
    }

    @PostMapping("/compras/{id}/completar")
    public String completarOrden(@PathVariable Long id, RedirectAttributes ra) {
        try {
            ordenService.completar(id);
            ra.addFlashAttribute("mensaje", "Orden completada y stock actualizado");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Error al completar: " + e.getMessage());
        }
        return "redirect:/productos/compras";
    }

    @PostMapping("/compras/{id}/cancelar")
    public String cancelarOrden(@PathVariable Long id, RedirectAttributes ra) {
        try {
            ordenService.cancelar(id);
            ra.addFlashAttribute("mensaje", "Orden cancelada");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Error al cancelar: " + e.getMessage());
        }
        return "redirect:/productos/compras";
    }

    @GetMapping("/compras")
    public String compras(Model model) {
        List<Orden> ordenes = ordenService.listar();
        model.addAttribute("ordenes", ordenes);
        return "compras";
    }

}
