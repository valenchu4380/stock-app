package com.valentin.tu_cv_spring_bot.TuCv.ProductoReposirotio.impl;

import com.valentin.tu_cv_spring_bot.TuCv.ProductoReposirotio.PromocionRepository;
import com.valentin.tu_cv_spring_bot.TuCv.mODEL.Promocion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Repository
public class PromocionRepositoryImpl implements PromocionRepository {

    private static final Logger log = LoggerFactory.getLogger(PromocionRepositoryImpl.class);
    private final DataSource dataSource;

    public PromocionRepositoryImpl(DataSource dataSource) {
        this.dataSource = dataSource;
        crearTabla();
    }

    private void crearTabla() {
        String sql = "CREATE TABLE IF NOT EXISTS promociones (" +
                "id SERIAL PRIMARY KEY, " +
                "nombre VARCHAR(255) NOT NULL, " +
                "descripcion TEXT DEFAULT '', " +
                "descuento DOUBLE PRECISION NOT NULL, " +
                "tipo_descuento VARCHAR(50) DEFAULT 'PORCENTAJE', " +
                "tipo_target VARCHAR(50) DEFAULT 'CATEGORIA', " +
                "target_valor VARCHAR(500) DEFAULT '', " +
                "fecha_inicio DATE, " +
                "fecha_fin DATE, " +
                "activa BOOLEAN DEFAULT true)";
        try (Connection con = dataSource.getConnection();
             Statement st = con.createStatement()) {
            st.execute(sql);
            log.info("Tabla promociones creada/verificada");
        } catch (SQLException e) {
            log.warn("Error al crear tabla promociones: {}", e.getMessage());
        }
    }

    private Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    @Override
    public List<Promocion> findAll() {
        List<Promocion> list = new ArrayList<>();
        String sql = "SELECT * FROM promociones ORDER BY id DESC";
        try (Connection con = getConnection();
             PreparedStatement st = con.prepareStatement(sql);
             ResultSet rs = st.executeQuery()) {
            while (rs.next()) {
                list.add(mapResult(rs));
            }
        } catch (SQLException e) {
            log.error("Error finding all promos", e);
        }
        return list;
    }

    @Override
    public Promocion findById(Long id) {
        String sql = "SELECT * FROM promociones WHERE id = ?";
        try (Connection con = getConnection();
             PreparedStatement st = con.prepareStatement(sql)) {
            st.setLong(1, id);
            ResultSet rs = st.executeQuery();
            if (rs.next()) return mapResult(rs);
        } catch (SQLException e) {
            log.error("Error finding promo by id: {}", id, e);
        }
        return null;
    }

    @Override
    public List<Promocion> findActivas() {
        List<Promocion> list = new ArrayList<>();
        String sql = "SELECT * FROM promociones WHERE activa = true AND (fecha_fin IS NULL OR fecha_fin >= CURRENT_DATE) AND (fecha_inicio IS NULL OR fecha_inicio <= CURRENT_DATE) ORDER BY id DESC";
        try (Connection con = getConnection();
             PreparedStatement st = con.prepareStatement(sql);
             ResultSet rs = st.executeQuery()) {
            while (rs.next()) {
                list.add(mapResult(rs));
            }
        } catch (SQLException e) {
            log.error("Error finding active promos", e);
        }
        return list;
    }

    @Override
    public void save(Promocion p) {
        String sql = "INSERT INTO promociones(nombre, descripcion, descuento, tipo_descuento, tipo_target, target_valor, fecha_inicio, fecha_fin, activa) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection con = getConnection();
             PreparedStatement st = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            st.setString(1, p.getNombre());
            st.setString(2, p.getDescripcion() != null ? p.getDescripcion() : "");
            st.setDouble(3, p.getDescuento());
            st.setString(4, p.getTipoDescuento() != null ? p.getTipoDescuento() : "PORCENTAJE");
            st.setString(5, p.getTipoTarget() != null ? p.getTipoTarget() : "CATEGORIA");
            st.setString(6, p.getTargetValor() != null ? p.getTargetValor() : "");
            st.setDate(7, p.getFechaInicio() != null ? Date.valueOf(p.getFechaInicio()) : null);
            st.setDate(8, p.getFechaFin() != null ? Date.valueOf(p.getFechaFin()) : null);
            st.setBoolean(9, p.isActiva());
            st.executeUpdate();
            ResultSet keys = st.getGeneratedKeys();
            if (keys.next()) p.setId(keys.getLong(1));
        } catch (SQLException e) {
            log.error("Error saving promo: {}", p.getNombre(), e);
            throw new RuntimeException("Error al guardar promocion: " + e.getMessage(), e);
        }
    }

    @Override
    public void update(Promocion p) {
        String sql = "UPDATE promociones SET nombre=?, descripcion=?, descuento=?, tipo_descuento=?, tipo_target=?, target_valor=?, fecha_inicio=?, fecha_fin=?, activa=? WHERE id=?";
        try (Connection con = getConnection();
             PreparedStatement st = con.prepareStatement(sql)) {
            st.setString(1, p.getNombre());
            st.setString(2, p.getDescripcion() != null ? p.getDescripcion() : "");
            st.setDouble(3, p.getDescuento());
            st.setString(4, p.getTipoDescuento() != null ? p.getTipoDescuento() : "PORCENTAJE");
            st.setString(5, p.getTipoTarget() != null ? p.getTipoTarget() : "CATEGORIA");
            st.setString(6, p.getTargetValor() != null ? p.getTargetValor() : "");
            st.setDate(7, p.getFechaInicio() != null ? Date.valueOf(p.getFechaInicio()) : null);
            st.setDate(8, p.getFechaFin() != null ? Date.valueOf(p.getFechaFin()) : null);
            st.setBoolean(9, p.isActiva());
            st.setLong(10, p.getId());
            st.executeUpdate();
        } catch (SQLException e) {
            log.error("Error updating promo: {}", p.getId(), e);
            throw new RuntimeException("Error al actualizar promocion: " + e.getMessage(), e);
        }
    }

    @Override
    public void delete(Long id) {
        String sql = "DELETE FROM promociones WHERE id = ?";
        try (Connection con = getConnection();
             PreparedStatement st = con.prepareStatement(sql)) {
            st.setLong(1, id);
            st.executeUpdate();
        } catch (SQLException e) {
            log.error("Error deleting promo: {}", id, e);
            throw new RuntimeException("Error al eliminar promocion: " + e.getMessage(), e);
        }
    }

    private Promocion mapResult(ResultSet rs) throws SQLException {
        Promocion p = new Promocion();
        p.setId(rs.getLong("id"));
        p.setNombre(rs.getString("nombre"));
        p.setDescripcion(rs.getString("descripcion"));
        p.setDescuento(rs.getDouble("descuento"));
        p.setTipoDescuento(rs.getString("tipo_descuento"));
        p.setTipoTarget(rs.getString("tipo_target"));
        p.setTargetValor(rs.getString("target_valor"));
        Date fi = rs.getDate("fecha_inicio");
        p.setFechaInicio(fi != null ? fi.toLocalDate() : null);
        Date ff = rs.getDate("fecha_fin");
        p.setFechaFin(ff != null ? ff.toLocalDate() : null);
        p.setActiva(rs.getBoolean("activa"));
        return p;
    }
}
