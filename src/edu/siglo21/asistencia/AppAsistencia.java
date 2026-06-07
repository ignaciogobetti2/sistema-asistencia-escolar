package edu.siglo21.asistencia;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.time.LocalDateTime;

public class AppAsistencia extends JFrame {
    
    // Componentes de la Interfaz Gráfica
    private JComboBox<String> comboCursos;
    private JComboBox<String> comboMaterias;
    private JTable tablaAlumnos;
    private DefaultTableModel modeloTabla;
    private JButton btnGuardar;

    // Configuración de conexión a MySQL
    private static final String URL = "jdbc:mysql://localhost:3306/control_asistencia_db?useSSL=true&trustServerCertificate=true&allowPublicKeyRetrieval=true";
    private static final String USER = "root"; 
    private static final String PASS = "abc123456789"; // Cambiala si tu MySQL tiene contraseña

    public AppAsistencia() {
        // Configuración básica de la ventana principal
        setTitle("Sistema de Gestión de Asistencia Escolar");
        setSize(700, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // 1. PANEL SUPERIOR: Filtros de selección
        JPanel panelSuperior = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        comboCursos = new JComboBox<>();
        comboMaterias = new JComboBox<>();
        
        panelSuperior.add(new JLabel("Curso:"));
        panelSuperior.add(comboCursos);
        panelSuperior.add(new JLabel("Materia:"));
        panelSuperior.add(comboMaterias);
        add(panelSuperior, BorderLayout.NORTH);

        // 2. PANEL CENTRAL: Tabla dinámica de estudiantes
        // Columnas: ID, Apellido, Nombre, DNI, ¿Presente? (Booleano para renderizar Checkbox)
        String[] columnas = {"ID", "Apellido", "Nombre", "DNI", "Presente"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return columnIndex == 4 ? Boolean.class : Object.class;
            }
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 4; // Solo la columna del checkbox es editable
            }
        };
        tablaAlumnos = new JTable(modeloTabla);
        add(new JScrollPane(tablaAlumnos), BorderLayout.CENTER);

        // 3. PANEL INFERIOR: Botón de acción principal
        JPanel panelInferior = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        btnGuardar = new JButton("Guardar Asistencia");
        panelInferior.add(btnGuardar);
        add(panelInferior, BorderLayout.SOUTH);

        // Cargar datos iniciales desde MySQL y configurar eventos
        conectarYAsignarComponentes();
        configurarEventos();
    }

    private Connection obtenerConexion() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }

    private void conectarYAsignarComponentes() {
        try (Connection con = obtenerConexion()) {
            // Cargar Cursos en el selector
            Statement stmtCursos = con.createStatement();
            ResultSet rsCursos = stmtCursos.executeQuery("SELECT id_curso, nombre_curso, division FROM cursos");
            while (rsCursos.next()) {
                String item = rsCursos.getInt("id_curso") + " - " + rsCursos.getString("nombre_curso") + " " + rsCursos.getString("division");
                comboCursos.addItem(item);
            }

            // Cargar Materias en el selector
            Statement stmtMaterias = con.createStatement();
            ResultSet rsMaterias = stmtMaterias.executeQuery("SELECT id_materia, nombre_materia FROM materias");
            while (rsMaterias.next()) {
                String item = rsMaterias.getInt("id_materia") + " - " + rsMaterias.getString("nombre_materia");
                comboMaterias.addItem(item);
            }

            // Forzar la carga inicial de alumnos según el curso seleccionado por defecto
            actualizarListaAlumnos();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Fallo al conectar con la base de datos: " + e.getMessage(), "Error de Conexión", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void actualizarListaAlumnos() {
        modeloTabla.setRowCount(0); // Limpiar registros anteriores de la interfaz
        String cursoSeleccionado = (String) comboCursos.getSelectedItem();
        if (cursoSeleccionado == null) return;

        // Extraer el número de ID del string del combobox (ej: "1 - 5to Año" extrae el 1)
        int idCurso = Integer.parseInt(cursoSeleccionado.split(" - ")[0]);

        String sql = "SELECT id_alumno, apellido, nombre, dni FROM alumnos WHERE id_curso = ? AND estado = 'ACTIVO'";
        try (Connection con = obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setInt(1, idCurso);
            ResultSet rs = ps.executeQuery();
            
            while (rs.next()) {
                // Por defecto, se inicializan todos los alumnos con el Checkbox desmarcado (false = Ausente)
                modeloTabla.addRow(new Object[]{
                    rs.getInt("id_alumno"),
                    rs.getString("apellido"),
                    rs.getString("nombre"),
                    rs.getString("dni"),
                    false 
                });
            }
        } catch (SQLException e) {
            System.err.println("Error al cargar alumnos: " + e.getMessage());
        }
    }

    private void configurarEventos() {
        // Evento: Al cambiar de curso en el combobox, se actualiza la grilla de alumnos automáticamente
        comboCursos.addActionListener(e -> actualizarListaAlumnos());

        // Evento: Al hacer clic en Guardar, se itera la tabla e impacta en la BD
        btnGuardar.addActionListener(e -> {
            String materiaSeleccionada = (String) comboMaterias.getSelectedItem();
            if (materiaSeleccionada == null) return;

            int idMateria = Integer.parseInt(materiaSeleccionada.split(" - ")[0]);
            int idUsuarioAuditor = 1; // ID de usuario administrativo ficticio para cumplir la integridad foránea
            
            String sqlInsert = "INSERT INTO registros_asistencia (id_alumno, id_materia, id_usuario, fecha_hora, estado_asistencia) VALUES (?, ?, ?, ?, ?)";
            int registrosGuardados = 0;

            try (Connection con = obtenerConexion();
                 PreparedStatement ps = con.prepareStatement(sqlInsert)) {
                
                // Recorrer secuencialmente cada una de las filas que tiene la tabla Swing
                for (int i = 0; i < modeloTabla.getRowCount(); i++) {
                    int idAlumno = (int) modeloTabla.getValueAt(i, 0);
                    boolean estaPresente = (boolean) modeloTabla.getValueAt(i, 4);
                    String estadoString = estaPresente ? "PRESENTE" : "AUSENTE";

                    ps.setInt(1, idAlumno);
                    ps.setInt(2, idMateria);
                    ps.setInt(3, idUsuarioAuditor);
                    ps.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
                    ps.setString(5, estadoString);
                    
                    ps.executeUpdate();
                    registrosGuardados++;
                }

                JOptionPane.showMessageDialog(this, "Se procesaron con éxito " + registrosGuardados + " registros de asistencia.", "Operación Exitosa", JOptionPane.INFORMATION_MESSAGE);

            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error de persistencia al guardar datos: " + ex.getMessage(), "Error Crítico", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    // Método Main: Punto de entrada único para ejecutar la aplicación directamente
    public static void main(String[] args) {
        // Asegurar que la interfaz gráfica se dibuje de forma segura en el hilo de eventos de Swing
        SwingUtilities.invokeLater(() -> {
            new AppAsistencia().setVisible(true);
        });
    }
}