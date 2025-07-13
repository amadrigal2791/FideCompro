import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class InventarioDAO implements BaseDAO<Inventario> {
    private Connection connection;

    public InventarioDAO() {
        this.connection = DatabaseConfig.getInstance().getConnection();
    }

    @Override
    public void insertar(Inventario inventario) throws DatabaseException {
        String sql = "INSERT INTO inventario (productoId, tipoMovimiento, cantidad, motivo, usuarioId, facturaId) VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, inventario.getProductoId());
            stmt.setString(2, inventario.getTipoMovimiento());
            stmt.setInt(3, inventario.getCantidad());
            stmt.setString(4, inventario.getMotivo());
            stmt.setInt(5, inventario.getUsuarioId());
            if (inventario.getFacturaId() != null) {
                stmt.setInt(6, inventario.getFacturaId());
            } else {
                stmt.setNull(6, Types.INTEGER);
            }

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new DatabaseException("Error al insertar movimiento de inventario");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    inventario.setId(generatedKeys.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error al insertar movimiento de inventario: " + e.getMessage(), e);
        }
    }

    @Override
    public void actualizar(Inventario inventario) throws DatabaseException {
        String sql = "UPDATE inventario SET productoId = ?, tipoMovimiento = ?, cantidad = ?, motivo = ?, usuarioId = ?, facturaId = ? WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, inventario.getProductoId());
            stmt.setString(2, inventario.getTipoMovimiento());
            stmt.setInt(3, inventario.getCantidad());
            stmt.setString(4, inventario.getMotivo());
            stmt.setInt(5, inventario.getUsuarioId());
            if (inventario.getFacturaId() != null) {
                stmt.setInt(6, inventario.getFacturaId());
            } else {
                stmt.setNull(6, Types.INTEGER);
            }
            stmt.setInt(7, inventario.getId());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new DatabaseException("Movimiento de inventario no encontrado con ID: " + inventario.getId());
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error al actualizar movimiento de inventario: " + e.getMessage(), e);
        }
    }

    @Override
    public void eliminar(int id) throws DatabaseException {
        String sql = "DELETE FROM inventario WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new DatabaseException("Movimiento de inventario no encontrado con ID: " + id);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error al eliminar movimiento de inventario: " + e.getMessage(), e);
        }
    }

    @Override
    public Inventario obtenerPorId(int id) throws DatabaseException {
        String sql = "SELECT i.*, p.nombre AS producto_nombre, u.nombre || ' ' || u.apellido AS usuario_nombre " +
                "FROM inventario i " +
                "JOIN productos p ON i.productoId = p.id " +
                "JOIN usuarios u ON i.usuarioId = u.id " +
                "WHERE i.id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapearInventario(rs);
                }
                return null;
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error al obtener movimiento de inventario: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Inventario> obtenerTodos() throws DatabaseException {
        String sql = "SELECT i.*, p.nombre AS producto_nombre, u.nombre || ' ' || u.apellido AS usuario_nombre " +
                "FROM inventario i " +
                "JOIN productos p ON i.productoId = p.id " +
                "JOIN usuarios u ON i.usuarioId = u.id " +
                "ORDER BY i.fechaMovimiento DESC";

        try (PreparedStatement stmt = connection.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {
            List<Inventario> movimientos = new ArrayList<>();
            while (rs.next()) {
                movimientos.add(mapearInventario(rs));
            }
            return movimientos;
        } catch (SQLException e) {
            throw new DatabaseException("Error al obtener movimientos de inventario: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Inventario> obtenerActivos() throws DatabaseException {
        // No aplica "activo" para inventario, todos los movimientos son relevantes
        return obtenerTodos();
    }

    public List<Inventario> obtenerPorProducto(int productoId) throws DatabaseException {
        String sql = "SELECT i.*, p.nombre AS producto_nombre, u.nombre || ' ' || u.apellido AS usuario_nombre " +
                "FROM inventario i " +
                "JOIN productos p ON i.productoId = p.id " +
                "JOIN usuarios u ON i.usuarioId = u.id " +
                "WHERE i.productoId = ? " +
                "ORDER BY i.fechaMovimiento DESC";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, productoId);

            try (ResultSet rs = stmt.executeQuery()) {
                List<Inventario> movimientos = new ArrayList<>();
                while (rs.next()) {
                    movimientos.add(mapearInventario(rs));
                }
                return movimientos;
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error al obtener movimientos por producto: " + e.getMessage(), e);
        }
    }

    private Inventario mapearInventario(ResultSet rs) throws SQLException {
        Inventario inventario = new Inventario();
        inventario.setId(rs.getInt("id"));
        inventario.setProductoId(rs.getInt("productoId"));
        inventario.setProductoNombre(rs.getString("producto_nombre"));
        inventario.setTipoMovimiento(rs.getString("tipoMovimiento"));
        inventario.setCantidad(rs.getInt("cantidad"));
        inventario.setMotivo(rs.getString("motivo"));
        inventario.setUsuarioId(rs.getInt("usuarioId"));
        inventario.setUsuarioNombre(rs.getString("usuario_nombre"));

        int facturaId = rs.getInt("facturaId");
        if (!rs.wasNull()) {
            inventario.setFacturaId(facturaId);
        }

        Timestamp timestamp = rs.getTimestamp("fechaMovimiento");
        if (timestamp != null) {
            inventario.setFechaMovimiento(timestamp.toLocalDateTime());
        }

        return inventario;
    }
}