import java.util.List;

public interface BaseDAO<T> {
    void insertar(T entity) throws DatabaseException;

    void actualizar(T entity) throws DatabaseException;

    void eliminar(int id) throws DatabaseException;

    T obtenerPorId(int id) throws DatabaseException;

    List<T> obtenerTodos() throws DatabaseException;

    List<T> obtenerActivos() throws DatabaseException;
}