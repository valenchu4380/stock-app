package com.valentin.tu_cv_spring_bot.TuCv.ProductoReposirotio.impl;

import com.valentin.tu_cv_spring_bot.TuCv.ProductoReposirotio.MovementRepository;
import com.valentin.tu_cv_spring_bot.TuCv.mODEL.Movement;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Repository;
import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Repository
public class MovementRepositoryImpl implements MovementRepository {

    private final DataSource dataSource;

    public MovementRepositoryImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @PostConstruct
    public void init() {
        String sql = """
            CREATE TABLE IF NOT EXISTS movements (
                id SERIAL PRIMARY KEY,
                product_name VARCHAR(255),
                product_sub_category VARCHAR(100),
                action VARCHAR(50),
                old_price DOUBLE PRECISION,
                new_price DOUBLE PRECISION,
                old_stock INTEGER,
                new_stock INTEGER,
                timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
        """;
        try (Connection con = dataSource.getConnection();
             Statement st = con.createStatement()) {
            st.execute(sql);
        } catch (SQLException e) {
            System.out.println("Error creating movements table: " + e.getMessage());
        }
    }

    private Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    @Override
    public void save(Movement movement) {
        String sql = "INSERT INTO movements (product_name, product_sub_category, action, old_price, new_price, old_stock, new_stock, timestamp) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection con = getConnection();
             PreparedStatement st = con.prepareStatement(sql)) {
            st.setString(1, movement.getProductName());
            st.setString(2, movement.getProductSubCategory());
            st.setString(3, movement.getAction());
            if (movement.getOldPrice() != null) st.setDouble(4, movement.getOldPrice());
            else st.setNull(4, Types.DOUBLE);
            if (movement.getNewPrice() != null) st.setDouble(5, movement.getNewPrice());
            else st.setNull(5, Types.DOUBLE);
            if (movement.getOldStock() != null) st.setInt(6, movement.getOldStock());
            else st.setNull(6, Types.INTEGER);
            if (movement.getNewStock() != null) st.setInt(7, movement.getNewStock());
            else st.setNull(7, Types.INTEGER);
            st.setTimestamp(8, Timestamp.valueOf(movement.getTimestamp() != null ? movement.getTimestamp() : LocalDateTime.now()));
            st.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error saving movement: " + e.getMessage());
        }
    }

    @Override
    public List<Movement> findAllPaged(int offset, int limit) {
        List<Movement> movements = new ArrayList<>();
        String sql = "SELECT * FROM movements ORDER BY timestamp DESC LIMIT ? OFFSET ?";
        try (Connection con = getConnection();
             PreparedStatement st = con.prepareStatement(sql)) {
            st.setInt(1, limit);
            st.setInt(2, offset);
            ResultSet rs = st.executeQuery();
            while (rs.next()) {
                movements.add(mapResult(rs));
            }
        } catch (SQLException e) {
            System.out.println("Error finding movements: " + e.getMessage());
        }
        return movements;
    }

    @Override
    public List<Movement> findByProductName(String productName, String subCategory) {
        List<Movement> movements = new ArrayList<>();
        String sql = "SELECT * FROM movements WHERE LOWER(product_name) = LOWER(?) AND product_sub_category = ? ORDER BY timestamp DESC LIMIT 50";
        try (Connection con = getConnection();
             PreparedStatement st = con.prepareStatement(sql)) {
            st.setString(1, productName.trim());
            st.setString(2, subCategory);
            ResultSet rs = st.executeQuery();
            while (rs.next()) movements.add(mapResult(rs));
        } catch (SQLException e) {
            System.out.println("Error finding movements by product: " + e.getMessage());
        }
        return movements;
    }

    @Override
    public int countAll() {
        String sql = "SELECT COUNT(*) FROM movements";
        try (Connection con = getConnection();
             PreparedStatement st = con.prepareStatement(sql);
             ResultSet rs = st.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.out.println("Error counting movements: " + e.getMessage());
        }
        return 0;
    }

    private Movement mapResult(ResultSet rs) throws SQLException {
        Movement m = new Movement();
        m.setId(rs.getLong("id"));
        m.setProductName(rs.getString("product_name"));
        m.setProductSubCategory(rs.getString("product_sub_category"));
        m.setAction(rs.getString("action"));
        double oldP = rs.getDouble("old_price");
        m.setOldPrice(rs.wasNull() ? null : oldP);
        double newP = rs.getDouble("new_price");
        m.setNewPrice(rs.wasNull() ? null : newP);
        int oldS = rs.getInt("old_stock");
        m.setOldStock(rs.wasNull() ? null : oldS);
        int newS = rs.getInt("new_stock");
        m.setNewStock(rs.wasNull() ? null : newS);
        Timestamp ts = rs.getTimestamp("timestamp");
        m.setTimestamp(ts != null ? ts.toLocalDateTime() : null);
        return m;
    }
}
