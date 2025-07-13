import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ClienteDAO implements BaseDAO<Cliente> {
    private Connection connection;

    public ClienteDAO() {
        this.connection = DatabaseConfig.getInstance().getConnection();
    }

    @Override
    public void insertar(Cliente cliente) throws DatabaseException {
        String sql = "INSERT INTO clientes (nombre, apellido, empresa, telefono, email, direccion, cedula) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, cliente.getNombre());
            stmt.setString(2, cliente.getApellido());
            stmt.setString(3, cliente.getEmpresa());
            stmt.setString(4, cliente.getTelefono());
            stmt.setString(5, cliente.getEmail());
            stmt.setString(6, cliente.getDireccion());
            stmt.setString(7, cliente.getCedula());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new DatabaseException("Error al insertar cliente");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    cliente.setId(generatedKeys.getInt(1));
                }
            }
        } catch (SQLException e) {
            if (e.getMessage().contains("UNIQUE constraint failed: clientes.cedula")) {
                throw new DatabaseException("Ya existe un cliente con la cédula: " + cliente.getCedula());
            }
            throw new DatabaseException("Error al insertar cliente: " + e.getMessage(), e);
        }
    }

    @Override
    public void actualizar(Cliente cliente) throws DatabaseException {
        String sql = "UPDATE clientes SET nombre = ?, apellido = ?, empresa = ?, telefono = ?, email = ?, direccion = ?, cedula = ?, activo = ? WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, cliente.getNombre());
            stmt.setString(2, cliente.getApellido());
            stmt.setString(3, cliente.getEmpresa());
            stmt.setString(4, cliente.getTelefono());
            stmt.setString(5, cliente.getEmail());
            stmt.setString(6, cliente.getDireccion());
            stmt.setString(7, cliente.getCedula());
            stmt.setBoolean(8, cliente.isActivo());
            stmt.setInt(9, cliente.getId());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new DatabaseException("Cliente no encontrado con ID: " + cliente.getId());
            }
        } catch (SQLException e) {
            if (e.getMessage().contains("UNIQUE constraint failed: clientes.cedula")) {
                throw new DatabaseException("Ya existe un cliente con la cédula: " + cliente.getCedula());
            }
            throw new DatabaseException("Error al actualizar cliente: " + e.getMessage(), e);
        }
    }

    @Override
    public void eliminar(int id) throws DatabaseException {
        String sql = "UPDATE clientes SET activo = 0 WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new DatabaseException("Cliente no encontrado con ID: " + id);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error al eliminar cliente: " + e.getMessage(), e);
        }
    }

    @Override
    public Cliente obtenerPorId(int id) throws DatabaseException {
        String sql = "SELECT * FROM clientes WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapearCliente(rs);
                }
                return null;
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error al obtener cliente: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Cliente> obtenerTodos() throws DatabaseException {
        String sql = "SELECT * FROM clientes ORDER BY nombre, apellido";

        try (PreparedStatement stmt = connection.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {

            List<Cliente> clientes = new ArrayList<>();
            while (rs.next()) {
                clientes.add(mapearCliente(rs));
            }
            return clientes;
        } catch (SQLException e) {
            throw new DatabaseException("Error al obtener clientes: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Cliente> obtenerActivos() throws DatabaseException {
        String sql = "SELECT * FROM clientes WHERE activo = 1 ORDER BY nombre, apellido";

        try (PreparedStatement stmt = connection.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {

            List<Cliente> clientes = new ArrayList<>();
            while (rs.next()) {
                clientes.add(mapearCliente(rs));
            }
            return clientes;
        } catch (SQLException e) {
            throw new DatabaseException("Error al obtener clientes activos: " + e.getMessage(), e);
        }
    }

    // Métodos específicos para Cliente
    public Cliente obtenerPorCedula(String cedula) throws DatabaseException {
        String sql = "SELECT * FROM clientes WHERE cedula = ? AND activo = 1";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, cedula);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapearCliente(rs);
                }
                return null;
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error al buscar cliente por cédula: " + e.getMessage(), e);
        }
    }

    public List<Cliente> buscarPorNombre(String nombre) throws DatabaseException {
        String sql = "SELECT * FROM clientes WHERE (nombre LIKE ? OR apellido LIKE ?) AND activo = 1 ORDER BY nombre, apellido";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            String searchPattern = "%" + nombre + "%";
            stmt.setString(1, searchPattern);
            stmt.setString(2, searchPattern);

            try (ResultSet rs = stmt.executeQuery()) {
                List<Cliente> clientes = new ArrayList<>();
                while (rs.next()) {
                    clientes.add(mapearCliente(rs));
                }
                return clientes;
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error al buscar clientes por nombre: " + e.getMessage(), e);
        }
    }

    public boolean existeCedula(String cedula, int excludeId) throws DatabaseException {
        String sql = "SELECT COUNT(*) FROM clientes WHERE cedula = ? AND id != ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, cedula);
            stmt.setInt(2, excludeId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
                return false;
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error al verificar cédula: " + e.getMessage(), e);
        }
    }

    private Cliente mapearCliente(ResultSet rs) throws SQLException {
        Cliente cliente = new Cliente();
        cliente.setId(rs.getInt("id"));
        cliente.setNombre(rs.getString("nombre"));
        cliente.setApellido(rs.getString("apellido"));
        cliente.setEmpresa(rs.getString("empresa"));
        cliente.setTelefono(rs.getString("telefono"));
        cliente.setEmail(rs.getString("email"));
        cliente.setDireccion(rs.getString("direccion"));
        cliente.setCedula(rs.getString("cedula"));
        cliente.setActivo(rs.getBoolean("activo"));

        Timestamp timestamp = rs.getTimestamp("fechaRegistro");
        if (timestamp != null) {
            cliente.setFechaRegistro(timestamp.toLocalDateTime());
        }

        return cliente;
    }
}
