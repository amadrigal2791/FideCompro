import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductoDAO implements BaseDAO<Producto> {
    private Connection connection;

    public ProductoDAO() {
        this.connection = DatabaseConfig.getInstance().getConnection();
    }

    @Override
    public void insertar(Producto producto) throws DatabaseException {
        String sql = "INSERT INTO productos (codigo, nombre, descripcion, categoriaId, precio, stock, stockMinimo, unidadMedida) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, producto.getCodigo());
            stmt.setString(2, producto.getNombre());
            stmt.setString(3, producto.getDescripcion());
            stmt.setInt(4, producto.getCategoriaId());
            stmt.setBigDecimal(5, producto.getPrecio());
            stmt.setInt(6, producto.getStock());
            stmt.setInt(7, producto.getStockMinimo());
            stmt.setString(8, producto.getUnidadMedida());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new DatabaseException("Error al insertar producto");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    producto.setId(generatedKeys.getInt(1));
                }
            }
        } catch (SQLException e) {
            if (e.getMessage().contains("UNIQUE constraint failed: productos.codigo")) {
                throw new DatabaseException("Ya existe un producto con el código: " + producto.getCodigo());
            }
            if (e.getMessage().contains("UNIQUE constraint failed: productos.nombre, productos.categoriaId")) {
                throw new DatabaseException("Ya existe un producto con ese nombre en la categoría seleccionada");
            }
            throw new DatabaseException("Error al insertar producto: " + e.getMessage(), e);
        }
    }

    @Override
    public void actualizar(Producto producto) throws DatabaseException {
        String sql = "UPDATE productos SET codigo = ?, nombre = ?, descripcion = ?, categoriaId = ?, precio = ?, stock = ?, stockMinimo = ?, unidadMedida = ?, activo = ? WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, producto.getCodigo());
            stmt.setString(2, producto.getNombre());
            stmt.setString(3, producto.getDescripcion());
            stmt.setInt(4, producto.getCategoriaId());
            stmt.setBigDecimal(5, producto.getPrecio());
            stmt.setInt(6, producto.getStock());
            stmt.setInt(7, producto.getStockMinimo());
            stmt.setString(8, producto.getUnidadMedida());
            stmt.setBoolean(9, producto.isActivo());
            stmt.setInt(10, producto.getId());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new DatabaseException("Producto no encontrado con ID: " + producto.getId());
            }
        } catch (SQLException e) {
            if (e.getMessage().contains("UNIQUE constraint failed: productos.codigo")) {
                throw new DatabaseException("Ya existe un producto con el código: " + producto.getCodigo());
            }
            if (e.getMessage().contains("UNIQUE constraint failed: productos.nombre, productos.categoriaId")) {
                throw new DatabaseException("Ya existe un producto con ese nombre en la categoría seleccionada");
            }
            throw new DatabaseException("Error al actualizar producto: " + e.getMessage(), e);
        }
    }

    @Override
    public void eliminar(int id) throws DatabaseException {
        String sql = "UPDATE productos SET activo = 0 WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new DatabaseException("Producto no encontrado con ID: " + id);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error al eliminar producto: " + e.getMessage(), e);
        }
    }

    @Override
    public Producto obtenerPorId(int id) throws DatabaseException {
        String sql = "SELECT * FROM vista_productos WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapearProductoCompleto(rs);
                }
                return null;
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error al obtener producto: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Producto> obtenerTodos() throws DatabaseException {
        String sql = "SELECT * FROM vista_productos ORDER BY nombre";

        try (PreparedStatement stmt = connection.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {

            List<Producto> productos = new ArrayList<>();
            while (rs.next()) {
                productos.add(mapearProductoCompleto(rs));
            }
            return productos;
        } catch (SQLException e) {
            throw new DatabaseException("Error al obtener productos: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Producto> obtenerActivos() throws DatabaseException {
        String sql = "SELECT * FROM vista_productos WHERE activo = 1 ORDER BY nombre";

        try (PreparedStatement stmt = connection.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {

            List<Producto> productos = new ArrayList<>();
            while (rs.next()) {
                productos.add(mapearProductoCompleto(rs));
            }
            return productos;
        } catch (SQLException e) {
            throw new DatabaseException("Error al obtener productos activos: " + e.getMessage(), e);
        }
    }

    // Métodos específicos para Producto
    public List<Producto> obtenerPorCategoria(int categoriaId) throws DatabaseException {
        String sql = "SELECT * FROM vista_productos WHERE categoriaId = ? AND activo = 1 ORDER BY nombre";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, categoriaId);

            try (ResultSet rs = stmt.executeQuery()) {
                List<Producto> productos = new ArrayList<>();
                while (rs.next()) {
                    productos.add(mapearProductoCompleto(rs));
                }
                return productos;
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error al obtener productos por categoría: " + e.getMessage(), e);
        }
    }

    public List<Producto> obtenerConStockBajo() throws DatabaseException {
        String sql = "SELECT * FROM vista_productos WHERE stock_bajo = 1 AND activo = 1 ORDER BY nombre";

        try (PreparedStatement stmt = connection.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {

            List<Producto> productos = new ArrayList<>();
            while (rs.next()) {
                productos.add(mapearProductoCompleto(rs));
            }
            return productos;
        } catch (SQLException e) {
            throw new DatabaseException("Error al obtener productos con stock bajo: " + e.getMessage(), e);
        }
    }

    public List<Producto> buscarPorNombre(String nombre) throws DatabaseException {
        String sql = "SELECT * FROM vista_productos WHERE nombre LIKE ? AND activo = 1 ORDER BY nombre";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, "%" + nombre + "%");

            try (ResultSet rs = stmt.executeQuery()) {
                List<Producto> productos = new ArrayList<>();
                while (rs.next()) {
                    productos.add(mapearProductoCompleto(rs));
                }
                return productos;
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error al buscar productos por nombre: " + e.getMessage(), e);
        }
    }

    public List<Producto> buscarPorCodigo(String codigo) throws DatabaseException {
        String sql = "SELECT * FROM vista_productos WHERE codigo LIKE ? AND activo = 1 ORDER BY codigo";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, "%" + codigo + "%");

            try (ResultSet rs = stmt.executeQuery()) {
                List<Producto> productos = new ArrayList<>();
                while (rs.next()) {
                    productos.add(mapearProductoCompleto(rs));
                }
                return productos;
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error al buscar productos por código: " + e.getMessage(), e);
        }
    }

    public Producto obtenerPorCodigo(String codigo) throws DatabaseException {
        String sql = "SELECT * FROM vista_productos WHERE codigo = ? AND activo = 1";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, codigo);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapearProductoCompleto(rs);
                }
                return null;
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error al obtener producto por código: " + e.getMessage(), e);
        }
    }

    public boolean existeCodigo(String codigo, int excludeId) throws DatabaseException {
        String sql = "SELECT COUNT(*) FROM productos WHERE codigo = ? AND id != ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, codigo);
            stmt.setInt(2, excludeId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
                return false;
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error al verificar código de producto: " + e.getMessage(), e);
        }
    }

    public boolean existeNombreEnCategoria(String nombre, int categoriaId, int excludeId) throws DatabaseException {
        String sql = "SELECT COUNT(*) FROM productos WHERE nombre = ? AND categoriaId = ? AND id != ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, nombre);
            stmt.setInt(2, categoriaId);
            stmt.setInt(3, excludeId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
                return false;
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error al verificar nombre de producto: " + e.getMessage(), e);
        }
    }

    public void actualizarStock(int productoId, int nuevoStock) throws DatabaseException {
        String sql = "UPDATE productos SET stock = ? WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, nuevoStock);
            stmt.setInt(2, productoId);

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new DatabaseException("Producto no encontrado con ID: " + productoId);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error al actualizar stock: " + e.getMessage(), e);
        }
    }

    private Producto mapearProductoCompleto(ResultSet rs) throws SQLException {
        Producto producto = new Producto();
        producto.setId(rs.getInt("id"));
        producto.setCodigo(rs.getString("codigo"));
        producto.setNombre(rs.getString("nombre"));
        producto.setDescripcion(rs.getString("descripcion"));
        producto.setCategoriaId(rs.getInt("categoriaId"));
        producto.setCategoriaNombre(rs.getString("categoria_nombre"));
        producto.setPrecio(rs.getBigDecimal("precio"));
        producto.setStock(rs.getInt("stock"));
        producto.setStockMinimo(rs.getInt("stockMinimo"));
        producto.setUnidadMedida(rs.getString("unidadMedida"));
        producto.setActivo(rs.getBoolean("activo"));
        producto.setStockBajo(rs.getBoolean("stock_bajo"));

        Timestamp timestamp = rs.getTimestamp("fechaCreacion");
        if (timestamp != null) {
            producto.setFechaCreacion(timestamp.toLocalDateTime());
        }

        return producto;
    }
}