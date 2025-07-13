import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UsuarioDAO implements BaseDAO<Usuario> {
    private Connection connection;

    public UsuarioDAO() {
        this.connection = DatabaseConfig.getInstance().getConnection();
    }

    @Override
    public void insertar(Usuario usuario) throws DatabaseException {
        String sql = "INSERT INTO usuarios (nombreUsuario, contraseña, nombre, apellido, email, rol) VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, usuario.getNombreUsuario());
            stmt.setString(2, usuario.getContraseña());
            stmt.setString(3, usuario.getNombre());
            stmt.setString(4, usuario.getApellido());
            stmt.setString(5, usuario.getEmail());
            stmt.setString(6, usuario.getRol());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new DatabaseException("Error al insertar usuario");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    usuario.setId(generatedKeys.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error al insertar usuario: " + e.getMessage(), e);
        }
    }

    @Override
    public void actualizar(Usuario usuario) throws DatabaseException {
        String sql = "UPDATE usuarios SET nombreUsuario = ?, contraseña = ?, nombre = ?, apellido = ?, email = ?, rol = ?, activo = ? WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, usuario.getNombreUsuario());
            stmt.setString(2, usuario.getContraseña());
            stmt.setString(3, usuario.getNombre());
            stmt.setString(4, usuario.getApellido());
            stmt.setString(5, usuario.getEmail());
            stmt.setString(6, usuario.getRol());
            stmt.setBoolean(7, usuario.isActivo());
            stmt.setInt(8, usuario.getId());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new DatabaseException("Usuario no encontrado con ID: " + usuario.getId());
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error al actualizar usuario: " + e.getMessage(), e);
        }
    }

    @Override
    public void eliminar(int id) throws DatabaseException {
        String sql = "UPDATE usuarios SET activo = 0 WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new DatabaseException("Usuario no encontrado con ID: " + id);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error al eliminar usuario: " + e.getMessage(), e);
        }
    }

    @Override
    public Usuario obtenerPorId(int id) throws DatabaseException {
        String sql = "SELECT * FROM usuarios WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapearUsuario(rs);
                }
                return null;
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error al obtener usuario: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Usuario> obtenerTodos() throws DatabaseException {
        String sql = "SELECT * FROM usuarios ORDER BY nombre, apellido";

        try (PreparedStatement stmt = connection.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {

            List<Usuario> usuarios = new ArrayList<>();
            while (rs.next()) {
                usuarios.add(mapearUsuario(rs));
            }
            return usuarios;
        } catch (SQLException e) {
            throw new DatabaseException("Error al obtener usuarios: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Usuario> obtenerActivos() throws DatabaseException {
        String sql = "SELECT * FROM usuarios WHERE activo = 1 ORDER BY nombre, apellido";

        try (PreparedStatement stmt = connection.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {

            List<Usuario> usuarios = new ArrayList<>();
            while (rs.next()) {
                usuarios.add(mapearUsuario(rs));
            }
            return usuarios;
        } catch (SQLException e) {
            throw new DatabaseException("Error al obtener usuarios activos: " + e.getMessage(), e);
        }
    }

    // Métodos específicos para Usuario
    public Usuario autenticar(String nombreUsuario, String contraseña) throws DatabaseException {
        String sql = "SELECT * FROM usuarios WHERE nombreUsuario = ? AND contraseña = ? AND activo = 1";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, nombreUsuario);
            stmt.setString(2, contraseña);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapearUsuario(rs);
                }
                return null;
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error al autenticar usuario: " + e.getMessage(), e);
        }
    }

    public boolean existeNombreUsuario(String nombreUsuario, int excludeId) throws DatabaseException {
        String sql = "SELECT COUNT(*) FROM usuarios WHERE nombreUsuario = ? AND id != ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, nombreUsuario);
            stmt.setInt(2, excludeId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
                return false;
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error al verificar nombre de usuario: " + e.getMessage(), e);
        }
    }

    public boolean existeEmail(String email, int excludeId) throws DatabaseException {
        String sql = "SELECT COUNT(*) FROM usuarios WHERE email = ? AND id != ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, email);
            stmt.setInt(2, excludeId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
                return false;
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error al verificar email: " + e.getMessage(), e);
        }
    }

    private Usuario mapearUsuario(ResultSet rs) throws SQLException {
        Usuario usuario = new Usuario();
        usuario.setId(rs.getInt("id"));
        usuario.setNombreUsuario(rs.getString("nombreUsuario"));
        usuario.setContraseña(rs.getString("contraseña"));
        usuario.setNombre(rs.getString("nombre"));
        usuario.setApellido(rs.getString("apellido"));
        usuario.setEmail(rs.getString("email"));
        usuario.setRol(rs.getString("rol"));
        usuario.setActivo(rs.getBoolean("activo"));

        Timestamp timestamp = rs.getTimestamp("fechaCreacion");
        if (timestamp != null) {
            usuario.setFechaCreacion(timestamp.toLocalDateTime());
        }

        return usuario;
    }
}