package com.valentin.tu_cv_spring_bot.TuCv.ProductoReposirotio.impl;

import com.valentin.tu_cv_spring_bot.TuCv.ProductoReposirotio.UbicacionRepository;
import com.valentin.tu_cv_spring_bot.TuCv.mODEL.Ubicacion;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

@Repository
public class UbicacionRepositoryImpl implements UbicacionRepository {

    private static final Logger log = LoggerFactory.getLogger(UbicacionRepositoryImpl.class);

    private final DataSource dataSource;

    public UbicacionRepositoryImpl(DataSource dataSource) {
        this.dataSource = dataSource;
        migrar();
    }

    private void migrar() {
        try (Connection con = dataSource.getConnection();
             Statement st = con.createStatement()) {
            st.execute("CREATE TABLE IF NOT EXISTS ubicaciones (" +
                    "id BIGSERIAL PRIMARY KEY," +
                    "nombre VARCHAR(100) NOT NULL UNIQUE)");
            st.execute("CREATE TABLE IF NOT EXISTS producto_ubicacion (" +
                    "product_name VARCHAR(255) NOT NULL," +
                    "product_subcategory VARCHAR(100) NOT NULL," +
                    "ubicacion_id BIGINT NOT NULL REFERENCES ubicaciones(id) ON DELETE CASCADE," +
                    "cantidad INT NOT NULL DEFAULT 0 CHECK (cantidad >= 0)," +
                    "PRIMARY KEY (product_name, product_subcategory, ubicacion_id))");
            st.execute("INSERT INTO ubicaciones (nombre) SELECT 'CASA' WHERE NOT EXISTS (SELECT 1 FROM ubicaciones WHERE nombre = 'CASA')");
            st.execute("INSERT INTO ubicaciones (nombre) SELECT 'ESCUELA' WHERE NOT EXISTS (SELECT 1 FROM ubicaciones WHERE nombre = 'ESCUELA')");
        } catch (SQLException e) {
            log.warn("Error al migrar tablas de ubicaciones: {}", e.getMessage());
        }
    }

    @Override
    public List<Ubicacion> list() {
        List<Ubicacion> ubicaciones = new ArrayList<>();
        String sql = "SELECT id, nombre FROM ubicaciones ORDER BY id";
        try (Connection con = dataSource.getConnection();
             PreparedStatement st = con.prepareStatement(sql);
             ResultSet rs = st.executeQuery()) {
            while (rs.next()) {
                ubicaciones.add(new Ubicacion(rs.getLong("id"), rs.getString("nombre")));
            }
        } catch (SQLException e) {
            log.error("Error listando ubicaciones", e);
        }
        return ubicaciones;
    }

    @Override
    public void add(String nombre) {
        String sql = "INSERT INTO ubicaciones (nombre) VALUES (?)";
        try (Connection con = dataSource.getConnection();
             PreparedStatement st = con.prepareStatement(sql)) {
            st.setString(1, nombre.trim().toUpperCase());
            st.executeUpdate();
        } catch (SQLException e) {
            log.error("Error agregando ubicacion: {}", nombre, e);
        }
    }

    @Override
    public void delete(Long id) {
        try (Connection con = dataSource.getConnection()) {
            con.setAutoCommit(false);
            List<String[]> afectados = new ArrayList<>();
            try (PreparedStatement st = con.prepareStatement(
                    "SELECT DISTINCT product_name, product_subcategory FROM producto_ubicacion WHERE ubicacion_id = ?")) {
                st.setLong(1, id);
                try (ResultSet rs = st.executeQuery()) {
                    while (rs.next()) {
                        afectados.add(new String[]{rs.getString("product_name"), rs.getString("product_subcategory")});
                    }
                }
            }
            try (PreparedStatement st = con.prepareStatement("DELETE FROM producto_ubicacion WHERE ubicacion_id = ?")) {
                st.setLong(1, id);
                st.executeUpdate();
            }
            try (PreparedStatement st = con.prepareStatement("DELETE FROM ubicaciones WHERE id = ?")) {
                st.setLong(1, id);
                st.executeUpdate();
            }
            try (PreparedStatement st = con.prepareStatement(
                    "UPDATE products SET stock = COALESCE(" +
                    "(SELECT SUM(cantidad) FROM producto_ubicacion WHERE product_name = ? AND product_subcategory = ?), 0) " +
                    "WHERE name = ? AND subcategory = ?")) {
                for (String[] p : afectados) {
                    st.setString(1, p[0]);
                    st.setString(2, p[1]);
                    st.setString(3, p[0]);
                    st.setString(4, p[1]);
                    st.addBatch();
                }
                st.executeBatch();
            }
            con.commit();
        } catch (SQLException e) {
            log.error("Error eliminando ubicacion: {}", id, e);
        }
    }

    @Override
    public Map<String, Map<Long, Integer>> getQuantitiesMap() {
        Map<String, Map<Long, Integer>> result = new HashMap<>();
        String sql = "SELECT pu.product_name, pu.product_subcategory, pu.ubicacion_id, pu.cantidad " +
                "FROM producto_ubicacion pu ORDER BY pu.product_name, pu.product_subcategory";
        try (Connection con = dataSource.getConnection();
             PreparedStatement st = con.prepareStatement(sql);
             ResultSet rs = st.executeQuery()) {
            while (rs.next()) {
                String key = rs.getString("product_name") + "|" + rs.getString("product_subcategory");
                Map<Long, Integer> m = result.computeIfAbsent(key, k -> new LinkedHashMap<>());
                m.put(rs.getLong("ubicacion_id"), rs.getInt("cantidad"));
            }
        } catch (SQLException e) {
            log.error("Error obteniendo cantidades por ubicacion", e);
        }
        return result;
    }

