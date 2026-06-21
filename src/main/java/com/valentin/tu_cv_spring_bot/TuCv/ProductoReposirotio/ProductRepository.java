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

    void actualizarpricePorCategory(ProductCategory Category, double porcentaje);

    boolean existsByname(String name);

    boolean existsBynameAndSubCategory(String name, SubCategory subCategory);

    void delete(String name, SubCategory subCategory) throws ProductNotFoundException;

    void update(Product product, String oldName, SubCategory oldSubCategory) throws ProductNotFoundException;

    List<Product> findAllPaged(int offset, int limit) throws InvalidProductException;

    int countAll();

    List<Product> findAllPagedFiltered(int offset, int limit, String name, String category, String subCategory) throws InvalidProductException;
int countFiltered(String name, String category, String subCategory);

}