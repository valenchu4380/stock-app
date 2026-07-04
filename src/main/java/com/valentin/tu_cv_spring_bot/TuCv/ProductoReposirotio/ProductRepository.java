package com.valentin.tu_cv_spring_bot.TuCv.ProductoReposirotio;

import java.util.List;
import java.util.Optional;

import com.valentin.tu_cv_spring_bot.TuCv.Exception.InvalidProductException;
import com.valentin.tu_cv_spring_bot.TuCv.Exception.ProductNotFoundException;
import com.valentin.tu_cv_spring_bot.TuCv.mODEL.Linea;
import com.valentin.tu_cv_spring_bot.TuCv.mODEL.LineaCost;
import com.valentin.tu_cv_spring_bot.TuCv.mODEL.Product;
import com.valentin.tu_cv_spring_bot.TuCv.mODEL.ProductCategory;
import com.valentin.tu_cv_spring_bot.TuCv.mODEL.SubCategory;

public interface ProductRepository {
    List<Product> findAll() throws InvalidProductException;
    List<Product> findAllFiltered(String name, String category, String subCategory, String linea) throws InvalidProductException;
    List<Linea> findAllLineas();
    void updateLineaCost(String linea, double costPrice);
    List<LineaCost> getLineaCosts();

    Optional<Product> findByname(String name);

    void save(Product product);

    void actualizarpricePorCategory(ProductCategory Category, double porcentaje);
    void actualizarpricePorSubCategoria(SubCategory subCategory, double porcentaje);
    List<Product> findRelated(Product product, int limit);

    List<Product> findBynameAndSubCategoryForUpdate(String name, SubCategory subCategory);

    boolean existsByname(String name);

    boolean existsBynameAndSubCategory(String name, SubCategory subCategory);

    void updateFields(String name, SubCategory subCategory, Double newPrice, Double newCostPrice, Integer newStock);

    void delete(String name, SubCategory subCategory) throws ProductNotFoundException;

    void update(Product product, String oldName, SubCategory oldSubCategory) throws ProductNotFoundException;
    void reduceStock(String name, String subCategory, int cantidad);

    List<Product> findAllPaged(int offset, int limit) throws InvalidProductException;

    int countAll();

    List<Product> findAllPagedFiltered(int offset, int limit, String name, String category, String subCategory, String linea, String sortBy, String sortDir, boolean stockBajo) throws InvalidProductException;
int countFiltered(String name, String category, String subCategory, String linea, boolean stockBajo);
double sumInventario(String name, String category, String subCategory, String linea, boolean stockBajo);
int sumStock(String name, String category, String subCategory, String linea, boolean stockBajo);
int countSinStock(String name, String category, String subCategory, String linea, boolean stockBajo);
int countStockBajo(String name, String category, String subCategory, String linea);


}