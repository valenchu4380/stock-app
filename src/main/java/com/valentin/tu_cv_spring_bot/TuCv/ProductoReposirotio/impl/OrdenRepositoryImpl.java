package com.valentin.tu_cv_spring_bot.TuCv.ProductoReposirotio.impl;

import com.valentin.tu_cv_spring_bot.TuCv.ProductoReposirotio.OrdenRepository;
import com.valentin.tu_cv_spring_bot.TuCv.mODEL.Orden;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Repository;
import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Repository
public class OrdenRepositoryImpl implements OrdenRepository {

    private final DataSource dataSource;

    public OrdenRepositoryImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @PostConstruct
    public void init() {
        String sql = """
            CREATE TABLE IF NOT EXISTS ordenes (
                id SERIAL PRIMARY KEY,
                items_json TEXT,
                total DECIMAL(12,2),
                estado VARCHAR(20) DEFAULT 'PENDIENTE',
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                completed_at TIMESTAMP
            )
        """;
        try (Connection con = dataSource.getConnection();
             Statement st = con.createStatement()) {
            st.execute(sql);
        } catch (SQLException e) {
            System.out.println("Error creating ordenes table: " + e.getMessage());
        }
    }

    private Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    @Override
    public void save(Orden orden) {
        String sql = "INSERT INTO ordenes (items_json, total, estado, created_at) VALUES (?, ?, ?, ?)";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, orden.getItemsJson());
            ps.setBigDecimal(2, orden.getTotal());
            ps.setString(3, orden.getEstado() != null ? orden.getEstado() : "PENDIENTE");
            ps.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
            ps.executeUpdate();
            var rs = ps.getGeneratedKeys();
            if (rs.next()) orden.setId(rs.getLong(1));
        } catch (SQLException e) {
            throw new RuntimeException("Error saving orden", e);
        }
    }

    @Override
    public List<Orden> findAll() {
        String sql = "SELECT * FROM ordenes ORDER BY created_at DESC";
        List<Orden> list = new ArrayList<>();
        try (Connection con = getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Error finding ordenes", e);
        }
        return list;
    }

    @Override
    public Orden findById(Long id) {
        String sql = "SELECT * FROM ordenes WHERE id = ?";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding orden", e);
        }
        return null;
    }

    @Override
    public void updateEstado(Long id, String estado) {
        String sql = "UPDATE ordenes SET estado = ?, completed_at = ? WHERE id = ?";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, estado);
            ps.setTimestamp(2, "COMPLETADA".equals(estado) ? Timestamp.valueOf(LocalDateTime.now()) : null);
            ps.setLong(3, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error updating orden estado", e);
        }
    }

    private Orden map(ResultSet rs) throws SQLException {
        Orden o = new Orden();
        o.setId(rs.getLong("id"));
        o.setItemsJson(rs.getString("items_json"));
        o.setTotal(rs.getBigDecimal("total"));
        o.setEstado(rs.getString("estado"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) o.setCreatedAt(ts.toLocalDateTime());
        ts = rs.getTimestamp("completed_at");
        if (ts != null) o.setCompletedAt(ts.toLocalDateTime());
        return o;
    }
}
