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
        return products;
    }

    @Override
    public Optional<Product> findByname(String name) {
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

        String catStr = rs.getString("category");
        String subCatStr = rs.getString("subcategory");

        ProductCategory category = (catStr != null) ? ProductCategory.valueOf(catStr) : null;
        SubCategory subCategory = (subCatStr != null) ? SubCategory.valueOf(subCatStr) : null;

        return new Product(name, price, stock, category, subCategory);
    }

    @Override
    public void save(Product product) {
        String sql = "INSERT INTO products(name, price, stock, category, subcategory) VALUES(?, ?, ?, ?, ?)";
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
    String sql = "UPDATE products SET name=?, price=?, stock=?, category=?, subCategory=? WHERE name=? AND subCategory=?";
    try (Connection con = getConnection();
         PreparedStatement st = con.prepareStatement(sql)) {
        st.setString(1, product.getName().trim());
        st.setDouble(2, product.getPrice());
        st.setInt(3, product.getStock());
        st.setString(4, product.getCategory().name());
        st.setString(5, product.getSubCategory().name());
        st.setString(6, oldName.trim());
        st.setString(7, oldSubCategory.name());
        int rows = st.executeUpdate(); // ← FALTABA ESTO
        if (rows == 0) throw new ProductNotFoundException("Producto no encontrado: " + oldName);
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

        double factor = 1.0 + (porcentaje / 100.0);
        String sql = "UPDATE products SET price = price * ? WHERE Category = ?";

        try (Connection con = getConnection();
                PreparedStatement st = con.prepareStatement(sql)) {

            st.setDouble(1, factor);
            st.setString(2, Category.name());
            st.executeUpdate();

        } catch (SQLException e) {
           
            System.out.println("Error en SQL: " + e.getMessage());
        }
    }

    @Override
public List<Product> findAllPaged(int offset, int limit) throws InvalidProductException {
    List<Product> products = new ArrayList<>();
    String sql = "SELECT * FROM products ORDER BY name LIMIT ? OFFSET ?";
    try (Connection con = getConnection();
         PreparedStatement st = con.prepareStatement(sql)) {
        st.setInt(1, limit);
        st.setInt(2, offset);
        ResultSet rs = st.executeQuery();
        while (rs.next()) {
            products.add(mapResult(rs));
        }
    } catch (SQLException e) {
        System.out.println(e.getMessage());
    }
    return products;
}

@Override
public int countAll() {
    String sql = "SELECT COUNT(*) FROM products";
    try (Connection con = getConnection();
         PreparedStatement st = con.prepareStatement(sql);
         ResultSet rs = st.executeQuery()) {
        if (rs.next()) return rs.getInt(1);
    } catch (SQLException e) {
        System.out.println(e.getMessage());
    }
    return 0;
}

@Override
public List<Product> findAllPagedFiltered(int offset, int limit, String name, String category, String subCategory) throws InvalidProductException {
    List<Product> products = new ArrayList<>();
    StringBuilder sql = new StringBuilder("SELECT * FROM products WHERE 1=1");
    List<Object> params = new ArrayList<>();

    if (name != null && !name.isBlank()) {
        sql.append(" AND LOWER(name) LIKE ?");
        params.add("%" + name.trim().toLowerCase() + "%");
    }
    if (category != null && !category.isBlank() && !category.equals("TODAS")) {
        sql.append(" AND category = ?");
        params.add(category);
    }
    if (subCategory != null && !subCategory.isBlank() && !subCategory.equals("TODAS")) {
        sql.append(" AND subcategory = ?");
        params.add(subCategory);
    }

    sql.append(" ORDER BY name LIMIT ? OFFSET ?");
    params.add(limit);
    params.add(offset);

    try (Connection con = getConnection();
         PreparedStatement st = con.prepareStatement(sql.toString())) {
        for (int i = 0; i < params.size(); i++) {
            st.setObject(i + 1, params.get(i));
        }
        ResultSet rs = st.executeQuery();
        while (rs.next()) {
            products.add(mapResult(rs));
        }
    } catch (SQLException e) {
        System.out.println(e.getMessage());
    }
    return products;
}

@Override
public int countFiltered(String name, String category, String subCategory) {
    StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM products WHERE 1=1");
    List<Object> params = new ArrayList<>();

    if (name != null && !name.isBlank()) {
        sql.append(" AND LOWER(name) LIKE ?");
        params.add("%" + name.trim().toLowerCase() + "%");
    }
    if (category != null && !category.isBlank() && !category.equals("TODAS")) {
        sql.append(" AND category = ?");
        params.add(category);
    }
    if (subCategory != null && !subCategory.isBlank() && !subCategory.equals("TODAS")) {
        sql.append(" AND subcategory = ?");
        params.add(subCategory);
    }

    try (Connection con = getConnection();
         PreparedStatement st = con.prepareStatement(sql.toString())) {
        for (int i = 0; i < params.size(); i++) {
            st.setObject(i + 1, params.get(i));
        }
        ResultSet rs = st.executeQuery();
        if (rs.next()) return rs.getInt(1);
    } catch (SQLException e) {
        System.out.println(e.getMessage());
    }
    return 0;
}

@Override
public int sumStock() {
    String sql = "SELECT COALESCE(SUM(stock), 0) FROM products";
    try (Connection con = getConnection();
         PreparedStatement st = con.prepareStatement(sql);
         ResultSet rs = st.executeQuery()) {
        if (rs.next()) return rs.getInt(1);
    } catch (SQLException e) {
        System.out.println(e.getMessage());
    }
    return 0;
}

@Override
public double sumInventario() {
    String sql = "SELECT COALESCE(SUM(price * stock), 0) FROM products";
    try (Connection con = getConnection();
         PreparedStatement st = con.prepareStatement(sql);
         ResultSet rs = st.executeQuery()) {
        if (rs.next()) return rs.getDouble(1);
    } catch (SQLException e) {
        System.out.println(e.getMessage());
    }
    return 0.0;
}

@Override
public int countSinStock() {
    String sql = "SELECT COUNT(*) FROM products WHERE stock = 0";
    try (Connection con = getConnection();
         PreparedStatement st = con.prepareStatement(sql);
         ResultSet rs = st.executeQuery()) {
        if (rs.next()) return rs.getInt(1);
    } catch (SQLException e) {
        System.out.println(e.getMessage());
    }
    return 0;
}

}
