package com.valentin.tu_cv_spring_bot.TuCv.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;

import com.valentin.tu_cv_spring_bot.TuCv.Exception.InvalidProductException;
import com.valentin.tu_cv_spring_bot.TuCv.Exception.ProductNotFoundException;
import com.valentin.tu_cv_spring_bot.TuCv.ProductoReposirotio.ProductRepository;
import com.valentin.tu_cv_spring_bot.TuCv.mODEL.Linea;
import com.valentin.tu_cv_spring_bot.TuCv.mODEL.LineaCost;
import com.valentin.tu_cv_spring_bot.TuCv.mODEL.Product;
import com.valentin.tu_cv_spring_bot.TuCv.mODEL.ProductCategory;
import com.valentin.tu_cv_spring_bot.TuCv.mODEL.SubCategory;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final LineaDetectionService lineaDetectionService;

    @PostConstruct
    public void asignarLineasExistentes() {
        try {
            List<Product> todos = productRepository.findAll();
            boolean hayCambios = false;
            for (Product p : todos) {
                if (p.getLinea() == null) {
                    Linea detected = lineaDetectionService.detectarLinea(
                        p.getName(),
                        p.getCategory() != null ? p.getCategory().name() : null,
                        p.getSubCategory() != null ? p.getSubCategory().name() : null
                    );
                    if (detected != null) {
                        p.setLinea(detected);
                        productRepository.update(p, p.getName(), p.getSubCategory());
                        hayCambios = true;
                    }
                }
            }
            if (hayCambios) {
                System.out.println("Líneas asignadas automáticamente a productos existentes.");
            }
        } catch (Exception e) {
            System.out.println("Error al asignar líneas: " + e.getMessage());
        }
    }

    @Override
    public List<Product> getAll() throws InvalidProductException {
        return productRepository.findAll();
    }

    @Override
    public Optional<Product> getByname(String name) {
        return productRepository.findByname(name);
    }

  @Override
 public void save(Product product) throws InvalidProductException {
     ProductValidator.validate(product);

     if (product.getLinea() == null) {
         Linea detected = lineaDetectionService.detectarLinea(
             product.getName(),
             product.getCategory() != null ? product.getCategory().name() : null,
             product.getSubCategory() != null ? product.getSubCategory().name() : null
         );
         product.setLinea(detected);
     }

     boolean existe = productRepository.existsBynameAndSubCategory(
         product.getName(), 
         product.getSubCategory()
     );

     if (existe) {
         throw new InvalidProductException("Ya existe un producto con el nombre '" + 
             product.getName() + "' en la subcategoría " + product.getSubCategory());
     }

     productRepository.save(product);
 }

    @Override
    public void delete(String name, SubCategory subCategory) throws ProductNotFoundException {
        if (!productRepository.existsByname(name)) {
            throw new ProductNotFoundException("Producto no encontrado: " + name);
        }
        productRepository.delete(name,subCategory);
    }

@Override
public void update(Product product, String oldName, SubCategory oldSubCategory) 
        throws ProductNotFoundException, InvalidProductException {
    
    ProductValidator.validate(product);

    if (product.getLinea() == null) {
        Linea detected = lineaDetectionService.detectarLinea(
            product.getName(),
            product.getCategory() != null ? product.getCategory().name() : null,
            product.getSubCategory() != null ? product.getSubCategory().name() : null
        );
        product.setLinea(detected);
    }

    if (!productRepository.existsBynameAndSubCategory(oldName, oldSubCategory)) {
        throw new ProductNotFoundException("Producto no encontrado: " + oldName + " en " + oldSubCategory);
    }
    productRepository.update(product, oldName, oldSubCategory);
}
    @Override
    public List<Product> getAllFiltered(String name, String category, String subCategory, String linea) throws InvalidProductException {
        return productRepository.findAllFiltered(name, category, subCategory, linea);
    }

    @Override
    public boolean actualizarpricesPorCategoria(ProductCategory categoria, double porcentaje) throws ProductNotFoundException {
        if (categoria == null) {
            throw new ProductNotFoundException("Categoría inválida");
        }
        productRepository.actualizarpricePorCategory(categoria, porcentaje);
        return true;
    }

    @Override
    public void actualizarpricesPorSubCategoria(SubCategory subCategory, double porcentaje) throws ProductNotFoundException {
        if (subCategory == null) {
            throw new ProductNotFoundException("Subcategoría inválida");
        }
        productRepository.actualizarpricePorSubCategoria(subCategory, porcentaje);
    }

    @Override
    public List<Product> findBynameAndSubCategoryForUpdate(String name, SubCategory subCategory) {
        return productRepository.findBynameAndSubCategoryForUpdate(name, subCategory);
    }

@Override
public List<Product> getAllPaged(int page, int size, String name, String category, String subCategory, String linea, String sortBy, String sortDir, boolean stockBajo) throws InvalidProductException {
    int offset = page * size;
    return productRepository.findAllPagedFiltered(offset, size, name, category, subCategory, linea, sortBy, sortDir, stockBajo);
}

@Override
public int getTotalPages(int size, String name, String category, String subCategory, String linea, boolean stockBajo) {
    int total = productRepository.countFiltered(name, category, subCategory, linea, stockBajo);
    return (int) Math.ceil((double) total / size);
}
@Override
public int countFiltered(String name, String category, String subCategory, String linea, boolean stockBajo) {
    return productRepository.countFiltered(name, category, subCategory, linea, stockBajo);
}

@Override
public double sumInventario(String name, String category, String subCategory, String linea, boolean stockBajo) {
    return productRepository.sumInventario(name, category, subCategory, linea, stockBajo);
}

@Override
public int sumStock(String name, String category, String subCategory, String linea, boolean stockBajo) {
    return productRepository.sumStock(name, category, subCategory, linea, stockBajo);
}

@Override
public int countSinStock(String name, String category, String subCategory, String linea, boolean stockBajo) {
    return productRepository.countSinStock(name, category, subCategory, linea, stockBajo);
}

    @Override
    public int countStockBajo(String name, String category, String subCategory, String linea) {
        return productRepository.countStockBajo(name, category, subCategory, linea);
    }

    @Override
    public List<Linea> findAllLineas() {
        return productRepository.findAllLineas();
    }

    @Override
    public void updateLineaCost(String linea, double costPrice) {
        productRepository.updateLineaCost(linea, costPrice);
    }

    @Override
    public List<LineaCost> getLineaCosts() {
        return productRepository.getLineaCosts();
    }




}
