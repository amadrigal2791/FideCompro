import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DetalleFacturaDAO implements BaseDAO<DetalleFactura> {
    private Connection connection;

    public DetalleFacturaDAO() {
        this.connection = DatabaseConfig.getInstance().getConnection();
    }

    @Override
    public void insertar(DetalleFactura detalle) throws DatabaseException {
        String sql = "INSERT INTO detalle_facturas (facturaId, productoId, cantidad, precioUnitario, descuento, subtotal) VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, detalle.getFacturaId());
            stmt.setInt(2, detalle.getProductoId());
            stmt.setInt(3, detalle.getCantidad());
            stmt.setBigDecimal(4, detalle.getPrecioUnitario());
            stmt.setBigDecimal(5, detalle.getDescuento());
            stmt.setBigDecimal(6, detalle.getSubtotal());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new DatabaseException("Error al insertar detalle de factura");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    detalle.setId(generatedKeys.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error al insertar detalle de factura: " + e.getMessage(), e);
        }
    }

    @Override
    public void actualizar(DetalleFactura detalle) throws DatabaseException {
        String sql = "UPDATE detalle_facturas SET facturaId = ?, productoId = ?, cantidad = ?, precioUnitario = ?, descuento = ?, subtotal = ? WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, detalle.getFacturaId());
            stmt.setInt(2, detalle.getProductoId());
            stmt.setInt(3, detalle.getCantidad());
            stmt.setBigDecimal(4, detalle.getPrecioUnitario());
            stmt.setBigDecimal(5, detalle.getDescuento());
            stmt.setBigDecimal(6, detalle.getSubtotal());
            stmt.setInt(7, detalle.getId());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new DatabaseException("Detalle de factura no encontrado con ID: " + detalle.getId());
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error al actualizar detalle de factura: " + e.getMessage(), e);
        }
    }

    @Override
    public void eliminar(int id) throws DatabaseException {
        String sql = "DELETE FROM detalle_facturas WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new DatabaseException("Detalle de factura no encontrado con ID: " + id);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error al eliminar detalle de factura: " + e.getMessage(), e);
        }
    }

    public void eliminarPorFactura(int facturaId) throws DatabaseException {
        String sql = "DELETE FROM detalle_facturas WHERE facturaId = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, facturaId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Error al eliminar detalles de factura: " + e.getMessage(), e);
        }
    }

    @Override
    public DetalleFactura obtenerPorId(int id) throws DatabaseException {
        String sql = "SELECT df.*, p.nombre AS producto_nombre FROM detalle_facturas df JOIN productos p ON df.productoId = p.id WHERE df.id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapearDetalleFactura(rs);
                }
                return null;
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error al obtener detalle de factura: " + e.getMessage(), e);
        }
    }

    @Override
    public List<DetalleFactura> obtenerTodos() throws DatabaseException {
        String sql = "SELECT df.*, p.nombre AS producto_nombre FROM detalle_facturas df JOIN productos p ON df.productoId = p.id ORDER BY df.facturaId, df.id";

        try (PreparedStatement stmt = connection.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {
            List<DetalleFactura> detalles = new ArrayList<>();
            while (rs.next()) {
                detalles.add(mapearDetalleFactura(rs));
            }
            return detalles;
        } catch (SQLException e) {
            throw new DatabaseException("Error al obtener detalles de factura: " + e.getMessage(), e);
        }
    }

    @Override
    public List<DetalleFactura> obtenerActivos() throws DatabaseException {
        // No aplica, los detalles están ligados a facturas activas
        return obtenerTodos();
    }

    public List<DetalleFactura> obtenerPorFactura(int facturaId) throws DatabaseException {
        String sql = "SELECT df.*, p.nombre AS producto_nombre FROM detalle_facturas df JOIN productos p ON df.productoId = p.id WHERE df.facturaId = ? ORDER BY df.id";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, facturaId);

            try (ResultSet rs = stmt.executeQuery()) {
                List<DetalleFactura> detalles = new ArrayList<>();
                while (rs.next()) {
                    detalles.add(mapearDetalleFactura(rs));
                }
                return detalles;
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error al obtener detalles por factura: " + e.getMessage(), e);
        }
    }

    private DetalleFactura mapearDetalleFactura(ResultSet rs) throws SQLException {
        DetalleFactura detalle = new DetalleFactura();
        detalle.setId(rs.getInt("id"));
        detalle.setFacturaId(rs.getInt("facturaId"));
        detalle.setProductoId(rs.getInt("productoId"));
        detalle.setProductoNombre(rs.getString("producto_nombre"));
        detalle.setCantidad(rs.getInt("cantidad"));
        detalle.setPrecioUnitario(rs.getBigDecimal("precioUnitario"));
        detalle.setDescuento(rs.getBigDecimal("descuento"));
        detalle.setSubtotal(rs.getBigDecimal("subtotal"));
        return detalle;
    }
}