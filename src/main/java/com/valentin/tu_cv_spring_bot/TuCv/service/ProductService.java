package com.valentin.tu_cv_spring_bot.TuCv.service;

import java.util.List;
import java.util.Optional;

import com.valentin.tu_cv_spring_bot.TuCv.Exception.InvalidProductException;
import com.valentin.tu_cv_spring_bot.TuCv.Exception.ProductNotFoundException;
import com.valentin.tu_cv_spring_bot.TuCv.mODEL.Product;
import com.valentin.tu_cv_spring_bot.TuCv.mODEL.ProductCategory;
import com.valentin.tu_cv_spring_bot.TuCv.mODEL.SubCategory;


public interface ProductService {
    List<Product> getAll() throws InvalidProductException;
    Optional<Product> getByname(String name);
    void save(Product product) throws InvalidProductException;
    boolean actualizarpricesPorCategoria(ProductCategory categoria, double porcentaje) throws ProductNotFoundException;
    void delete(String name, SubCategory subCategory) throws ProductNotFoundException;
    void update(Product product, String oldName, SubCategory oldSubCategory)
            throws ProductNotFoundException, InvalidProductException;
List<Product> getAllPaged(int page, int size, String name, String category, String subCategory) throws InvalidProductException;
int getTotalPages(int size, String name, String category, String subCategory);
int countFiltered(String name, String category, String subCategory);

int getStockTotal();
    double getInventarioTotal();
    int getSinStockCount();
}
