package com.valentin.tu_cv_spring_bot.TuCv.conroller;

import com.valentin.tu_cv_spring_bot.TuCv.Exception.InvalidProductException;
import com.valentin.tu_cv_spring_bot.TuCv.mODEL.Linea;
import com.valentin.tu_cv_spring_bot.TuCv.mODEL.LineaCost;
import com.valentin.tu_cv_spring_bot.TuCv.mODEL.Movement;
import com.valentin.tu_cv_spring_bot.TuCv.mODEL.Orden;
import com.valentin.tu_cv_spring_bot.TuCv.mODEL.Product;
import com.valentin.tu_cv_spring_bot.TuCv.mODEL.ProductCategory;
import com.valentin.tu_cv_spring_bot.TuCv.mODEL.SubCategory;
import com.valentin.tu_cv_spring_bot.TuCv.service.LineaDetectionService;
import com.valentin.tu_cv_spring_bot.TuCv.service.MovementService;
import com.valentin.tu_cv_spring_bot.TuCv.service.OrdenService;
import com.valentin.tu_cv_spring_bot.TuCv.service.ProductService;

import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
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
    private final LineaDetectionService lineaDetectionService;
    private final OrdenService ordenService;

    @Value("${whatsapp.number:543854202134}")
    private String whatsappNumber;

    @GetMapping
    public String index(
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
            int totalPages     = productService.getTotalPages(size, name, category, subCategory, linea, stockBajo);
            int totalRegistros = productService.countFiltered(name, category, subCategory, linea, stockBajo);
            int totalStock     = productService.sumStock(name, category, subCategory, linea, stockBajo);
            double inventario  = productService.sumInventario(name, category, subCategory, linea, stockBajo);
            int sinStock       = productService.countSinStock(name, category, subCategory, linea, stockBajo);
            int stockBajoCount = productService.countStockBajo(name, category, subCategory, linea);

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
            model.addAttribute("filtroLinea",    linea);
            model.addAttribute("sortBy",         sortBy);
            model.addAttribute("sortDir",        sortDir);
            model.addAttribute("sortDirNext", "asc".equals(sortDir) ? "desc" : "asc");
            model.addAttribute("stockBajo",      stockBajo);

            int pageStart = page;
            int pageEnd = Math.min(page + 4, totalPages - 1);
            if (pageEnd - pageStart < 4) {
                pageStart = Math.max(0, pageEnd - 4);
            }
            model.addAttribute("pageStart", pageStart);
            model.addAttribute("pageEnd", pageEnd);
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
            model.addAttribute("filtroLinea",    "");
            model.addAttribute("sortBy",         "name");
            model.addAttribute("sortDir",        "asc");
            model.addAttribute("sortDirNext",    "desc");
            model.addAttribute("stockBajo",      false);
            model.addAttribute("pageStart",      0);
            model.addAttribute("pageEnd",        0);
        }
        model.addAttribute("Categorys",    ProductCategory.values());
        model.addAttribute("SubCategorys", SubCategory.values());
        model.addAttribute("lineas", productService.findAllLineas());
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
        model.addAttribute("lineas", productService.findAllLineas());
        model.addAttribute("allLineas", Linea.values());
        return "form";
    }

    private void autoDetectarLinea(Product product) {
        if (product.getLinea() == null) {
            Linea detected = lineaDetectionService.detectarLinea(
                product.getName(),
                product.getCategory() != null ? product.getCategory().name() : null,
                product.getSubCategory() != null ? product.getSubCategory().name() : null
            );
            product.setLinea(detected);
        }
    }

    @PostMapping("/nuevo")
    public String guardar(@ModelAttribute Product product,
            RedirectAttributes ra) {
        try {
            autoDetectarLinea(product);
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
            productService.getByname(name).ifPresent(p ->
                model.addAttribute("product", p));
        }
        model.addAttribute("Categorys", ProductCategory.values());
        model.addAttribute("SubCategorys", SubCategory.values());
        model.addAttribute("lineas", productService.findAllLineas());
        model.addAttribute("allLineas", Linea.values());
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

            autoDetectarLinea(product);
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
            List<Product> matches = productService.findBynameAndSubCategoryForUpdate(name, sub);
            if (matches.isEmpty()) {
                return Map.of("success", false, "message", "Producto no encontrado");
            }
            Product p = matches.get(0);
            int nuevoStock = Math.max(0, p.getStock() + cantidad);
            productService.updateFields(name, sub, null, null, nuevoStock);

            Movement m = new Movement();
            m.setProductName(p.getName());
            m.setProductSubCategory(sub.name());
            m.setAction(cantidad > 0 ? "STOCK_IN" : "STOCK_OUT");
            m.setOldStock(p.getStock());
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

    @GetMapping("/lineas")
    public String lineas(Model model) {
        List<LineaCost> lineas = productService.getLineaCosts();
        model.addAttribute("lineas", lineas);
        return "lineas";
    }

    @PostMapping("/lineas/actualizar-costo")
    public String actualizarCostoLinea(@RequestParam String linea, @RequestParam double costPrice, RedirectAttributes ra) {
        try {
            productService.updateLineaCost(linea, costPrice);
            ra.addFlashAttribute("mensaje", "Costo actualizado para la linea: " + linea);
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Error: " + e.getMessage());
        }
        return "redirect:/productos/lineas";
    }

    @PostMapping("/asignar-lineas-pendientes")
    @ResponseBody
    public Map<String, Object> asignarLineasPendientes() {
        int asignados = 0;
        try {
            List<Product> todos = productService.getAll();
            for (Product p : todos) {
                if (p.getLinea() == null) {
                    Linea detected = lineaDetectionService.detectarLinea(
                        p.getName(),
                        p.getCategory() != null ? p.getCategory().name() : null,
                        p.getSubCategory() != null ? p.getSubCategory().name() : null
                    );
                    if (detected != null) {
                        p.setLinea(detected);
                        try {
                            productService.update(p, p.getName(), p.getSubCategory());
                            asignados++;
                        } catch (Exception e) {
                            // skip
                        }
                    }
                }
            }
        } catch (Exception e) {
            // skip
        }
        return Map.of("asignados", asignados);
    }

    @GetMapping("/detectar-linea")
    @ResponseBody
    public Map<String, String> detectarLinea(
            @RequestParam String name,
            @RequestParam(defaultValue = "") String category,
            @RequestParam(defaultValue = "") String subCategory) {
        Linea linea = lineaDetectionService.detectarLinea(name, category, subCategory);
        return Map.of("linea", linea != null ? linea.getDisplayName() : "");
    }

    @GetMapping("/lineas-por-categoria")
    @ResponseBody
    public Map<String, List<String>> lineasPorCategoria(
            @RequestParam(defaultValue = "") String category,
            @RequestParam(defaultValue = "") String subCategory) {
        List<Linea> lineas = lineaDetectionService.getLineasPorCategoriaYSub(category, subCategory);
        List<String> names = lineas.stream().map(Linea::name).toList();
        return Map.of("lineas", names);
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
            List<Product> todos = productService.getAllFiltered(name, category, subCategory, "");
            double totalInversion = 0;
            double totalVenta = 0;
            double totalGanancia = 0;
            int countConCosto = 0;

            java.util.Map<String, Double> gananciaPorCat = new java.util.HashMap<>();
            java.util.Map<String, Double> gananciaPorSub = new java.util.HashMap<>();
            java.util.List<Product> conDatos = new java.util.ArrayList<>();

            for (Product p : todos) {
                double inversion = p.getCostPrice() * p.getStock();
                double venta = p.getPrice() * p.getStock();
                double ganancia = venta - inversion;
                totalInversion += inversion;
                totalVenta += venta;
                totalGanancia += ganancia;

                String cat = p.getCategory() != null ? p.getCategory().name() : "SIN CAT";
                gananciaPorCat.merge(cat, ganancia, Double::sum);

                String sub = p.getSubCategory() != null ? p.getSubCategory().name() : "SIN SUB";
                gananciaPorSub.merge(sub, ganancia, Double::sum);

                if (p.getCostPrice() > 0) countConCosto++;

                if (p.getStock() > 0) conDatos.add(p);
            }

            double margenPromedio = 0;
            if (!todos.isEmpty() && countConCosto > 0) {
                double sumaMargenes = 0;
                for (Product p : todos) {
                    if (p.getPrice() > 0 && p.getCostPrice() > 0) {
                        sumaMargenes += ((p.getPrice() - p.getCostPrice()) / p.getPrice()) * 100;
                    }
                }
                margenPromedio = sumaMargenes / countConCosto;
            }

            conDatos.sort((a, b) -> {
                double gA = (a.getPrice() - a.getCostPrice()) * a.getStock();
                double gB = (b.getPrice() - b.getCostPrice()) * b.getStock();
                return Double.compare(gB, gA);
            });

            java.util.List<String> labels = new java.util.ArrayList<>();
            java.util.List<Double> ganancias = new java.util.ArrayList<>();
            java.util.List<Double> costos = new java.util.ArrayList<>();
            java.util.List<Double> ventas = new java.util.ArrayList<>();
            java.util.List<Double> margenes = new java.util.ArrayList<>();

            int limit = Math.min(conDatos.size(), 20);
            for (int i = 0; i < limit; i++) {
                Product p = conDatos.get(i);
                labels.add(p.getName().length() > 20 ? p.getName().substring(0, 20) + "…" : p.getName());
                double g = (p.getPrice() - p.getCostPrice()) * p.getStock();
                ganancias.add(g);
                costos.add(p.getCostPrice() * p.getStock());
                ventas.add(p.getPrice() * p.getStock());
                if (p.getPrice() > 0 && p.getCostPrice() > 0) {
                    margenes.add(((p.getPrice() - p.getCostPrice()) / p.getPrice()) * 100);
                } else {
                    margenes.add(0.0);
                }
            }

            java.util.List<String> catLabels = new java.util.ArrayList<>(gananciaPorCat.keySet());
            java.util.List<Double> catGanancias = new java.util.ArrayList<>();
            for (String cl : catLabels) catGanancias.add(gananciaPorCat.get(cl));

            java.util.Map<String, Double> gananciaPorLinea = new java.util.HashMap<>();
            java.util.Map<String, Double> inversionPorLinea = new java.util.HashMap<>();
            for (Product p : todos) {
                String linea = p.getLinea() != null ? p.getLinea().getDisplayName() : "SIN LINEA";
                double inversion = p.getCostPrice() * p.getStock();
                double venta = p.getPrice() * p.getStock();
                gananciaPorLinea.merge(linea, venta - inversion, Double::sum);
                inversionPorLinea.merge(linea, inversion, Double::sum);
            }
            java.util.List<String> lineaLabels = new java.util.ArrayList<>(gananciaPorLinea.keySet());
            lineaLabels.sort((a, b) -> Double.compare(gananciaPorLinea.get(b), gananciaPorLinea.get(a)));
            java.util.List<Double> lineaGanancias = new java.util.ArrayList<>();
            for (String bl : lineaLabels) lineaGanancias.add(gananciaPorLinea.get(bl));

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
            model.addAttribute("totalProductos", todos.size());
            model.addAttribute("mejorProducto", conDatos.isEmpty() ? "—" : conDatos.get(0).getName());
            model.addAttribute("filtroName",     name);
            model.addAttribute("filtroCategory", category);
            model.addAttribute("filtroSub",      subCategory);
            model.addAttribute("Categorys",      ProductCategory.values());
            model.addAttribute("SubCategorys",   SubCategory.values());
            model.addAttribute("productos",      todos);

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
        String linea = p.getLinea() != null ? p.getLinea().getDisplayName() : "";
        String cat = p.getCategory() != null ? p.getCategory().name() : "";

        String desc = switch (sub) {
            case "PERFUME" ->
                "Fragancia \"" + p.getName() + "\" de " + cat + ". " +
                (linea.isEmpty() ? "" : "Pertenece a la línea " + linea + ". ") +
                "Ideal para regalar o para uso personal.";
            case "MAQUILLAJE" ->
                p.getName() + " — Maquillaje " + cat + ". " +
                (linea.isEmpty() ? "" : "De la línea " + linea + ". ") +
                "Perfecto para resaltar tu belleza.";
            case "CABELLO" ->
                p.getName() + " — Cuidado capilar " + cat + ". " +
                (linea.isEmpty() ? "" : "Línea " + linea + ". ") +
                "Producto de calidad para el cabello.";
            case "CREMA" ->
                p.getName() + " — Crema " + cat + ". " +
                (linea.isEmpty() ? "" : "Línea " + linea + ". ") +
                "Nutre e hidrata tu piel.";
            case "CUIDADO_DIARIO" ->
                p.getName() + " — Cuidado diario " + cat + ". " +
                (linea.isEmpty() ? "" : "Línea " + linea + ". ") +
                "Ideal para tu rutina diaria.";
            case "TEXTIL" ->
                p.getName() + " — Textil " + cat + ". " +
                (linea.isEmpty() ? "" : "Línea " + linea + ". ") +
                "Perfecto para el cuidado de tus prendas.";
            case "AEROSOL" ->
                p.getName() + " — Aerosol " + cat + ". " +
                (linea.isEmpty() ? "" : "Línea " + linea + ". ") +
                "Frescura en cada uso.";
            case "DIFUSOR" ->
                p.getName() + " — Difusor " + cat + ". " +
                (linea.isEmpty() ? "" : "Línea " + linea + ". ") +
                "Ambientá tu hogar con esta fragancia.";
            case "JABON" ->
                p.getName() + " — Jabón " + cat + ". " +
                (linea.isEmpty() ? "" : "Línea " + linea + ". ") +
                "Suavidad y frescura para tu piel.";
            case "PERFUME_PISO" ->
                p.getName() + " — Perfume para piso " + cat + ". " +
                (linea.isEmpty() ? "" : "Línea " + linea + ". ") +
                "Dejá tu hogar con un aroma increíble.";
            case "SAHUMERIO" ->
                p.getName() + " — Sahumerio " + cat + ". " +
                (linea.isEmpty() ? "" : "Línea " + linea + ". ") +
                "Aromas naturales para tu espacio.";
            default ->
                p.getName() + " — Producto " + cat + ". " +
                (linea.isEmpty() ? "" : "Línea " + linea + ". ") +
                (sub.isEmpty() ? "" : "Subcategoría: " + sub.replace("_", " ") + ". ");
        };
        return desc.trim();
    }

    @GetMapping("/carrito")
    public String carrito(Model model) {
        model.addAttribute("whatsappNum", whatsappNumber);
        return "carrito";
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
