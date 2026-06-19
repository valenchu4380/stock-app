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
    return new Product(
        rs.getString("name"),
        rs.getDouble("price"),
        rs.getInt("stock"),
        ProductCategory.valueOf(rs.getString("category"))
    );
}

    @Override
    public void save(Product product) {
        String sql = "INSERT INTO products(name, price, stock, Category) VALUES(?, ?, ?, ?)";
        try (Connection con = getConnection();
             PreparedStatement st = con.prepareStatement(sql)) {

            st.setString(1, product.getName());
            st.setDouble(2, product.getPrice());
            st.setInt(3, product.getStock());
            st.setString(4, product.getCategory().name());
            st.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void delete(String name) throws ProductNotFoundException {
        String sql = "DELETE FROM products WHERE name = ?";
        try (Connection con = getConnection();
             PreparedStatement st = con.prepareStatement(sql)) {

            st.setString(1, name);
            int rows = st.executeUpdate();
            if (rows == 0) {
                throw new ProductNotFoundException("Producto no encontrado: " + name);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void update(Product product) throws ProductNotFoundException {
        String sql = "UPDATE products SET price=?, stock=?, Category=? WHERE name=?";
        try (Connection con = getConnection();
             PreparedStatement st = con.prepareStatement(sql)) {

            st.setDouble(1, product.getPrice());
            st.setInt(2, product.getStock());
            st.setString(3, product.getCategory().name());
            st.setString(4, product.getName());

            int rows = st.executeUpdate();
            if (rows == 0) {
                throw new ProductNotFoundException("Producto no encontrado: " + product.getName());
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
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
        // En un entorno real, usa un Logger: log.error("Error al actualizar prices", e);
        System.out.println("Error en SQL: " + e.getMessage());
    }
}
}
