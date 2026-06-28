package edu.siglo21.asistencia;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AppAsistencia extends JFrame {
    
    private JComboBox<String> comboCursos;
    private JComboBox<String> comboMaterias;
    private JTable tablaAlumnos;
    private DefaultTableModel modeloTabla;
    private JButton btnGuardar;
    private JButton btnEliminarCursoAsis;
    private JButton btnEliminarMateriaAsis;

    private JTextField txtIdAlumnoAlta; 
    private JTextField txtNombreAlumno;
    private JTextField txtApellidoAlumno;
    private JTextField txtDniAlumno;
    private JComboBox<String> comboCursosAlta;
    private JButton btnAltaAlumno;
    private JButton btnEliminarAlumno;

    private JTextField txtNombreCurso;
    private JTextField txtDivisionCurso;
    private JButton btnAltaCurso;
    private JTextField txtNombreMateria;
    private JButton btnAltaMateria;

    private JTable tablaHistorial;
    private DefaultTableModel modeloHistorial;
    private JButton btnVaciarHistorial;

    private HashMap<String, Integer> mapaCursos = new HashMap<>();
    private HashMap<String, Integer> mapaMaterias = new HashMap<>();

    private static final String URL = "jdbc:mysql://localhost:3306/control_asistencia_db?useSSL=true&trustServerCertificate=true&allowPublicKeyRetrieval=true";
    private static final String USER = "root"; 
    private static final String PASS = "abc123456789"; 

    private final AsistenciaDAO asistenciaDAO = new AsistenciaDAOImpl();
    
    private final Color COLOR_BG = new Color(240, 244, 248);       
    private final Color COLOR_CARD = new Color(255, 255, 255);    
    private final Color COLOR_PRIMARY = new Color(26, 86, 219);   
    private final Color COLOR_PRIMARY_HOVER = new Color(30, 66, 159);
    private final Color COLOR_DANGER = new Color(220, 38, 38);
    private final Color COLOR_DANGER_HOVER = new Color(185, 28, 28);
    private final Color COLOR_TEXT = new Color(17, 24, 39);       
    private final Color COLOR_BORDER = new Color(229, 231, 235);   

    public AppAsistencia() {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception e) {}

        setTitle("Plataforma de Control de Asistencia Profesional");
        setSize(1000, 750);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        JTabbedPane panelSolapas = new JTabbedPane();
        panelSolapas.setFont(new Font("Segoe UI", Font.BOLD, 13));
        
        panelSolapas.addTab("  Registrar Asistencia  ", crearPanelAsistencia());
        panelSolapas.addTab("  Administrar Alumnos  ", crearPanelAlumnos());
        panelSolapas.addTab("  Configurar Estructuras  ", crearPanelEstructuras());
        panelSolapas.addTab("  Historial de Auditoría  ", crearPanelHistorial());
        
        panelSolapas.addChangeListener(e -> {
            int index = panelSolapas.getSelectedIndex();
            if (index == 0 || index == 1) {
                refrescarCombosCursosYMaterias();
            } else if (index == 3) {
                cargarHistorialEnTabla();
            }
        });

        setContentPane(panelSolapas);
        refrescarCombosCursosYMaterias();
        configurarEventos();
    }

    private JPanel crearPanelAsistencia() {
        JPanel panel = new JPanel(new BorderLayout(20, 20));
        panel.setBorder(new EmptyBorder(25, 25, 25, 25));
        panel.setBackground(COLOR_BG);

        JPanel panelHeader = new JPanel(new BorderLayout(5, 5));
        panelHeader.setBackground(COLOR_BG);
        JLabel lblTitulo = new JLabel("Toma de Asistencia Diaria");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitulo.setForeground(COLOR_TEXT);
        panelHeader.add(lblTitulo, BorderLayout.NORTH);
        panel.add(panelHeader, BorderLayout.NORTH);

        JPanel panelFiltros = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 15));
        panelFiltros.setBackground(COLOR_CARD);
        panelFiltros.setBorder(BorderFactory.createCompoundBorder(crearBordeSeccion(" Parámetros de Registro "), new EmptyBorder(5, 10, 5, 10)));
        
        comboCursos = new JComboBox<>();
        comboMaterias = new JComboBox<>();
        estilarCombo(comboCursos);
        estilarCombo(comboMaterias);
        
        btnEliminarCursoAsis = new JButton("Borrar Curso");
        estilarBotonChico(btnEliminarCursoAsis, COLOR_DANGER, COLOR_DANGER_HOVER);
        btnEliminarMateriaAsis = new JButton("Borrar Materia");
        estilarBotonChico(btnEliminarMateriaAsis, COLOR_DANGER, COLOR_DANGER_HOVER);

        panelFiltros.add(new JLabel("Curso:")); panelFiltros.add(comboCursos); panelFiltros.add(btnEliminarCursoAsis);
        panelFiltros.add(Box.createHorizontalStrut(20));
        panelFiltros.add(new JLabel("Materia:")); panelFiltros.add(comboMaterias); panelFiltros.add(btnEliminarMateriaAsis);

        String[] columnas = {"ID Alumno", "Apellido", "Nombre", "DNI", "Estado Presencia"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override public Class<?> getColumnClass(int col) { return col == 4 ? Boolean.class : Object.class; }
            @Override public boolean isCellEditable(int r, int c) { return c == 4; }
        };
        
        tablaAlumnos = new JTable(modeloTabla);
        estilarTabla(tablaAlumnos);

        DefaultTableCellRenderer renderCentrado = new DefaultTableCellRenderer();
        renderCentrado.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < 4; i++) {
            tablaAlumnos.getColumnModel().getColumn(i).setCellRenderer(renderCentrado);
        }

        JScrollPane scrollPane = new JScrollPane(tablaAlumnos);
        scrollPane.setBorder(BorderFactory.createLineBorder(COLOR_BORDER, 1));
        scrollPane.getViewport().setBackground(COLOR_CARD);

        JPanel centro = new JPanel(new BorderLayout(0, 20));
        centro.setBackground(COLOR_BG);
        centro.add(panelFiltros, BorderLayout.NORTH);
        centro.add(scrollPane, BorderLayout.CENTER);
        panel.add(centro, BorderLayout.CENTER);

        JPanel panelInferior = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        panelInferior.setBackground(COLOR_BG);
        btnGuardar = new JButton("Guardar Registro Abierto");
        estilarBoton(btnGuardar, COLOR_PRIMARY, COLOR_PRIMARY_HOVER);
        panelInferior.add(btnGuardar);
        panel.add(panelInferior, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel crearPanelAlumnos() {
        JPanel panel = new JPanel(new BorderLayout(20, 20));
        panel.setBorder(new EmptyBorder(25, 25, 25, 25));
        panel.setBackground(COLOR_BG);

        JPanel panelHeader = new JPanel(new BorderLayout(5, 5));
        panelHeader.setBackground(COLOR_BG);
        JLabel lblTitulo = new JLabel("Matriculación y Baja de Alumnos");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitulo.setForeground(COLOR_TEXT);
        panelHeader.add(lblTitulo, BorderLayout.NORTH);
        panel.add(panelHeader, BorderLayout.NORTH);

        JPanel panelForm = new JPanel(new GridBagLayout());
        panelForm.setBackground(COLOR_CARD);
        panelForm.setBorder(BorderFactory.createCompoundBorder(crearBordeSeccion(" Gestión de Estudiantes "), new EmptyBorder(20, 20, 20, 20)));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 15, 8, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        txtIdAlumnoAlta = new JTextField(20); 
        txtApellidoAlumno = new JTextField(20); 
        txtNombreAlumno = new JTextField(20); 
        txtDniAlumno = new JTextField(20);
        comboCursosAlta = new JComboBox<>();
        
        estilarTextField(txtIdAlumnoAlta);
        estilarTextField(txtApellidoAlumno); 
        estilarTextField(txtNombreAlumno); 
        estilarTextField(txtDniAlumno);
        estilarCombo(comboCursosAlta);

        gbc.gridx = 0; gbc.gridy = 0; panelForm.add(new JLabel("ID Alumno:"), gbc);
        gbc.gridx = 1; panelForm.add(txtIdAlumnoAlta, gbc);
        gbc.gridx = 0; gbc.gridy = 1; panelForm.add(new JLabel("Apellido/s:"), gbc);
        gbc.gridx = 1; panelForm.add(txtApellidoAlumno, gbc);
        gbc.gridx = 0; gbc.gridy = 2; panelForm.add(new JLabel("Nombre/s:"), gbc);
        gbc.gridx = 1; panelForm.add(txtNombreAlumno, gbc);
        gbc.gridx = 0; gbc.gridy = 3; panelForm.add(new JLabel("Documento (DNI):"), gbc);
        gbc.gridx = 1; panelForm.add(txtDniAlumno, gbc);
        gbc.gridx = 0; gbc.gridy = 4; panelForm.add(new JLabel("Asignar Curso:"), gbc);
        gbc.gridx = 1; panelForm.add(comboCursosAlta, gbc);

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        panelBotones.setBackground(COLOR_CARD);
        
        btnEliminarAlumno = new JButton("Dar de Baja por ID");
        estilarBoton(btnEliminarAlumno, COLOR_DANGER, COLOR_DANGER_HOVER);
        btnAltaAlumno = new JButton("Registrar y Matricular");
        estilarBoton(btnAltaAlumno, COLOR_PRIMARY, COLOR_PRIMARY_HOVER);
        
        panelBotones.add(btnEliminarAlumno);
        panelBotones.add(btnAltaAlumno);
        
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2;
        panelForm.add(panelBotones, gbc);

        panel.add(panelForm, BorderLayout.CENTER);
        return panel;
    }

    private JPanel crearPanelEstructuras() {
        JPanel panel = new JPanel(new GridLayout(1, 2, 25, 0));
        panel.setBorder(new EmptyBorder(25, 25, 25, 25));
        panel.setBackground(COLOR_BG);

        JPanel panelCursos = new JPanel(new GridBagLayout());
        panelCursos.setBackground(COLOR_CARD);
        panelCursos.setBorder(BorderFactory.createCompoundBorder(crearBordeSeccion(" Nuevo Curso / División "), new EmptyBorder(20, 20, 20, 20)));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10); gbc.fill = GridBagConstraints.HORIZONTAL;

        txtNombreCurso = new JTextField(15); 
        txtDivisionCurso = new JTextField(15);
        estilarTextField(txtNombreCurso); 
        estilarTextField(txtDivisionCurso);
        btnAltaCurso = new JButton("Crear Curso"); 
        estilarBoton(btnAltaCurso, COLOR_PRIMARY, COLOR_PRIMARY_HOVER);

        gbc.gridx = 0; gbc.gridy = 0; panelCursos.add(new JLabel("Nombre del Año:"), gbc);
        gbc.gridx = 1; panelCursos.add(txtNombreCurso, gbc);
        gbc.gridx = 0; gbc.gridy = 1; panelCursos.add(new JLabel("División:"), gbc);
        gbc.gridx = 1; panelCursos.add(txtDivisionCurso, gbc);
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2; panelCursos.add(btnAltaCurso, gbc);

        JPanel panelMaterias = new JPanel(new GridBagLayout());
        panelMaterias.setBackground(COLOR_CARD);
        panelMaterias.setBorder(BorderFactory.createCompoundBorder(crearBordeSeccion(" Nueva Asignatura "), new EmptyBorder(20, 20, 20, 20)));

        txtNombreMateria = new JTextField(15); 
        estilarTextField(txtNombreMateria);
        btnAltaMateria = new JButton("Crear Materia"); 
        estilarBoton(btnAltaMateria, COLOR_PRIMARY, COLOR_PRIMARY_HOVER);

        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 1; panelMaterias.add(new JLabel("Nombre Materia:"), gbc);
        gbc.gridx = 1; panelMaterias.add(txtNombreMateria, gbc);
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 2; panelMaterias.add(btnAltaMateria, gbc);

        panel.add(panelCursos);
        panel.add(panelMaterias);
        return panel;
    }

    private JPanel crearPanelHistorial() {
        JPanel panel = new JPanel(new BorderLayout(20, 20));
        panel.setBorder(new EmptyBorder(25, 25, 25, 25));
        panel.setBackground(COLOR_BG);

        JPanel panelHeader = new JPanel(new BorderLayout(5, 5));
        panelHeader.setBackground(COLOR_BG);
        JLabel lblTitulo = new JLabel("Auditoría General de Registros");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitulo.setForeground(COLOR_TEXT);
        panelHeader.add(lblTitulo, BorderLayout.NORTH);
        panel.add(panelHeader, BorderLayout.NORTH);

        String[] columnasHistorial = {"ID Registro", "Estudiante", "Asignatura", "Fecha / Hora", "Condición"};
        modeloHistorial = new DefaultTableModel(columnasHistorial, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        tablaHistorial = new JTable(modeloHistorial);
        estilarTabla(tablaHistorial);

        tablaHistorial.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object val, boolean isSel, boolean hasFocus, int r, int c) {
                Component comp = super.getTableCellRendererComponent(table, val, isSel, hasFocus, r, c);
                
                if (!isSel) {
                    try {
                        String fechaActual = table.getValueAt(r, 3).toString();
                        String grupoActual = fechaActual.length() > 16 ? fechaActual.substring(0, 16) : fechaActual;
                        
                        int hashGrupo = grupoActual.hashCode();
                        if (hashGrupo % 2 == 0) {
                            comp.setBackground(COLOR_CARD);
                        } else {
                            comp.setBackground(new Color(242, 245, 249)); 
                        }
                    } catch (Exception e) {
                        comp.setBackground(COLOR_CARD);
                    }
                }
                
                comp.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                comp.setForeground(COLOR_TEXT);

                setHorizontalAlignment(JLabel.CENTER);

                if (table.getColumnName(c).equals("ID Registro") || table.getColumnName(c).equals("Fecha / Hora")) {
                    try {
                        String estadoFila = table.getValueAt(r, 4).toString();
                        if (estadoFila.equals("PRESENTE")) {
                            comp.setForeground(new Color(22, 163, 74)); 
                            comp.setFont(new Font("Segoe UI", Font.BOLD, 14));
                        } else {
                            comp.setForeground(COLOR_DANGER); 
                            comp.setFont(new Font("Segoe UI", Font.BOLD, 14));
                        }
                    } catch (Exception e) {}

                } else if (table.getColumnName(c).equals("Condición")) {
                    if (val != null && val.toString().equals("PRESENTE")) {
                        comp.setForeground(new Color(22, 163, 74)); 
                        comp.setFont(new Font("Segoe UI", Font.BOLD, 13));
                    } else {
                        comp.setForeground(COLOR_DANGER); 
                        comp.setFont(new Font("Segoe UI", Font.BOLD, 13));
                    }
                }

                if (r < table.getRowCount() - 1) {
                    try {
                        String fechaSiguiente = table.getValueAt(r + 1, 3).toString();
                        String grupoActual = table.getValueAt(r, 3).toString().substring(0, 16);
                        String grupoSiguiente = fechaSiguiente.substring(0, 16);
                        
                        if (!grupoActual.equals(grupoSiguiente)) {
                            ((JComponent) comp).setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(180, 190, 205)));
                        } else {
                            ((JComponent) comp).setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, COLOR_BORDER));
                        }
                    } catch (Exception e) {
                        ((JComponent) comp).setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, COLOR_BORDER));
                    }
                } else {
                    ((JComponent) comp).setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, COLOR_BORDER));
                }

                return comp;
            }
        });

        JScrollPane scrollPane = new JScrollPane(tablaHistorial);
        scrollPane.setBorder(BorderFactory.createLineBorder(COLOR_BORDER, 1));
        scrollPane.getViewport().setBackground(COLOR_CARD);
        panel.add(scrollPane, BorderLayout.CENTER);

        JPanel panelInferior = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        panelInferior.setBackground(COLOR_BG);
        btnVaciarHistorial = new JButton("Limpiar Historial Completo");
        estilarBoton(btnVaciarHistorial, COLOR_DANGER, COLOR_DANGER_HOVER);
        panelInferior.add(btnVaciarHistorial);
        panel.add(panelInferior, BorderLayout.SOUTH);

        return panel;
    }

    private TitledBorder crearBordeSeccion(String titulo) {
        TitledBorder borde = BorderFactory.createTitledBorder(BorderFactory.createLineBorder(COLOR_BORDER, 1), titulo);
        borde.setTitleFont(new Font("Segoe UI", Font.BOLD, 12));
        borde.setTitleColor(COLOR_PRIMARY);
        return borde;
    }

    private void estilarTabla(JTable tabla) {
        tabla.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tabla.setRowHeight(40);
        tabla.setGridColor(COLOR_BORDER);
        tabla.setShowVerticalLines(false);
        tabla.setSelectionBackground(new Color(239, 246, 255));
        tabla.setSelectionForeground(COLOR_TEXT);

        javax.swing.table.JTableHeader header = tabla.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 13));
        header.setBackground(new Color(248, 250, 252));
        header.setForeground(new Color(71, 85, 105));
        header.setPreferredSize(new Dimension(100, 45));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, COLOR_BORDER));
    }

    private void estilarCombo(JComboBox<String> combo) {
        combo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        combo.setBackground(COLOR_CARD);
        combo.setForeground(COLOR_TEXT);
    }

    private void estilarTextField(JTextField field) {
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(COLOR_BORDER, 1), BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
    }

    private void estilarBoton(JButton boton, Color normal, Color hover) {
        boton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        boton.setBackground(normal);
        boton.setForeground(Color.WHITE);
        boton.setFocusPainted(false);
        boton.setPreferredSize(new Dimension(210, 45));
        boton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        boton.setBorder(BorderFactory.createEmptyBorder());
        
        boton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) { boton.setBackground(hover); }
            public void mouseExited(java.awt.event.MouseEvent e) { boton.setBackground(normal); }
        });
    }

    private void estilarBotonChico(JButton boton, Color normal, Color hover) {
        boton.setFont(new Font("Segoe UI", Font.BOLD, 11));
        boton.setBackground(normal);
        boton.setForeground(Color.WHITE);
        boton.setFocusPainted(false);
        boton.setPreferredSize(new Dimension(110, 32));
        boton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        boton.setBorder(BorderFactory.createEmptyBorder());
        boton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) { boton.setBackground(hover); }
            public void mouseExited(java.awt.event.MouseEvent e) { boton.setBackground(normal); }
        });
    }

    private Connection obtenerConexion() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }

    private void refrescarCombosCursosYMaterias() {
        String cursoSeleccionado = (String) comboCursos.getSelectedItem();
        String materiaSeleccionada = (String) comboMaterias.getSelectedItem();

        comboCursos.removeAllItems();
        comboMaterias.removeAllItems();
        comboCursosAlta.removeAllItems();
        mapaCursos.clear();
        mapaMaterias.clear();

        try (Connection con = obtenerConexion(); Statement st = con.createStatement()) {
            ResultSet rsCursos = st.executeQuery("SELECT id_curso, nombre_curso, division FROM cursos");
            while (rsCursos.next()) {
                int id = rsCursos.getInt("id_curso");
                String visual = rsCursos.getString("nombre_curso") + " " + rsCursos.getString("division");
                
                comboCursos.addItem(visual);
                comboCursosAlta.addItem(visual);
                mapaCursos.put(visual, id);
            }

            ResultSet rsMaterias = st.executeQuery("SELECT id_materia, nombre_materia FROM materias");
            while (rsMaterias.next()) {
                int id = rsMaterias.getInt("id_materia");
                String visual = rsMaterias.getString("nombre_materia");
                
                comboMaterias.addItem(visual);
                mapaMaterias.put(visual, id);
            }

            if (cursoSeleccionado != null) comboCursos.setSelectedItem(cursoSeleccionado);
            if (materiaSeleccionada != null) comboMaterias.setSelectedItem(materiaSeleccionada);

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error de sincronización: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void actualizarListaAlumnos() {
        modeloTabla.setRowCount(0);
        String seleccionado = (String) comboCursos.getSelectedItem();
        if (seleccionado == null || !mapaCursos.containsKey(seleccionado)) return;

        int idCurso = mapaCursos.get(seleccionado);
        try {
            ArrayList<Alumno> lista = asistenciaDAO.obtenerAlumnosPorCurso(idCurso);
            for (Alumno al : lista) {
                modeloTabla.addRow(new Object[]{al.getId(), al.getApellido(), al.getNombre(), al.getDni(), al.isPresente()});
            }
        } catch (AsistenciaException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cargarHistorialEnTabla() {
        modeloHistorial.setRowCount(0);
        try {
            ArrayList<Object[]> datos = asistenciaDAO.obtenerHistorialAsistencias();
            for (Object[] fila : datos) { 
                modeloHistorial.addRow(fila); 
            }
        } catch (AsistenciaException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void configurarEventos() {
        comboCursos.addActionListener(e -> actualizarListaAlumnos());

        btnEliminarCursoAsis.addActionListener(e -> {
            String sel = (String) comboCursos.getSelectedItem();
            if (sel == null || !mapaCursos.containsKey(sel)) return;
            int id = mapaCursos.get(sel);
            int conf = JOptionPane.showConfirmDialog(this, "¿Seguro que desea eliminar este Curso?", "Confirmar Baja", JOptionPane.YES_NO_OPTION);
            if (conf == JOptionPane.YES_OPTION) {
                try {
                    if (asistenciaDAO.eliminarCurso(id)) {
                        JOptionPane.showMessageDialog(this, "Curso eliminado.");
                        refrescarCombosCursosYMaterias();
                        actualizarListaAlumnos();
                    }
                } catch (AsistenciaException ex) {
                    JOptionPane.showMessageDialog(this, ex.getMessage(), "Restricción de Datos", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        btnEliminarMateriaAsis.addActionListener(e -> {
            String sel = (String) comboMaterias.getSelectedItem();
            if (sel == null || !mapaMaterias.containsKey(sel)) return;
            int id = mapaMaterias.get(sel);
            int conf = JOptionPane.showConfirmDialog(this, "¿Seguro que desea eliminar esta Asignatura?", "Confirmar Baja", JOptionPane.YES_NO_OPTION);
            if (conf == JOptionPane.YES_OPTION) {
                try {
                    if (asistenciaDAO.eliminarMateria(id)) {
                        JOptionPane.showMessageDialog(this, "Materia eliminada.");
                        refrescarCombosCursosYMaterias();
                    }
                } catch (AsistenciaException ex) {
                    JOptionPane.showMessageDialog(this, ex.getMessage(), "Restricción de Datos", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        btnEliminarAlumno.addActionListener(e -> {
            String idInput = JOptionPane.showInputDialog(this, "Ingrese el ID del Alumno que desea dar de baja:", "Baja de Estudiante", JOptionPane.QUESTION_MESSAGE);
            if (idInput == null || idInput.trim().isEmpty()) return;

            try {
                int idAlumno = Integer.parseInt(idInput.trim());
                int conf = JOptionPane.showConfirmDialog(this, "¿Confirmar desvinculación completa del alumno ID " + idAlumno + "?", "Confirmar Baja", JOptionPane.YES_NO_OPTION);
                if (conf == JOptionPane.YES_OPTION) {
                    if (asistenciaDAO.eliminarAlumno(idAlumno)) {
                        JOptionPane.showMessageDialog(this, "Estudiante removido de la base de datos.");
                        actualizarListaAlumnos();
                    } else {
                        JOptionPane.showMessageDialog(this, "No se encontró ningún alumno con el ID provisto.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            } catch (NumberFormatException nfe) {
                JOptionPane.showMessageDialog(this, "Por favor, ingrese un número de ID válido.", "Formato Inválido", JOptionPane.ERROR_MESSAGE);
            } catch (AsistenciaException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnAltaCurso.addActionListener(e -> {
            String nom = txtNombreCurso.getText().trim();
            String div = txtDivisionCurso.getText().trim();
            if (nom.isEmpty() || div.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Complete los campos vacíos.", "Atención", JOptionPane.WARNING_MESSAGE);
                return;
            }
            try {
                if (asistenciaDAO.insertarCurso(nom, div)) {
                    JOptionPane.showMessageDialog(this, "Curso creado con éxito.");
                    txtNombreCurso.setText(""); txtDivisionCurso.setText("");
                    refrescarCombosCursosYMaterias();
                }
            } catch (AsistenciaException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnAltaMateria.addActionListener(e -> {
            String mat = txtNombreMateria.getText().trim();
            if (mat.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Ingrese el nombre de la asignatura.", "Atención", JOptionPane.WARNING_MESSAGE);
                return;
            }
            try {
                if (asistenciaDAO.insertarMateria(mat)) {
                    JOptionPane.showMessageDialog(this, "Asignatura creada.");
                    txtNombreMateria.setText("");
                    refrescarCombosCursosYMaterias();
                }
            } catch (AsistenciaException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        // REPARADO: Ahora extrae el rawId, lo parsea y envía los 5 parámetros correspondientes al DAO
        btnAltaAlumno.addActionListener(e -> {
            String rawId = txtIdAlumnoAlta.getText().trim();
            String ape = txtApellidoAlumno.getText().trim();
            String nom = txtNombreAlumno.getText().trim();
            String dni = txtDniAlumno.getText().trim();
            String cursoSel = (String) comboCursosAlta.getSelectedItem();

            if (rawId.isEmpty() || ape.isEmpty() || nom.isEmpty() || dni.isEmpty() || cursoSel == null) {
                JOptionPane.showMessageDialog(this, "Complete todos los campos de matrícula, incluyendo el ID.", "Atención", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            if (!mapaCursos.containsKey(cursoSel)) return;
            int idCurso = mapaCursos.get(cursoSel);
            
            try {
                int idManual = Integer.parseInt(rawId);
                if (asistenciaDAO.insertarAlumno(idManual, ape, nom, dni, idCurso)) {
                    JOptionPane.showMessageDialog(this, "Estudiante matriculado con éxito.");
                    txtIdAlumnoAlta.setText(""); txtApellidoAlumno.setText(""); txtNombreAlumno.setText(""); txtDniAlumno.setText("");
                    actualizarListaAlumnos();
                }
            } catch (NumberFormatException nfe) {
                JOptionPane.showMessageDialog(this, "El ID del alumno debe ser un valor puramente numérico.", "Formato Inválido", JOptionPane.ERROR_MESSAGE);
            } catch (AsistenciaException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnVaciarHistorial.addActionListener(e -> {
            int conf = JOptionPane.showConfirmDialog(this, "ATENCIÓN: Se eliminarán todos los registros históricos.\n¿Desea continuar?", "Advertencia Crítica", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (conf == JOptionPane.YES_OPTION) {
                try {
                    if (asistenciaDAO.vaciarHistorial()) {
                        JOptionPane.showMessageDialog(this, "Historial completamente vaciado.");
                        cargarHistorialEnTabla();
                    }
                } catch (AsistenciaException ex) {
                    JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        btnGuardar.addActionListener(e -> {
            String matSel = (String) comboMaterias.getSelectedItem();
            if (matSel == null || !mapaMaterias.containsKey(matSel)) return;

            int idMateria = mapaMaterias.get(matSel);
            ArrayList<Alumno> lista = new ArrayList<>();
            
            for (int i = 0; i < modeloTabla.getRowCount(); i++) {
                Alumno al = new Alumno((int)modeloTabla.getValueAt(i,0), (String)modeloTabla.getValueAt(i,1), (String)modeloTabla.getValueAt(i,2), (String)modeloTabla.getValueAt(i,3));
                al.setPresente((boolean)modeloTabla.getValueAt(i,4));
                lista.add(al);
            }
            try {
                if (asistenciaDAO.guardarAsistencias(lista, idMateria)) {
                    JOptionPane.showMessageDialog(this, "Asistencias impactadas correctamente en el Servidor.");
                    actualizarListaAlumnos();
                }
            } catch (AsistenciaException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

   
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new VentanaLogin().setVisible(true));
    }
}