    @Override
    public void saveAllQuantities(Map<String, Map<Long, Integer>> porProducto) {
        try (Connection con = dataSource.getConnection()) {
            con.setAutoCommit(false);
            try (PreparedStatement del = con.prepareStatement(
                    "DELETE FROM producto_ubicacion WHERE product_name = ? AND product_subcategory = ?")) {
                for (String key : porProducto.keySet()) {
                    String[] parts = key.split("\\|", 2);
                    if (parts.length != 2) continue;
                    del.setString(1, parts[0]);
                    del.setString(2, parts[1]);
                    del.addBatch();
                }
                del.executeBatch();
            }
            try (PreparedStatement ins = con.prepareStatement(
                    "INSERT INTO producto_ubicacion (product_name, product_subcategory, ubicacion_id, cantidad) VALUES (?, ?, ?, ?)")) {
                for (Map.Entry<String, Map<Long, Integer>> e : porProducto.entrySet()) {
                    String[] parts = e.getKey().split("\\|", 2);
                    if (parts.length != 2) continue;
                    for (Map.Entry<Long, Integer> c : e.getValue().entrySet()) {
                        if (c.getValue() == null || c.getValue() <= 0) continue;
                        ins.setString(1, parts[0]);
                        ins.setString(2, parts[1]);
                        ins.setLong(3, c.getKey());
                        ins.setInt(4, c.getValue());
                        ins.addBatch();
                    }
                }
                ins.executeBatch();
            }
            try (PreparedStatement upd = con.prepareStatement(
                    "UPDATE products SET stock = ? WHERE name = ? AND subcategory = ?")) {
                for (Map.Entry<String, Map<Long, Integer>> e : porProducto.entrySet()) {
                    String[] parts = e.getKey().split("\\|", 2);
                    if (parts.length != 2) continue;
                    int total = 0;
                    for (Integer c : e.getValue().values()) total += (c != null ? c : 0);
                    upd.setInt(1, total);
                    upd.setString(2, parts[0]);
                    upd.setString(3, parts[1]);
                    upd.addBatch();
                }
                upd.executeBatch();
            }
            con.commit();
        } catch (SQLException e) {
            log.error("Error guardando cantidades masivas en almacen", e);
            throw new RuntimeException("Error al guardar el almacén: " + e.getMessage(), e);
        }
    }

    @Override
    public void adjust(String productName, String subCategoryName, Long ubicacionId, int cantidad) {
        try (Connection con = dataSource.getConnection()) {
            con.setAutoCommit(false);
            if (cantidad > 0) {
                try (PreparedStatement st = con.prepareStatement(
                        "INSERT INTO producto_ubicacion (product_name, product_subcategory, ubicacion_id, cantidad) " +
                        "VALUES (?, ?, ?, ?) ON CONFLICT (product_name, product_subcategory, ubicacion_id) " +
                        "DO UPDATE SET cantidad = GREATEST(0, producto_ubicacion.cantidad + EXCLUDED.cantidad)")) {
                    st.setString(1, productName);
                    st.setString(2, subCategoryName);
                    st.setLong(3, ubicacionId);
                    st.setInt(4, cantidad);
                    st.executeUpdate();
                }
            } else {
                try (PreparedStatement st = con.prepareStatement(
                        "UPDATE producto_ubicacion SET cantidad = GREATEST(0, cantidad + ?) " +
                        "WHERE product_name = ? AND product_subcategory = ? AND ubicacion_id = ?")) {
                    st.setInt(1, cantidad);
                    st.setString(2, productName);
                    st.setString(3, subCategoryName);
                    st.setLong(4, ubicacionId);
                    st.executeUpdate();
                }
            }
            updateProductStock(con, productName, subCategoryName);
            con.commit();
        } catch (SQLException e) {
            log.error("Error ajustando cantidad en ubicacion: {} | {} | {}", productName, subCategoryName, ubicacionId, e);
        }
    }

    @Override
    public int getTotalStock(String productName, String subCategoryName) {
        String sql = "SELECT COALESCE(SUM(cantidad), 0) FROM producto_ubicacion WHERE product_name = ? AND product_subcategory = ?";
        try (Connection con = dataSource.getConnection();
             PreparedStatement st = con.prepareStatement(sql)) {
            st.setString(1, productName);
            st.setString(2, subCategoryName);
            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            log.error("Error obteniendo stock total por ubicaciones", e);
        }
        return 0;
    }

    private void updateProductStock(Connection con, String productName, String subCategoryName) throws SQLException {
        try (PreparedStatement upd = con.prepareStatement(
                "UPDATE products SET stock = COALESCE(" +
                "(SELECT SUM(cantidad) FROM producto_ubicacion WHERE product_name = ? AND product_subcategory = ?), 0) " +
                "WHERE name = ? AND subcategory = ?")) {
            upd.setString(1, productName);
            upd.setString(2, subCategoryName);
            upd.setString(3, productName);
            upd.setString(4, subCategoryName);
            upd.executeUpdate();
        }
    }
}
