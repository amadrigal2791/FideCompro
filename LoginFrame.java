import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class LoginFrame extends JFrame {
    private JTextField txtNombreUsuario;
    private JPasswordField txtContrasena;
    private JButton btnIniciarSesion;
    private JLabel lblMensaje;
    private UsuarioDAO usuarioDAO;

    public LoginFrame() {
        usuarioDAO = new UsuarioDAO();
        initComponents();
    }

    private void initComponents() {
        setTitle("Inicio de Sesion - Sistema Fidecompro");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 300);
        setLocationRelativeTo(null);
        setLayout(new GridLayout(4, 2, 10, 10));

        JLabel lblNombreUsuario = new JLabel("Nombre de usuario:");
        txtNombreUsuario = new JTextField();
        JLabel lblContrasena = new JLabel("Contrasena:");
        txtContrasena = new JPasswordField();
        btnIniciarSesion = new JButton("Iniciar Sesion");
        lblMensaje = new JLabel();

        add(lblNombreUsuario);
        add(txtNombreUsuario);
        add(lblContrasena);
        add(txtContrasena);
        add(new JLabel()); // Espacio vacio
        add(btnIniciarSesion);
        add(new JLabel()); // Espacio vacio
        add(lblMensaje);

        btnIniciarSesion.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                iniciarSesion();
            }
        });

        txtContrasena.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                iniciarSesion();
            }
        });
    }

    private void iniciarSesion() {
        String nombreUsuario = txtNombreUsuario.getText().trim();
        String contrasena = new String(txtContrasena.getPassword()).trim();

        if (nombreUsuario.isEmpty() || contrasena.isEmpty()) {
            lblMensaje.setText("Ingrese usuario y contrasena");
            return;
        }

        try {
            Usuario usuario = usuarioDAO.autenticar(nombreUsuario, contrasena);
            if (usuario != null) {
                dispose();
                new MainFrame(usuario).setVisible(true);
            } else {
                lblMensaje.setText("Usuario o contrasena incorrectos");
            }
        } catch (DatabaseException ex) {
            lblMensaje.setText("Error: " + ex.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new LoginFrame().setVisible(true);
            }
        });
    }
}