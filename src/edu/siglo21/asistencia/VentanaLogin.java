package edu.siglo21.asistencia;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class VentanaLogin extends JFrame {

    private JTextField txtUsuario;
    private JPasswordField txtPassword;
    private JButton btnIngresar;

    private static final String URL = "jdbc:mysql://localhost:3306/control_asistencia_db?useSSL=true&trustServerCertificate=true&allowPublicKeyRetrieval=true";
    private static final String USER = "root"; 
    private static final String PASS = "abc123456789"; 

    private final Color COLOR_BG = new Color(240, 244, 248);       
    private final Color COLOR_CARD = new Color(255, 255, 255);    
    private final Color COLOR_PRIMARY = new Color(26, 86, 219);   
    private final Color COLOR_PRIMARY_HOVER = new Color(30, 66, 159);
    private final Color COLOR_TEXT = new Color(17, 24, 39);       
    private final Color COLOR_BORDER = new Color(229, 231, 235);

    public VentanaLogin() {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception e) {}

        setTitle("Acceso al Sistema");
        setSize(400, 480);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel panelPrincipal = new JPanel(new BorderLayout());
        panelPrincipal.setBackground(COLOR_BG);
        panelPrincipal.setBorder(new EmptyBorder(30, 30, 30, 30));

        JPanel panelHeader = new JPanel(new GridLayout(2, 1, 5, 5));
        panelHeader.setBackground(COLOR_BG);
        
        JLabel lblTitulo = new JLabel("¡Bienvenido!", JLabel.CENTER);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblTitulo.setForeground(COLOR_TEXT);
        
        JLabel lblSubtitulo = new JLabel("Control de Asistencia Profesional", JLabel.CENTER);
        lblSubtitulo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblSubtitulo.setForeground(new Color(107, 114, 128));
        
        panelHeader.add(lblTitulo);
        panelHeader.add(lblSubtitulo);
        panelPrincipal.add(panelHeader, BorderLayout.NORTH);

        JPanel panelForm = new JPanel(new GridBagLayout());
        panelForm.setBackground(COLOR_CARD);
        panelForm.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(COLOR_BORDER, 1),
            new EmptyBorder(25, 20, 25, 20)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 0, 8, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;

        JLabel lblUser = new JLabel("Usuario:");
        lblUser.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblUser.setForeground(COLOR_TEXT);
        txtUsuario = new JTextField();
        estilarTextField(txtUsuario);

        JLabel lblPass = new JLabel("Contraseña:");
        lblPass.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblPass.setForeground(COLOR_TEXT);
        txtPassword = new JPasswordField();
        estilarTextField(txtPassword);

        btnIngresar = new JButton("Iniciar Sesión");
        estilarBoton(btnIngresar, COLOR_PRIMARY, COLOR_PRIMARY_HOVER);

        gbc.gridy = 0; panelForm.add(lblUser, gbc);
        gbc.gridy = 1; panelForm.add(txtUsuario, gbc);
        gbc.gridy = 2; panelForm.add(lblPass, gbc);
        gbc.gridy = 3; panelForm.add(txtPassword, gbc);
        
        gbc.gridy = 4; 
        gbc.insets = new Insets(25, 0, 0, 0);
        gbc.ipady = 15;
        panelForm.add(btnIngresar, gbc);
        gbc.ipady = 0;

        panelPrincipal.add(panelForm, BorderLayout.CENTER);
        setContentPane(panelPrincipal);

        btnIngresar.addActionListener(e -> procesarLogin());
        txtPassword.addActionListener(e -> procesarLogin()); 
    }

    private void procesarLogin() {
        String username = txtUsuario.getText().trim();
        String password = new String(txtPassword.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Por favor ingrese sus credenciales corporativas.", "Campos Vacíos", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String sql = "SELECT id_usuario FROM usuarios WHERE username = ? AND password = ?";
        
        try (Connection con = DriverManager.getConnection(URL, USER, PASS);
             PreparedStatement stmt = con.prepareStatement(sql)) {
            
            stmt.setString(1, username);
            stmt.setString(2, password);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    this.dispose(); 
                    SwingUtilities.invokeLater(() -> new AppAsistencia().setVisible(true));
                } else {
                    JOptionPane.showMessageDialog(this, "Usuario o contraseña incorrectos.", "Acceso Denegado", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error de conexión con el servidor MySQL:\n" + ex.getMessage(), "Error Crítico", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void estilarTextField(JTextField field) {
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setPreferredSize(new Dimension(300, 38));
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(COLOR_BORDER, 1),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
    }

    private void estilarBoton(JButton boton, Color normal, Color hover) {
        boton.setFont(new Font("Segoe UI", Font.BOLD, 13)); 
        boton.setBackground(normal);
        boton.setForeground(Color.WHITE);
        boton.setFocusPainted(false);
        boton.setPreferredSize(new Dimension(300, 48)); 
        boton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        boton.setBorder(BorderFactory.createEmptyBorder());
        
        boton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) { boton.setBackground(hover); }
            public void mouseExited(java.awt.event.MouseEvent e) { boton.setBackground(normal); }
        });
    }
    

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new VentanaLogin().setVisible(true));
    }
}
