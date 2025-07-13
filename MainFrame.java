import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MainFrame extends JFrame {
    private Usuario usuario;
    private JMenuBar menuBar;
    private JMenu menuClientes;
    private JMenu menuProductos;
    private JMenu menuFacturas;
    private JMenu menuInventario;
    private JMenu menuUsuarios;

    public MainFrame(Usuario usuario) {
        this.usuario = usuario;
        initComponents();
    }

    private void initComponents() {
        setTitle("Sistema Fidecompro - " + usuario.getNombreCompleto());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);

        menuBar = new JMenuBar();
        menuClientes = new JMenu("Clientes");
        menuProductos = new JMenu("Productos");
        menuFacturas = new JMenu("Facturas");
        menuInventario = new JMenu("Inventario");
        menuUsuarios = new JMenu("Usuarios");

        // Menu Clientes
        JMenuItem itemRegistrarCliente = new JMenuItem("Registrar Cliente");
        JMenuItem itemConsultarClientes = new JMenuItem("Consultar Clientes");
        menuClientes.add(itemRegistrarCliente);
        menuClientes.add(itemConsultarClientes);

        // Menu Productos
        JMenuItem itemRegistrarProducto = new JMenuItem("Registrar Producto");
        JMenuItem itemConsultarProductos = new JMenuItem("Consultar Productos");
        menuProductos.add(itemRegistrarProducto);
        menuProductos.add(itemConsultarProductos);

        // Menu Facturas
        JMenuItem itemCrearFactura = new JMenuItem("Crear Factura");
        JMenuItem itemConsultarFacturas = new JMenuItem("Consultar Facturas");
        menuFacturas.add(itemCrearFactura);
        menuFacturas.add(itemConsultarFacturas);

        // Menu Inventario
        JMenuItem itemMovimientosInventario = new JMenuItem("Movimientos de Inventario");
        menuInventario.add(itemMovimientosInventario);

        // Menu Usuarios (solo para administradores)
        if (usuario.getRol().equals("ADMINISTRADOR")) {
            JMenuItem itemRegistrarUsuario = new JMenuItem("Registrar Usuario");
            JMenuItem itemConsultarUsuarios = new JMenuItem("Consultar Usuarios");
            menuUsuarios.add(itemRegistrarUsuario);
            menuUsuarios.add(itemConsultarUsuarios);
        }

        menuBar.add(menuClientes);
        menuBar.add(menuProductos);
        menuBar.add(menuFacturas);
        menuBar.add(menuInventario);
        if (usuario.getRol().equals("ADMINISTRADOR")) {
            menuBar.add(menuUsuarios);
        }

        setJMenuBar(menuBar);

        // TODO: Agregar ActionListeners para cada JMenuItem
    }
}