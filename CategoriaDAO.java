import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CategoriaDAO implements BaseDAO<Categoria> {
    private Connection connection;

    public CategoriaDAO() {
        this.connection = DatabaseConfig.getInstance().getConnection();
    }

    @Override
    public void insertar(Categoria categoria) throws DatabaseException {
        String sql = "INSERT INTO categorias (nombre, descripcion) VALUES (?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, categoria.getNombre());
            stmt.setString(2, categoria.getDescripcion());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new DatabaseException("Error al insertar categoría");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    categoria.setId(generatedKeys.getInt(1));
                }
            }
        } catch (SQLException e) {
            if (e.getMessage().contains("UNIQUE constraint failed: categorias.nombre")) {
                throw new DatabaseException("Ya existe una categoría con el nombre: " + categoria.getNombre());
            }
            throw new DatabaseException("Error al insertar categoría: " + e.getMessage(), e);
        }
    }

    @Override
    public void actualizar(Categoria categoria) throws DatabaseException {
        String sql = "UPDATE categorias SET nombre = ?, descripcion = ?, activo = ? WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, categoria.getNombre());
            stmt.setString(2, categoria.getDescripcion());
            stmt.setBoolean(3, categoria.isActivo());
            stmt.setInt(4, categoria.getId());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new DatabaseException("Categoría no encontrada con ID: " + categoria.getId());
            }
        } catch (SQLException e) {
            if (e.getMessage().contains("UNIQUE constraint failed: categorias.nombre")) {
                throw new DatabaseException("Ya existe una categoría con el nombre: " + categoria.getNombre());
            }
            throw new DatabaseException("Error al actualizar categoría: " + e.getMessage(), e);
        }
    }

    @Override
    public void eliminar(int id) throws DatabaseException {
        String sql = "UPDATE categorias SET activo = 0 WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new DatabaseException("Categoría no encontrada con ID: " + id);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error al eliminar categoría: " + e.getMessage(), e);
        }
    }

    @Override
    public Categoria obtenerPorId(int id) throws DatabaseException {
        String sql = "SELECT * FROM categorias WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapearCategoria(rs);
                }
                return null;
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error al obtener categoría: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Categoria> obtenerTodos() throws DatabaseException {
        String sql = "SELECT * FROM categorias ORDER BY nombre";

        try (PreparedStatement stmt = connection.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {

            List<Categoria> categorias = new ArrayList<>();
            while (rs.next()) {
                categorias.add(mapearCategoria(rs));
            }
            return categorias;
        } catch (SQLException e) {
            throw new DatabaseException("Error al obtener categorías: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Categoria> obtenerActivos() throws DatabaseException {
        String sql = "SELECT * FROM categorias WHERE activo = 1 ORDER BY nombre";

        try (PreparedStatement stmt = connection.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {

            List<Categoria> categorias = new ArrayList<>();
            while (rs.next()) {
                categorias.add(mapearCategoria(rs));
            }
            return categorias;
        } catch (SQLException e) {
            throw new DatabaseException("Error al obtener categorías activas: " + e.getMessage(), e);
        }
    }

    // Métodos específicos para Categoria
    public boolean existeNombre(String nombre, int excludeId) throws DatabaseException {
        String sql = "SELECT COUNT(*) FROM categorias WHERE nombre = ? AND id != ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, nombre);
            stmt.setInt(2, excludeId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
                return false;
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error al verificar nombre de categoría: " + e.getMessage(), e);
        }
    }

    public boolean tieneProductos(int categoriaId) throws DatabaseException {
        String sql = "SELECT COUNT(*) FROM productos WHERE categoriaId = ? AND activo = 1";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, categoriaId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
                return false;
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error al verificar productos de categoría: " + e.getMessage(), e);
        }
    }

    private Categoria mapearCategoria(ResultSet rs) throws SQLException {
        Categoria categoria = new Categoria();
        categoria.setId(rs.getInt("id"));
        categoria.setNombre(rs.getString("nombre"));
        categoria.setDescripcion(rs.getString("descripcion"));
        categoria.setActivo(rs.getBoolean("activo"));

        Timestamp timestamp = rs.getTimestamp("fechaCreacion");
        if (timestamp != null) {
            categoria.setFechaCreacion(timestamp.toLocalDateTime());
        }

        return categoria;
    }
}
