package com.valentin.tu_cv_spring_bot.TuCv.service;

import java.util.List;
import java.util.Optional;

import com.valentin.tu_cv_spring_bot.TuCv.Exception.InvalidProductException;
import com.valentin.tu_cv_spring_bot.TuCv.Exception.ProductNotFoundException;
import com.valentin.tu_cv_spring_bot.TuCv.mODEL.Product;
import com.valentin.tu_cv_spring_bot.TuCv.mODEL.ProductCategory;


public interface ProductService {
    List<Product> getAll() throws InvalidProductException;
    Optional<Product> getByname(String name);
    void save(Product product) throws InvalidProductException;
    void delete(String name) throws ProductNotFoundException;
    void update(Product product) throws ProductNotFoundException, InvalidProductException;
    boolean actualizarpricesPorCategoria(ProductCategory categoria, double porcentaje) throws ProductNotFoundException;

}
