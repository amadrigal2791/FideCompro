import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Producto {
    private int id;
    private String codigo;
    private String nombre;
    private String descripcion;
    private int categoriaId;
    private String categoriaNombre; // Para joins
    private BigDecimal precio;
    private int stock;
    private int stockMinimo;
    private String unidadMedida;
    private boolean activo;
    private LocalDateTime fechaCreacion;
    private boolean stockBajo; // Para la vista

    // Constructores
    public Producto() {
    }

    public Producto(String codigo, String nombre, int categoriaId,
            BigDecimal precio, int stock, int stockMinimo) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.categoriaId = categoriaId;
        this.precio = precio;
        this.stock = stock;
        this.stockMinimo = stockMinimo;
        this.unidadMedida = "UNIDAD";
        this.activo = true;
    }

    // Getters y Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public int getCategoriaId() {
        return categoriaId;
    }

    public void setCategoriaId(int categoriaId) {
        this.categoriaId = categoriaId;
    }

    public String getCategoriaNombre() {
        return categoriaNombre;
    }

    public void setCategoriaNombre(String categoriaNombre) {
        this.categoriaNombre = categoriaNombre;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public int getStockMinimo() {
        return stockMinimo;
    }

    public void setStockMinimo(int stockMinimo) {
        this.stockMinimo = stockMinimo;
    }

    public String getUnidadMedida() {
        return unidadMedida;
    }

    public void setUnidadMedida(String unidadMedida) {
        this.unidadMedida = unidadMedida;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public boolean isStockBajo() {
        return stockBajo;
    }

    public void setStockBajo(boolean stockBajo) {
        this.stockBajo = stockBajo;
    }

    public boolean tieneStockSuficiente(int cantidad) {
        return stock >= cantidad;
    }

    public String getCodigoNombre() {
        return codigo + " - " + nombre;
    }

    @Override
    public String toString() {
        return getCodigoNombre();
    }
}