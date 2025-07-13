import java.util.List;
import java.sql.*;
import java.util.ArrayList;

public class FacturaService {
    private FacturaDAO facturaDAO = new FacturaDAO();
    private ProductoDAO productoDAO = new ProductoDAO();
    private ClienteDAO clienteDAO = new ClienteDAO();
    private UsuarioDAO usuarioDAO = new UsuarioDAO();

    public void crearFactura(Factura factura, Usuario usuario) throws BusinessException, DatabaseException {
        // Verificar permisos
        if (!usuario.getRol().equals("CAJERO") && !usuario.getRol().equals("ADMINISTRADOR")) {
            throw new BusinessException("Usuario no autorizado para crear facturas");
        }

        // Validar cliente
        Cliente cliente = clienteDAO.obtenerPorId(factura.getClienteId());
        if (cliente == null || !cliente.isActivo()) {
            throw new BusinessException("Cliente no valido o inactivo");
        }

        // Validar numero de factura
        if (facturaDAO.existeNumeroFactura(factura.getNumeroFactura(), 0)) {
            throw new BusinessException("El numero de factura ya existe");
        }

        // Validar que haya detalles
        if (factura.getDetalles().isEmpty()) {
            throw new BusinessException("La factura debe tener al menos un producto");
        }

        // Validar stock y precios de productos
        for (DetalleFactura detalle : factura.getDetalles()) {
            Producto producto = productoDAO.obtenerPorId(detalle.getProductoId());
            if (producto == null || !producto.isActivo()) {
                throw new BusinessException("Producto no valido o inactivo: " + detalle.getProductoNombre());
            }
            if (!producto.tieneStockSuficiente(detalle.getCantidad())) {
                throw new BusinessException("Stock insuficiente para el producto: " + producto.getNombre());
            }
            if (!detalle.getPrecioUnitario().equals(producto.getPrecio())) {
                throw new BusinessException("El precio unitario no coincide con el producto: " + producto.getNombre());
            }
            if (detalle.getCantidad() <= 0) {
                throw new BusinessException("La cantidad debe ser mayor que cero para: " + producto.getNombre());
            }
        }

        // Guardar factura
        factura.setUsuarioId(usuario.getId());
        factura.recalcularTotales();
        facturaDAO.insertar(factura);
    }

    public void anularFactura(int facturaId, Usuario usuario) throws BusinessException, DatabaseException {
        // Verificar permisos (solo administrador)
        if (!usuario.getRol().equals("ADMINISTRADOR")) {
            throw new BusinessException("Solo los administradores pueden anular facturas");
        }

        Factura factura = facturaDAO.obtenerPorId(facturaId);
        if (factura == null) {
            throw new BusinessException("Factura no encontrada con ID: " + facturaId);
        }
        if (factura.getEstado().equals("ANULADA")) {
            throw new BusinessException("La factura ya esta anulada");
        }

        factura.setEstado("ANULADA");
        facturaDAO.actualizar(factura);
    }

    public List<Factura> obtenerVentasDiarias() throws DatabaseException {
        String sql = "SELECT * FROM vista_ventas_diarias WHERE fecha = DATE('now')";
        try (Connection conn = DatabaseConfig.getInstance().getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {
            List<Factura> facturas = new ArrayList<>();
            while (rs.next()) {
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

                factura.setDetalles(new DetalleFacturaDAO().obtenerPorFactura(factura.getId()));
                facturas.add(factura);
            }
            return facturas;
        } catch (SQLException e) {
            throw new DatabaseException("Error al obtener ventas diarias: " + e.getMessage(), e);
        }
    }
}