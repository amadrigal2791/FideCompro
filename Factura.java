import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Factura {
    private int id;
    private String numeroFactura;
    private int clienteId;
    private String clienteNombre; // Para joins
    private int usuarioId;
    private String usuarioNombre; // Para joins
    private LocalDateTime fechaEmision;
    private BigDecimal subtotal;
    private BigDecimal impuestos;
    private BigDecimal descuentos;
    private BigDecimal total;
    private String estado;
    private String observaciones;
    private List<DetalleFactura> detalles;

    // Constructores
    public Factura() {
        this.detalles = new ArrayList<>();
        this.estado = "COMPLETADA";
        this.subtotal = BigDecimal.ZERO;
        this.impuestos = BigDecimal.ZERO;
        this.descuentos = BigDecimal.ZERO;
        this.total = BigDecimal.ZERO;
    }

    public Factura(String numeroFactura, int clienteId, int usuarioId) {
        this();
        this.numeroFactura = numeroFactura;
        this.clienteId = clienteId;
        this.usuarioId = usuarioId;
    }

    // Getters y Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNumeroFactura() {
        return numeroFactura;
    }

    public void setNumeroFactura(String numeroFactura) {
        this.numeroFactura = numeroFactura;
    }

    public int getClienteId() {
        return clienteId;
    }

    public void setClienteId(int clienteId) {
        this.clienteId = clienteId;
    }

    public String getClienteNombre() {
        return clienteNombre;
    }

    public void setClienteNombre(String clienteNombre) {
        this.clienteNombre = clienteNombre;
    }

    public int getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(int usuarioId) {
        this.usuarioId = usuarioId;
    }

    public String getUsuarioNombre() {
        return usuarioNombre;
    }

    public void setUsuarioNombre(String usuarioNombre) {
        this.usuarioNombre = usuarioNombre;
    }

    public LocalDateTime getFechaEmision() {
        return fechaEmision;
    }

    public void setFechaEmision(LocalDateTime fechaEmision) {
        this.fechaEmision = fechaEmision;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public BigDecimal getImpuestos() {
        return impuestos;
    }

    public void setImpuestos(BigDecimal impuestos) {
        this.impuestos = impuestos;
    }

    public BigDecimal getDescuentos() {
        return descuentos;
    }

    public void setDescuentos(BigDecimal descuentos) {
        this.descuentos = descuentos;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public List<DetalleFactura> getDetalles() {
        return detalles;
    }

    public void setDetalles(List<DetalleFactura> detalles) {
        this.detalles = detalles;
    }

    public void addDetalle(DetalleFactura detalle) {
        this.detalles.add(detalle);
        recalcularTotales();
    }

    public void recalcularTotales() {
        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal descuentos = BigDecimal.ZERO;

        for (DetalleFactura detalle : detalles) {
            subtotal = subtotal.add(detalle.getSubtotal());
            descuentos = descuentos.add(detalle.getDescuento());
        }

        this.subtotal = subtotal;
        this.descuentos = descuentos;
        this.total = subtotal.subtract(descuentos).add(impuestos);
    }

    @Override
    public String toString() {
        return numeroFactura + " - " + clienteNombre + " (" + estado + ")";
    }
}