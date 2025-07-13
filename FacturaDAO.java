import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FacturaDAO implements BaseDAO<Factura> {
    private Connection connection;

    public FacturaDAO() {
        this.connection = DatabaseConfig.getInstance().getConnection();
    }

    @Override
    public void insertar(Factura factura) throws DatabaseException {
        String sql = "INSERT INTO facturas (numeroFactura, clienteId, usuarioId, subtotal, impuestos, descuentos, total, estado, observaciones) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try {
            connection.setAutoCommit(false); // Iniciar transacción

            // Insertar factura
            try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, factura.getNumeroFactura());
                stmt.setInt(2, factura.getClienteId());
                stmt.setInt(3, factura.getUsuarioId());
                stmt.setBigDecimal(4, factura.getSubtotal());
                stmt.setBigDecimal(5, factura.getImpuestos());
                stmt.setBigDecimal(6, factura.getDescuentos());
                stmt.setBigDecimal(7, factura.getTotal());
                stmt.setString(8, factura.getEstado());
                stmt.setString(9, factura.getObservaciones());

                int affectedRows = stmt.executeUpdate();
                if (affectedRows == 0) {
                    throw new DatabaseException("Error al insertar factura");
                }

                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        factura.setId(generatedKeys.getInt(1));
                    }
                }
            }

            // Insertar detalles
            DetalleFacturaDAO detalleDAO = new DetalleFacturaDAO();
            for (DetalleFactura detalle : factura.getDetalles()) {
                detalle.setFacturaId(factura.getId());
                detalleDAO.insertar(detalle);
            }

            connection.commit(); // Confirmar transacción
        } catch (SQLException e) {
            try {
                connection.rollback(); // Revertir en caso de error
            } catch (SQLException rollbackEx) {
                throw new DatabaseException("Error al revertir transacción: " + rollbackEx.getMessage(), rollbackEx);
            }
            if (e.getMessage().contains("UNIQUE constraint failed: facturas.numeroFactura")) {
                throw new DatabaseException("Ya existe una factura con el número: " + factura.getNumeroFactura());
            }
            throw new DatabaseException("Error al insertar factura: " + e.getMessage(), e);
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                throw new DatabaseException("Error al restaurar autocommit: " + e.getMessage(), e);
            }
        }
    }

    @Override
    public void actualizar(Factura factura) throws DatabaseException {
        String sql = "UPDATE facturas SET numeroFactura = ?, clienteId = ?, usuarioId = ?, subtotal = ?, impuestos = ?, descuentos = ?, total = ?, estado = ?, observaciones = ? WHERE id = ?";

        try {
            connection.setAutoCommit(false); // Iniciar transacción

            // Actualizar factura
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, factura.getNumeroFactura());
                stmt.setInt(2, factura.getClienteId());
                stmt.setInt(3, factura.getUsuarioId());
                stmt.setBigDecimal(4, factura.getSubtotal());
                stmt.setBigDecimal(5, factura.getImpuestos());
                stmt.setBigDecimal(6, factura.getDescuentos());
                stmt.setBigDecimal(7, factura.getTotal());
                stmt.setString(8, factura.getEstado());
                stmt.setString(9, factura.getObservaciones());
                stmt.setInt(10, factura.getId());

                int affectedRows = stmt.executeUpdate();
                if (affectedRows == 0) {
                    throw new DatabaseException("Factura no encontrada con ID: " + factura.getId());
                }
            }

            // Actualizar detalles (eliminar existentes y reinsertar)
            DetalleFacturaDAO detalleDAO = new DetalleFacturaDAO();
            detalleDAO.eliminarPorFactura(factura.getId());
            for (DetalleFactura detalle : factura.getDetalles()) {
                detalle.setFacturaId(factura.getId());
                detalleDAO.insertar(detalle);
            }

            connection.commit();
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException rollbackEx) {
                throw new DatabaseException("Error al revertir transacción: " + rollbackEx.getMessage(), rollbackEx);
            }
            if (e.getMessage().contains("UNIQUE constraint failed: facturas.numeroFactura")) {
                throw new DatabaseException("Ya existe una factura con el número: " + factura.getNumeroFactura());
            }
            throw new DatabaseException("Error al actualizar factura: " + e.getMessage(), e);
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                throw new DatabaseException("Error al restaurar autocommit: " + e.getMessage(), e);
            }
        }
    }

    @Override
    public void eliminar(int id) throws DatabaseException {
        String sql = "UPDATE facturas SET estado = 'ANULADA' WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new DatabaseException("Factura no encontrada con ID: " + id);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error al anular factura: " + e.getMessage(), e);
        }
    }

    @Override
    public Factura obtenerPorId(int id) throws DatabaseException {
        String sql = "SELECT * FROM vista_facturas WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Factura factura = mapearFactura(rs);
                    factura.setDetalles(new DetalleFacturaDAO().obtenerPorFactura(factura.getId()));
                    return factura;
                }
                return null;
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error al obtener factura: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Factura> obtenerTodos() throws DatabaseException {
        String sql = "SELECT * FROM vista_facturas ORDER BY fechaEmision DESC";

        try (PreparedStatement stmt = connection.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {
            List<Factura> facturas = new ArrayList<>();
            while (rs.next()) {
                Factura factura = mapearFactura(rs);
                factura.setDetalles(new DetalleFacturaDAO().obtenerPorFactura(factura.getId()));
                facturas.add(factura);
            }
            return facturas;
        } catch (SQLException e) {
            throw new DatabaseException("Error al obtener facturas: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Factura> obtenerActivos() throws DatabaseException {
        String sql = "SELECT * FROM vista_facturas WHERE estado = 'COMPLETADA' OR estado = 'PENDIENTE' ORDER BY fechaEmision DESC";

        try (PreparedStatement stmt = connection.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {
            List<Factura> facturas = new ArrayList<>();
            while (rs.next()) {
                Factura factura = mapearFactura(rs);
                factura.setDetalles(new DetalleFacturaDAO().obtenerPorFactura(factura.getId()));
                facturas.add(factura);
            }
            return facturas;
        } catch (SQLException e) {
            throw new DatabaseException("Error al obtener facturas activas: " + e.getMessage(), e);
        }
    }

    // Métodos específicos
    public Factura obtenerPorNumero(String numeroFactura) throws DatabaseException {
        String sql = "SELECT * FROM vista_facturas WHERE numeroFactura = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, numeroFactura);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Factura factura = mapearFactura(rs);
                    factura.setDetalles(new DetalleFacturaDAO().obtenerPorFactura(factura.getId()));
                    return factura;
                }
                return null;
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error al buscar factura por número: " + e.getMessage(), e);
        }
    }

    public boolean existeNumeroFactura(String numeroFactura, int excludeId) throws DatabaseException {
        String sql = "SELECT COUNT(*) FROM facturas WHERE numeroFactura = ? AND id != ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, numeroFactura);
            stmt.setInt(2, excludeId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
                return false;
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error al verificar número de factura: " + e.getMessage(), e);
        }
    }

    private Factura mapearFactura(ResultSet rs) throws SQLException {
        Factura factura = new Factura();
        factura.setId(rs.getInt("id"));
        factura.setNumeroFactura(rs.getString("numeroFactura"));
        factura.setClienteId(rs.getInt("clienteId"));
        factura.setClienteNombre(rs.getString("cliente_nombre"));
        factura.setUsuarioId(rs.getInt("usuarioId"));
        factura.setUsuarioNombre(rs.getString("usuario_nombre"));
        factura.setSubtotal(rs.getBigDecimal("subtotal"));
        factura.setImpuestos(rs.getBigDecimal("impuestos"));
        factura.setDescuentos(rs.getBigDecimal("descuentos"));
        factura.setTotal(rs.getBigDecimal("total"));
        factura.setEstado(rs.getString("estado"));
        factura.setObservaciones(rs.getString("observaciones"));

        Timestamp timestamp = rs.getTimestamp("fechaEmision");
        if (timestamp != null) {
            factura.setFechaEmision(timestamp.toLocalDateTime());
        }

        return factura;
    }
}