package com.valentin.tu_cv_spring_bot.TuCv.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.valentin.tu_cv_spring_bot.TuCv.Exception.InvalidProductException;
import com.valentin.tu_cv_spring_bot.TuCv.Exception.ProductNotFoundException;
import com.valentin.tu_cv_spring_bot.TuCv.mODEL.Product;
import com.valentin.tu_cv_spring_bot.TuCv.mODEL.ProductCategory;
import com.valentin.tu_cv_spring_bot.TuCv.mODEL.SubCategory;


public interface ProductService {
    List<Product> getAll() throws InvalidProductException;
    List<Product> getAllFiltered(String name, String category, String subCategory, String linea) throws InvalidProductException;
    Optional<Product> getByname(String name);
    void save(Product product) throws InvalidProductException;
    boolean actualizarpricesPorCategoria(ProductCategory categoria, double porcentaje) throws ProductNotFoundException;
    void actualizarpricesPorSubCategoria(SubCategory subCategory, double porcentaje) throws ProductNotFoundException;
    List<Product> findRelated(Product product, int limit);

    List<Product> findBynameAndSubCategoryForUpdate(String name, SubCategory subCategory);
    void delete(String name, SubCategory subCategory) throws ProductNotFoundException;
    void update(Product product, String oldName, SubCategory oldSubCategory)
            throws ProductNotFoundException, InvalidProductException;
    List<Product> getAllPaged(int page, int size, String name, String category, String subCategory, String linea, String sortBy, String sortDir, boolean stockBajo) throws InvalidProductException;
    int getTotalPages(int size, String name, String category, String subCategory, String linea, boolean stockBajo);
    int countFiltered(String name, String category, String subCategory, String linea, boolean stockBajo);

    double sumInventario(String name, String category, String subCategory, String linea, boolean stockBajo);
    int sumStock(String name, String category, String subCategory, String linea, boolean stockBajo);
    int countSinStock(String name, String category, String subCategory, String linea, boolean stockBajo);
    int countStockBajo(String name, String category, String subCategory, String linea);
    void batchUpdateFields(List<String> items, Double price, Double costPrice, Integer stock) throws InvalidProductException;
    void updateFields(String name, SubCategory subCategory, Double price, Double costPrice, Integer stock) throws InvalidProductException;

    void adjustStock(String name, SubCategory subCategory, int cantidad);

    Map<String, Object> dashboardMetrics(String name, String category, String subCategory);
    List<Object[]> top20Products(String name, String category, String subCategory);
    List<Object[]> profitByCategory(String name, String category, String subCategory);
    List<Object[]> profitByLinea(String name, String category, String subCategory);
}
