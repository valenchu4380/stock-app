package com.valentin.tu_cv_spring_bot.TuCv.ProductoReposirotio;

import java.util.List;
import java.util.Optional;

import com.valentin.tu_cv_spring_bot.TuCv.Exception.InvalidProductException;
import com.valentin.tu_cv_spring_bot.TuCv.Exception.ProductNotFoundException;
import com.valentin.tu_cv_spring_bot.TuCv.mODEL.Product;
import com.valentin.tu_cv_spring_bot.TuCv.mODEL.ProductCategory;
import com.valentin.tu_cv_spring_bot.TuCv.mODEL.SubCategory;

import jakarta.transaction.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Modifying;

public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findByNameIgnoreCase(String name);

    @Modifying
    @Transactional
    @Query("UPDATE Product p SET p.price = p.price * (1 + :porcentaje / 100.0) WHERE p.category = :category")
    void actualizarPrecioPorCategoria(ProductCategory category, double porcentaje);

    boolean existsByNameAndSubCategory(String name, SubCategory subCategory);

    

    @Modifying
    @Transactional
    @Query("DELETE FROM Product p WHERE p.name = :name AND p.subCategory = :subSubCategory")
    void deleteByNameAndSubCategory(String name, SubCategory subSubCategory);

    void update(Product product, String oldName, SubCategory oldSubCategory) throws ProductNotFoundException;

    // Este método reemplaza a findAllPaged y findAllPagedFiltered
    Page<Product> findByNameContainingIgnoreCaseAndCategoryAndSubCategory(
            String name, String category, String subCategory, Pageable pageable);

    @Query("SELECT SUM(p.stock) FROM Product p")
    Integer sumarStockTotal();

    @Query("SELECT SUM(p.price * p.stock) FROM Product p")
    Double calcularValorTotalInventario();

    @Query("SELECT COUNT(p) FROM Product p WHERE p.stock = 0")
    int contarProductosSinStock();

    @Query("SELECT COUNT(p) FROM Product p WHERE " +
       "(:name IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
       "(:category IS NULL OR p.category = :category) AND " +
       "(:subCategory IS NULL OR p.subCategory = :subCategory)")
long countByFilters(@Param("name") String name, 
                    @Param("category") ProductCategory category, 
                    @Param("subCategory") SubCategory subCategory);
}