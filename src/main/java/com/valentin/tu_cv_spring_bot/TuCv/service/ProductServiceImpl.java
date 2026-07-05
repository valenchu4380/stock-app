package com.valentin.tu_cv_spring_bot.TuCv.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.valentin.tu_cv_spring_bot.TuCv.Exception.InvalidProductException;
import com.valentin.tu_cv_spring_bot.TuCv.Exception.ProductNotFoundException;
import com.valentin.tu_cv_spring_bot.TuCv.ProductoReposirotio.ProductRepository;
import com.valentin.tu_cv_spring_bot.TuCv.mODEL.Product;
import com.valentin.tu_cv_spring_bot.TuCv.mODEL.ProductCategory;
import com.valentin.tu_cv_spring_bot.TuCv.mODEL.SubCategory;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private static final Logger log = LoggerFactory.getLogger(ProductServiceImpl.class);

    private final ProductRepository productRepository;

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
        if (!productRepository.existsBynameAndSubCategory(name, subCategory)) {
            throw new ProductNotFoundException("Producto no encontrado: " + name + " en " + subCategory);
        }
        productRepository.delete(name, subCategory);
    }

    @Override
    public void update(Product product, String oldName, SubCategory oldSubCategory)
            throws ProductNotFoundException, InvalidProductException {

        ProductValidator.validate(product);

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
    public List<Product> findRelated(Product product, int limit) {
        return productRepository.findRelated(product, limit);
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
    public void batchUpdateFields(List<String> items, Double price, Double costPrice, Integer stock) throws InvalidProductException {
        for (String item : items) {
            String[] parts = item.split("\\|", 2);
            if (parts.length != 2) continue;
            String name = parts[0].trim();
            try {
                SubCategory subCategory = SubCategory.valueOf(parts[1].trim());
                productRepository.updateFields(name, subCategory, price, costPrice, stock);
            } catch (IllegalArgumentException e) {
                log.warn("Subcategoría inválida en batch: {}", parts[1]);
            }
        }
    }

    @Override
    public void updateFields(String name, SubCategory subCategory, Double price, Double costPrice, Integer stock) throws InvalidProductException {
        productRepository.updateFields(name, subCategory, price, costPrice, stock);
    }

    @Override
    public void adjustStock(String name, SubCategory subCategory, int cantidad) {
        productRepository.adjustStock(name, subCategory, cantidad);
    }

    @Override
    public Map<String, Object> dashboardMetrics(String name, String category, String subCategory) {
        return productRepository.dashboardMetrics(name, category, subCategory);
    }

    @Override
    public List<Object[]> top20Products(String name, String category, String subCategory) {
        return productRepository.top20Products(name, category, subCategory);
    }

    @Override
    public List<Object[]> profitByCategory(String name, String category, String subCategory) {
        return productRepository.profitByCategory(name, category, subCategory);
    }

    @Override
    public List<Object[]> profitByLinea(String name, String category, String subCategory) {
        return productRepository.profitByLinea(name, category, subCategory);
    }

}
