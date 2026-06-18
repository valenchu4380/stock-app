package com.valentin.tu_cv_spring_bot.TuCv.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.valentin.tu_cv_spring_bot.TuCv.Exception.InvalidProductException;
import com.valentin.tu_cv_spring_bot.TuCv.Exception.ProductNotFoundException;
import com.valentin.tu_cv_spring_bot.TuCv.ProductoReposirotio.ProductRepository;
import com.valentin.tu_cv_spring_bot.TuCv.mODEL.Product;
import com.valentin.tu_cv_spring_bot.TuCv.mODEL.ProductCategory;

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
        if (productRepository.existsByname(product.getName())) {
            throw new InvalidProductException("Ya existe un producto con ese name");
        }
        productRepository.save(product);
    }

    @Override
    public void delete(String name) throws ProductNotFoundException {
        if (!productRepository.existsByname(name)) {
            throw new ProductNotFoundException("Producto no encontrado: " + name);
        }
        productRepository.delete(name);
    }

    @Override
    public void update(Product product) throws ProductNotFoundException, InvalidProductException {
        ProductValidator.validate(product);
        if (!productRepository.existsByname(product.getName())) {
            throw new ProductNotFoundException("Producto no encontrado: " + product.getName());
        }
        productRepository.update(product);
    }

    @Override
    public boolean actualizarpricesPorCategoria(ProductCategory categoria, double porcentaje) throws ProductNotFoundException {
        // Lo implementás directo en el repo con SQL
        // Por ahora lanza excepcion si la categoria es null
        if (categoria == null) {
            throw new ProductNotFoundException("Categoría inválida");
        }
        // Delegamos al repo (ver paso siguiente)
        return true;
    }
}
