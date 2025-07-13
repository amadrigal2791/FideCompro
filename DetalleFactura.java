import java.math.BigDecimal;

public class DetalleFactura {
    private int id;
    private int facturaId;
    private int productoId;
    private String productoNombre; // Para joins
    private int cantidad;
    private BigDecimal precioUnitario;
    private BigDecimal descuento;
    private BigDecimal subtotal;

    // Constructores
    public DetalleFactura() {
    }

    public DetalleFactura(int productoId, int cantidad, BigDecimal precioUnitario, BigDecimal descuento) {
        this.productoId = productoId;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.descuento = descuento != null ? descuento : BigDecimal.ZERO;
        this.subtotal = precioUnitario.multiply(new BigDecimal(cantidad)).subtract(this.descuento);
    }

    // Getters y Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getFacturaId() {
        return facturaId;
    }

    public void setFacturaId(int facturaId) {
        this.facturaId = facturaId;
    }

    public int getProductoId() {
        return productoId;
    }

    public void setProductoId(int productoId) {
        this.productoId = productoId;
    }

    public String getProductoNombre() {
        return productoNombre;
    }

    public void setProductoNombre(String productoNombre) {
        this.productoNombre = productoNombre;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
        recalcularSubtotal();
    }

    public BigDecimal getPrecioUnitario() {
        return precioUnitario;
    }

    public void setPrecioUnitario(BigDecimal precioUnitario) {
        this.precioUnitario = precioUnitario;
        recalcularSubtotal();
    }

    public BigDecimal getDescuento() {
        return descuento;
    }

    public void setDescuento(BigDecimal descuento) {
        this.descuento = descuento != null ? descuento : BigDecimal.ZERO;
        recalcularSubtotal();
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    private void recalcularSubtotal() {
        this.subtotal = precioUnitario.multiply(new BigDecimal(cantidad)).subtract(descuento);
    }

    @Override
    public String toString() {
        return productoNombre + " x" + cantidad + " ($" + subtotal + ")";
    }
}