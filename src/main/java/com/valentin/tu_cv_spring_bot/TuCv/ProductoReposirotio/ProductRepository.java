package com.valentin.tu_cv_spring_bot.TuCv.ProductoReposirotio;

import java.util.List;
import java.util.Optional;

import com.valentin.tu_cv_spring_bot.TuCv.Exception.InvalidProductException;
import com.valentin.tu_cv_spring_bot.TuCv.Exception.ProductNotFoundException;
import com.valentin.tu_cv_spring_bot.TuCv.mODEL.Product;
import com.valentin.tu_cv_spring_bot.TuCv.mODEL.ProductCategory;
import com.valentin.tu_cv_spring_bot.TuCv.mODEL.SubCategory;

public interface ProductRepository {
    List<Product> findAll() throws InvalidProductException;
    Optional<Product> findByname(String name);
    void save(Product product);
    void delete(String name) throws ProductNotFoundException;
   void update(Product product,String oldName) throws ProductNotFoundException;
    void actualizarpricePorCategory(ProductCategory Category, double porcentaje);
    boolean existsByname(String name);
    boolean existsBynameAndSubCategory(String name, SubCategory subCategory);
}