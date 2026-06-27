/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.valentin.tu_cv_spring_bot.TuCv.ProductoReposirotio.impl;

import com.valentin.tu_cv_spring_bot.TuCv.Exception.InvalidProductException;
import com.valentin.tu_cv_spring_bot.TuCv.Exception.ProductNotFoundException;
import com.valentin.tu_cv_spring_bot.TuCv.ProductoReposirotio.ProductRepository;
import com.valentin.tu_cv_spring_bot.TuCv.mODEL.LineaCost;
import com.valentin.tu_cv_spring_bot.TuCv.mODEL.Product;
import com.valentin.tu_cv_spring_bot.TuCv.mODEL.ProductCategory;
import com.valentin.tu_cv_spring_bot.TuCv.mODEL.SubCategory;

import java.util.Arrays;
import java.util.List;

import com.valentin.tu_cv_spring_bot.TuCv.mODEL.Linea;

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
        migrarColumnaCostPrice();
    }

    private void migrarColumnaCostPrice() {
        try (Connection con = dataSource.getConnection();
             Statement st = con.createStatement()) {
            st.execute("ALTER TABLE products ADD COLUMN IF NOT EXISTS cost_price DOUBLE PRECISION DEFAULT 0");
            st.execute("ALTER TABLE products ADD COLUMN IF NOT EXISTS linea VARCHAR(255) DEFAULT ''");
            st.execute("ALTER TABLE products ADD COLUMN IF NOT EXISTS imagen VARCHAR(500) DEFAULT ''");
        } catch (SQLException e) {
            System.out.println("Nota: " + e.getMessage());
        }
    }

    @Override
    public List<Linea> findAllLineas() {
        return Arrays.asList(Linea.values());
    }

    @Override
    public void updateLineaCost(String linea, double costPrice) {
        String sql = "UPDATE products SET cost_price = ? WHERE linea = ?";
        try (Connection con = getConnection();
             PreparedStatement st = con.prepareStatement(sql)) {
            st.setDouble(1, costPrice);
            st.setString(2, linea);
            st.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public List<LineaCost> getLineaCosts() {
        List<LineaCost> list = new ArrayList<>();
        String sql = "SELECT linea, cost_price, COUNT(*) as cnt FROM products WHERE linea IS NOT NULL AND linea != '' GROUP BY linea, cost_price ORDER BY linea";
        try (Connection con = getConnection();
             PreparedStatement st = con.prepareStatement(sql);
             ResultSet rs = st.executeQuery()) {
            while (rs.next()) {
                String lineaStr = rs.getString("linea");
                Linea lineaEnum = null;
                if (lineaStr != null && !lineaStr.isBlank()) {
                    try { lineaEnum = Linea.valueOf(lineaStr); } catch (IllegalArgumentException e) { lineaEnum = null; }
                }
                list.add(new LineaCost(lineaEnum, rs.getDouble("cost_price"), rs.getInt("cnt")));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return list;
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
        double costPrice = rs.getDouble("cost_price");
        int stock = rs.getInt("stock");

        String catStr = rs.getString("category");
        String subCatStr = rs.getString("subcategory");
        String linea = rs.getString("linea");

        ProductCategory category = (catStr != null) ? ProductCategory.valueOf(catStr) : null;
        SubCategory subCategory = (subCatStr != null) ? SubCategory.valueOf(subCatStr) : null;

        Linea lineaEnum = null;
        if (linea != null && !linea.isBlank()) {
            try { lineaEnum = Linea.valueOf(linea); } catch (IllegalArgumentException e) { lineaEnum = null; }
        }
        String imagen = rs.getString("imagen");
        if (imagen == null) imagen = "";
        return new Product(name, price, costPrice, stock, category, subCategory, lineaEnum, imagen);
    }

    @Override
    public void save(Product product) {
        String sql = "INSERT INTO products(name, price, cost_price, stock, category, subcategory, linea, imagen) VALUES(?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection con = getConnection();
                PreparedStatement st = con.prepareStatement(sql)) {
            st.setString(1, product.getName().trim());
            st.setDouble(2, product.getPrice());
            st.setDouble(3, product.getCostPrice());
            st.setInt(4, product.getStock());
            st.setString(5, product.getCategory().name());
            st.setString(6, product.getSubCategory().name());
            st.setString(7, product.getLinea() != null ? product.getLinea().name() : "");
            st.setString(8, product.getImagen() != null ? product.getImagen() : "");
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
    String sql = "UPDATE products SET name=?, price=?, cost_price=?, stock=?, category=?, subCategory=?, linea=?, imagen=? WHERE name=? AND subCategory=?";
    try (Connection con = getConnection();
         PreparedStatement st = con.prepareStatement(sql)) {
        st.setString(1, product.getName().trim());
        st.setDouble(2, product.getPrice());
        st.setDouble(3, product.getCostPrice());
        st.setInt(4, product.getStock());
        st.setString(5, product.getCategory().name());
        st.setString(6, product.getSubCategory().name());
        st.setString(7, product.getLinea() != null ? product.getLinea().name() : "");
        st.setString(8, product.getImagen() != null ? product.getImagen() : "");
        st.setString(9, oldName.trim());
        st.setString(10, oldSubCategory.name());
        int rows = st.executeUpdate();
        if (rows == 0) throw new ProductNotFoundException("Producto no encontrado: " + oldName);
    } catch (SQLException e) {
        System.out.println("Error SQL: " + e.getMessage());
    }
}

    @Override
    public void updateFields(String name, SubCategory subCategory, Double newPrice, Double newCostPrice, Integer newStock) {
        String sql = "UPDATE products SET price = COALESCE(?, price), cost_price = COALESCE(?, cost_price), stock = COALESCE(?, stock) WHERE name = ? AND subcategory = ?";
        try (Connection con = getConnection();
             PreparedStatement st = con.prepareStatement(sql)) {
            if (newPrice != null) st.setDouble(1, newPrice);
            else st.setNull(1, java.sql.Types.DOUBLE);
            if (newCostPrice != null) st.setDouble(2, newCostPrice);
            else st.setNull(2, java.sql.Types.DOUBLE);
            if (newStock != null) st.setInt(3, newStock);
            else st.setNull(3, java.sql.Types.INTEGER);
            st.setString(4, name.trim());
            st.setString(5, subCategory.name());
            st.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error batch update: " + e.getMessage());
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
    public List<Product> findAllFiltered(String name, String category, String subCategory, String linea) throws InvalidProductException {
        List<Product> products = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM products WHERE 1=1");
        List<Object> params = new ArrayList<>();
        if (name != null && !name.isBlank()) {
            sql.append(" AND LOWER(name) LIKE ?");
            params.add("%" + name.trim().toLowerCase() + "%");
        }
        if (category != null && !category.isBlank()) {
            sql.append(" AND category = ?");
            params.add(category);
        }
        if (subCategory != null && !subCategory.isBlank()) {
            sql.append(" AND subcategory = ?");
            params.add(subCategory);
        }
        if (linea != null && !linea.isBlank()) {
            sql.append(" AND LOWER(linea) = LOWER(?)");
            params.add(linea.trim().toLowerCase());
        }
        sql.append(" ORDER BY name");
        try (Connection con = getConnection();
             PreparedStatement st = con.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) st.setObject(i + 1, params.get(i));
            ResultSet rs = st.executeQuery();
            while (rs.next()) products.add(mapResult(rs));
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return products;
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
    public void actualizarpricePorSubCategoria(SubCategory subCategory, double porcentaje) {
        double factor = 1.0 + (porcentaje / 100.0);
        String sql = "UPDATE products SET price = price * ? WHERE subcategory = ?";
        try (Connection con = getConnection();
             PreparedStatement st = con.prepareStatement(sql)) {
            st.setDouble(1, factor);
            st.setString(2, subCategory.name());
            st.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error en SQL: " + e.getMessage());
        }
    }

    @Override
    public List<Product> findBynameAndSubCategoryForUpdate(String name, SubCategory subCategory) {
        List<Product> products = new ArrayList<>();
        String sql;
        if (name != null && !name.isBlank()) {
            sql = "SELECT * FROM products WHERE LOWER(name) = LOWER(?) AND subcategory = ?";
        } else {
            sql = "SELECT * FROM products WHERE subcategory = ?";
        }
        try (Connection con = getConnection();
             PreparedStatement st = con.prepareStatement(sql)) {
            if (name != null && !name.isBlank()) {
                st.setString(1, name.trim());
                st.setString(2, subCategory.name());
            } else {
                st.setString(1, subCategory.name());
            }
            ResultSet rs = st.executeQuery();
            while (rs.next()) {
                products.add(mapResult(rs));
            }
        } catch (SQLException e) {
            System.out.println("Error en SQL: " + e.getMessage());
        }
        return products;
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
    public List<Product> findAllPagedFiltered(int offset, int limit, String name, String category, String subCategory, String linea, String sortBy, String sortDir, boolean stockBajo) throws InvalidProductException {
    List<Product> products = new ArrayList<>();
    StringBuilder sql = new StringBuilder("SELECT * FROM products WHERE 1=1");
    List<Object> params = new ArrayList<>();

    if (name != null && !name.isBlank()) {
        sql.append(" AND LOWER(name) LIKE ?");
        params.add("%" + name.trim().toLowerCase() + "%");
    }
    if (category != null && !category.isBlank()) {
        sql.append(" AND category = ?");
        params.add(category);
    }
    if (subCategory != null && !subCategory.isBlank()) {
        sql.append(" AND subcategory = ?");
        params.add(subCategory);
    }
    if (linea != null && !linea.isBlank()) {
        sql.append(" AND LOWER(linea) = LOWER(?)");
        params.add(linea.trim().toLowerCase());
    }
    if (stockBajo) {
        sql.append(" AND stock >= 0 AND stock <= ?");
        params.add(1);
    }

    String col = switch (sortBy != null ? sortBy : "") {
        case "price" -> "price";
        case "stock" -> "stock";
        default      -> "name";
    };
    String dir = "desc".equalsIgnoreCase(sortDir) ? "DESC" : "ASC";
    sql.append(" ORDER BY ").append(col).append(" ").append(dir);
    sql.append(" LIMIT ? OFFSET ?");
    params.add(limit);
    params.add(offset);

    try (Connection con = getConnection();
         PreparedStatement st = con.prepareStatement(sql.toString())) {
        for (int i = 0; i < params.size(); i++) st.setObject(i + 1, params.get(i));
        ResultSet rs = st.executeQuery();
        while (rs.next()) products.add(mapResult(rs));
    } catch (SQLException e) {
        System.out.println(e.getMessage());
    }
    return products;
}
@Override
    public int countFiltered(String name, String category, String subCategory, String linea, boolean stockBajo) {
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
    if (linea != null && !linea.isBlank()) {
        sql.append(" AND LOWER(linea) = LOWER(?)");
        params.add(linea.trim().toLowerCase());
    }
    if (stockBajo) {
        sql.append(" AND stock >= 0 AND stock <= ?");
        params.add(1);
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
    public int countStockBajo(String name, String category, String subCategory, String linea) {
    StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM products WHERE stock >= 0 AND stock <= 1");
    List<Object> params = new ArrayList<>();
    if (name != null && !name.isBlank()) {
        sql.append(" AND LOWER(name) LIKE ?");
        params.add("%" + name.trim().toLowerCase() + "%");
    }
    if (category != null && !category.isBlank()) {
        sql.append(" AND category = ?");
        params.add(category);
    }
    if (subCategory != null && !subCategory.isBlank()) {
        sql.append(" AND subcategory = ?");
        params.add(subCategory);
    }
    if (linea != null && !linea.isBlank()) {
        sql.append(" AND LOWER(linea) = LOWER(?)");
        params.add(linea.trim().toLowerCase());
    }
    try (Connection con = getConnection();
         PreparedStatement st = con.prepareStatement(sql.toString())) {
        for (int i = 0; i < params.size(); i++) st.setObject(i + 1, params.get(i));
        ResultSet rs = st.executeQuery();
        if (rs.next()) return rs.getInt(1);
    } catch (SQLException e) {
        System.out.println(e.getMessage());
    }
    return 0;
}


@Override
    public double sumInventario(String name, String category, String subCategory, String linea, boolean stockBajo) {
    StringBuilder sql = new StringBuilder("SELECT COALESCE(SUM(price * stock), 0) FROM products WHERE 1=1");
    List<Object> params = new ArrayList<>();

    if (name != null && !name.isBlank()) {
        sql.append(" AND LOWER(name) LIKE ?");
        params.add("%" + name.trim().toLowerCase() + "%");
    }
    if (category != null && !category.isBlank()) {
        sql.append(" AND category = ?");
        params.add(category);
    }
    if (subCategory != null && !subCategory.isBlank()) {
        sql.append(" AND subcategory = ?");
        params.add(subCategory);
    }
    if (linea != null && !linea.isBlank()) {
        sql.append(" AND LOWER(linea) = LOWER(?)");
        params.add(linea.trim().toLowerCase());
    }
    if (stockBajo) {
        sql.append(" AND stock >= 0 AND stock <= ?");
        params.add(1);
    }

    try (Connection con = getConnection();
         PreparedStatement st = con.prepareStatement(sql.toString())) {
        for (int i = 0; i < params.size(); i++) st.setObject(i + 1, params.get(i));
        ResultSet rs = st.executeQuery();
        if (rs.next()) return rs.getDouble(1);
    } catch (SQLException e) {
        System.out.println(e.getMessage());
    }
    return 0.0;
}

@Override
    public int sumStock(String name, String category, String subCategory, String linea, boolean stockBajo) {
    StringBuilder sql = new StringBuilder("SELECT COALESCE(SUM(stock), 0) FROM products WHERE 1=1");
    List<Object> params = new ArrayList<>();

    if (name != null && !name.isBlank()) {
        sql.append(" AND LOWER(name) LIKE ?");
        params.add("%" + name.trim().toLowerCase() + "%");
    }
    if (category != null && !category.isBlank()) {
        sql.append(" AND category = ?");
        params.add(category);
    }
    if (subCategory != null && !subCategory.isBlank()) {
        sql.append(" AND subcategory = ?");
        params.add(subCategory);
    }
    if (linea != null && !linea.isBlank()) {
        sql.append(" AND LOWER(linea) = LOWER(?)");
        params.add(linea.trim().toLowerCase());
    }
    if (stockBajo) {
        sql.append(" AND stock >= 0 AND stock <= ?");
        params.add(1);
    }

    try (Connection con = getConnection();
         PreparedStatement st = con.prepareStatement(sql.toString())) {
        for (int i = 0; i < params.size(); i++) st.setObject(i + 1, params.get(i));
        ResultSet rs = st.executeQuery();
        if (rs.next()) return rs.getInt(1);
    } catch (SQLException e) {
        System.out.println(e.getMessage());
    }
    return 0;
}

@Override
    public int countSinStock(String name, String category, String subCategory, String linea, boolean stockBajo) {
    StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM products WHERE stock = 0");
    List<Object> params = new ArrayList<>();

    if (name != null && !name.isBlank()) {
        sql.append(" AND LOWER(name) LIKE ?");
        params.add("%" + name.trim().toLowerCase() + "%");
    }
    if (category != null && !category.isBlank()) {
        sql.append(" AND category = ?");
        params.add(category);
    }
    if (subCategory != null && !subCategory.isBlank()) {
        sql.append(" AND subcategory = ?");
        params.add(subCategory);
    }
    if (linea != null && !linea.isBlank()) {
        sql.append(" AND LOWER(linea) = LOWER(?)");
        params.add(linea.trim().toLowerCase());
    }
    if (stockBajo) {
        sql.append(" AND stock >= 0 AND stock <= ?");
        params.add(1);
    }

    try (Connection con = getConnection();
         PreparedStatement st = con.prepareStatement(sql.toString())) {
        for (int i = 0; i < params.size(); i++) st.setObject(i + 1, params.get(i));
        ResultSet rs = st.executeQuery();
        if (rs.next()) return rs.getInt(1);
    } catch (SQLException e) {
        System.out.println(e.getMessage());
    }
    return 0;
}



}
