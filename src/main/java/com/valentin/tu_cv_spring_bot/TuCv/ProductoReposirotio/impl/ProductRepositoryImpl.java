/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.valentin.tu_cv_spring_bot.TuCv.ProductoReposirotio.impl;

import com.valentin.tu_cv_spring_bot.TuCv.Exception.InvalidProductException;
import com.valentin.tu_cv_spring_bot.TuCv.Exception.ProductNotFoundException;
import com.valentin.tu_cv_spring_bot.TuCv.ProductoReposirotio.ProductRepository;
import com.valentin.tu_cv_spring_bot.TuCv.mODEL.Product;
import com.valentin.tu_cv_spring_bot.TuCv.mODEL.ProductCategory;
import com.valentin.tu_cv_spring_bot.TuCv.mODEL.SubCategory;

import java.util.List;

import org.springframework.stereotype.Repository;

/**
 *
 * @author User
 */
import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.Optional;

@Repository
public class ProductRepositoryImpl implements ProductRepository {

    // Spring Boot inyecta el DataSource automaticamente con los datos
    // de application.properties — no necesitas HikariCP manual
    private final DataSource dataSource;
    // Cambia esto temporalmente para ver si el error cambia:

    public ProductRepositoryImpl(DataSource dataSource) {
        if (dataSource == null) {
            System.out.println("¡ERROR: El DataSource es nulo!");
        }
        this.dataSource = dataSource;
    }

    private Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    @Override
    public List<Product> findAll() throws InvalidProductException {
        System.out.println("Entrando a findAll()");
        List<Product> products = new ArrayList<>();
        String sql = "SELECT * FROM products";

        try (Connection con = getConnection();
                PreparedStatement st = con.prepareStatement(sql);
                ResultSet rs = st.executeQuery()) {

            while (rs.next()) {
                products.add(mapResult(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        System.out.println("Productos encontrados: " + products.size());
        // Ya no tiramos excepción si está vacía
        return products;
    }

    @Override
    public Optional<Product> findByname(String name) {
        // LOWER() en ambos lados para que coincida sin importar mayúsculas
        String sql = "SELECT * FROM products WHERE LOWER(name) = LOWER(?)";
        try (Connection con = getConnection();
                PreparedStatement st = con.prepareStatement(sql)) {

            st.setString(1, name.trim().toLowerCase());
            ResultSet rs = st.executeQuery();
            if (rs.next()) {
                return Optional.of(mapResult(rs));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return Optional.empty();
    }

    private Product mapResult(ResultSet rs) throws SQLException {
        String name = rs.getString("name");
        double price = rs.getDouble("price");
        int stock = rs.getInt("stock");

        // Obtener valores como String
        String catStr = rs.getString("category");
        String subCatStr = rs.getString("subCategory");

        // Convertir con seguridad (si es nulo, asignamos null al Enum)
        ProductCategory category = (catStr != null) ? ProductCategory.valueOf(catStr) : null;
        SubCategory subCategory = (subCatStr != null) ? SubCategory.valueOf(subCatStr) : null;

        return new Product(name, price, stock, category, subCategory);
    }

    @Override
    public void save(Product product) {
        String sql = "INSERT INTO products(name, price, stock, Category, subCategory ) VALUES(?, ?, ?, ?, ?)";
        try (Connection con = getConnection();
                PreparedStatement st = con.prepareStatement(sql)) {
            st.setString(1, product.getName().trim());
            st.setDouble(2, product.getPrice());
            st.setInt(3, product.getStock());
            st.setString(4, product.getCategory().name());
            st.setString(5, product.getSubCategory().name());
            st.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void delete(String name, SubCategory subCategory) throws ProductNotFoundException {
        String sql = "DELETE FROM products WHERE name = ? AND subCategory = ?";
        try (Connection con = getConnection();
                PreparedStatement st = con.prepareStatement(sql)) {

            st.setString(1, name);
            st.setString(2, subCategory.name());
            int rows = st.executeUpdate();
            if (rows == 0) {
                throw new ProductNotFoundException("Producto no encontrado: " + name);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

@Override
public void update(Product product, String oldName, SubCategory oldSubCategory) throws ProductNotFoundException {
    // 1. Contamos los ? : 1(name), 2(price), 3(stock), 4(category), 5(subCategory)
    // 6(WHERE name), 7(WHERE subCategory)
    String sql = "UPDATE products SET name = ?, price = ?, stock = ?, category = ?, subCategory = ? WHERE name = ? AND subCategory = ?";
                 
    try (Connection con = getConnection();
         PreparedStatement st = con.prepareStatement(sql)) {

        // SET (5 parámetros)
        st.setString(1, product.getName().trim());
        st.setDouble(2, product.getPrice());
        st.setInt(3, product.getStock());
        st.setString(4, product.getCategory().name());
        st.setString(5, product.getSubCategory().name());
        
        // WHERE (2 parámetros)
        st.setString(6, oldName.trim());
        st.setString(7, oldSubCategory.name()); // Aquí debe ir el 7

        int rows = st.executeUpdate();
        // ... resto del código
    } catch (SQLException e) {
        System.out.println("Error SQL: " + e.getMessage());
    }
}

    @Override
    public boolean existsByname(String name) {
        String sql = "SELECT COUNT(*) FROM products WHERE LOWER(name) = LOWER(?)";
        try (Connection con = getConnection();
                PreparedStatement st = con.prepareStatement(sql)) {

            st.setString(1, name.trim().toLowerCase());
            ResultSet rs = st.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return false;
    }

    @Override
    public boolean existsBynameAndSubCategory(String name, SubCategory subCategory) {
        // Buscamos si existe la combinación exacta de nombre + subcategoría
        String sql = "SELECT COUNT(*) FROM products WHERE LOWER(name) = LOWER(?) AND subCategory = ?";
        try (Connection con = getConnection();
                PreparedStatement st = con.prepareStatement(sql)) {

            st.setString(1, name.trim().toLowerCase());
            st.setString(2, subCategory.name()); // Convertimos el Enum a String

            ResultSet rs = st.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.out.println("Error en existsBynameAndSubCategory: " + e.getMessage());
        }
        return false;
    }

    @Override
    public void actualizarpricePorCategory(ProductCategory Category, double porcentaje) {

        // Calculamos el multiplicador (ej: 1.10 para un aumento del 10%)
        double factor = 1.0 + (porcentaje / 100.0);
        String sql = "UPDATE products SET price = price * ? WHERE Category = ?";

        try (Connection con = getConnection();
                PreparedStatement st = con.prepareStatement(sql)) {

            st.setDouble(1, factor);
            st.setString(2, Category.name());
            st.executeUpdate();

        } catch (SQLException e) {
            // En un entorno real, usa un Logger: log.error("Error al actualizar prices",
            // e);
            System.out.println("Error en SQL: " + e.getMessage());
        }
    }
}
