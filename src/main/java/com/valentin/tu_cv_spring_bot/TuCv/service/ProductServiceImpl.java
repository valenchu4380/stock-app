package com.valentin.tu_cv_spring_bot.TuCv.service;

import java.util.List;
import java.util.Optional;

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
        if (!productRepository.existsByname(name)) {
            throw new ProductNotFoundException("Producto no encontrado: " + name);
        }
        productRepository.delete(name,subCategory);
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
    public boolean actualizarpricesPorCategoria(ProductCategory categoria, double porcentaje) throws ProductNotFoundException {

        if (categoria == null) {
            throw new ProductNotFoundException("Categoría inválida");
        }
        return true;
    }



@Override
public List<Product> getAllPaged(int page, int size, String name, String category, String subCategory) throws InvalidProductException {
    int offset = page * size;
    return productRepository.findAllPagedFiltered(offset, size, name, category, subCategory);
}

@Override
public int getTotalPages(int size, String name, String category, String subCategory) {
    int total = productRepository.countFiltered(name, category, subCategory);
    return (int) Math.ceil((double) total / size);
}
@Override
public int countFiltered(String name, String category, String subCategory) {
    return productRepository.countFiltered(name, category, subCategory);
}

@Override
public int sumStock() { return productRepository.sumStock(); }

@Override
public double sumInventario() { return productRepository.sumInventario(); }

@Override
public int countSinStock() { return productRepository.countSinStock(); }
}
