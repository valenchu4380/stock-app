package com.valentin.tu_cv_spring_bot.TuCv.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.valentin.tu_cv_spring_bot.TuCv.Exception.InvalidProductException;
import com.valentin.tu_cv_spring_bot.TuCv.Exception.ProductNotFoundException;
import com.valentin.tu_cv_spring_bot.TuCv.ProductoReposirotio.ProductRepository;
import com.valentin.tu_cv_spring_bot.TuCv.mODEL.Product;
import com.valentin.tu_cv_spring_bot.TuCv.mODEL.ProductCategory;
import com.valentin.tu_cv_spring_bot.TuCv.mODEL.SubCategory;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    @Override
    public List<Product> getAll() throws InvalidProductException {
        return productRepository.findAll();
    }

    @Override
    public Optional<Product> getByname(String name) {
        return productRepository.findByNameIgnoreCase(name);
    }

 @Override
public void save(Product product) throws InvalidProductException {
    ProductValidator.validate(product);

    boolean existe = productRepository.existsByNameAndSubCategory(
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
        if (!productRepository.existsByNameAndSubCategory(name, subCategory)) {
            throw new ProductNotFoundException("Producto no encontrado: " + name);
        }
        productRepository.deleteByNameAndSubCategory(name, subCategory);
    }

@Override
public void update(Product product, String oldName, SubCategory oldSubCategory) 
        throws ProductNotFoundException, InvalidProductException {
    
    ProductValidator.validate(product);

    if (!productRepository.existsByNameAndSubCategory(oldName, oldSubCategory)) {
        throw new ProductNotFoundException("Producto no encontrado: " + oldName + " en " + oldSubCategory);
    }
    productRepository.update(product, oldName, oldSubCategory);
}
    @Override
    public boolean actualizarpricesPorCategoria(ProductCategory categoria, double porcentaje) throws ProductNotFoundException {

        if (categoria == null) {
            throw new ProductNotFoundException("Categoría inválida");
        }
        return true;
    }



@Override
public List<Product> getAllPaged(int page, int size, String name, String category, String subCategory) {
    PageRequest pageable = PageRequest.of(page, size);
    return productRepository.findByNameContainingIgnoreCaseAndCategoryAndSubCategory(
        name, category, subCategory, pageable).getContent(); // .getContent() convierte Page a List
}

@Override
public int getTotalPages(int size, String name, String category, String subCategory) {
    ProductCategory catEnum = (category == null || category.equals("TODAS")) ? null : ProductCategory.valueOf(category);
    SubCategory subCatEnum = (subCategory == null || subCategory.equals("TODAS")) ? null : SubCategory.valueOf(subCategory);

    long total = productRepository.countByFilters(name, catEnum, subCatEnum);
    
    if (size <= 0) return 1; 
    return (int) Math.ceil((double) total / size);
}

@Override
public int countFiltered(String name, String category, String subCategory) {
    ProductCategory catEnum = (category == null || category.equals("TODAS")) ? null : ProductCategory.valueOf(category);
    SubCategory subCatEnum = (subCategory == null || subCategory.equals("TODAS")) ? null : SubCategory.valueOf(subCategory);
    
    return (int) productRepository.countByFilters(name, catEnum, subCatEnum);
}

@Override
    public int getStockTotal() {
        return productRepository.sumarStockTotal() != null ? productRepository.sumarStockTotal() : 0;
    }

    @Override
    public double getInventarioTotal() {
        return productRepository.calcularValorTotalInventario() != null ? productRepository.calcularValorTotalInventario() : 0.0;
    }

    @Override
    public int getSinStockCount() {
        return productRepository.contarProductosSinStock();
    }
}
