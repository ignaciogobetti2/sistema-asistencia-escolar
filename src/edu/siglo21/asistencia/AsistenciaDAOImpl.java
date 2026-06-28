package edu.siglo21.asistencia;

import java.sql.*;
import java.util.ArrayList;

public class AsistenciaDAOImpl implements AsistenciaDAO {

    private static final String URL = "jdbc:mysql://localhost:3306/control_asistencia_db?useSSL=true&trustServerCertificate=true&allowPublicKeyRetrieval=true";
    private static final String USER = "root";
    private static final String PASS = "abc123456789";

    private Connection obtenerConexion() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }

    @Override
    public ArrayList<Alumno> obtenerAlumnosPorCurso(int idCurso) throws AsistenciaException {
        ArrayList<Alumno> lista = new ArrayList<>();
        String sql = "SELECT id_alumno, apellido, nombre, dni FROM alumnos WHERE id_curso = ? ORDER BY apellido, nombre";
        
        try (Connection con = obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idCurso);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new Alumno(
                        rs.getInt("id_alumno"),
                        rs.getString("apellido"),
                        rs.getString("nombre"),
                        rs.getString("dni")
                    ));
                }
            }
        } catch (SQLException e) {
            throw new AsistenciaException("Error al consultar la lista de alumnos en MySQL.", e);
        }
        return lista;
    }

    @Override
    public boolean guardarAsistencias(ArrayList<Alumno> listaAlumnos, int idMateria) throws AsistenciaException {
        // SOLUCIÓN: Agregamos id_usuario al INSERT con valor '1' para corregir el error crítico
        String sql = "INSERT INTO registros_asistencia (id_alumno, id_materia, id_usuario, fecha_hora, estado_asistencia) VALUES (?, ?, 1, NOW(), ?)";
        try (Connection con = obtenerConexion()) {
            con.setAutoCommit(false);
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                for (Alumno al : listaAlumnos) {
                    ps.setInt(1, al.getId());
                    ps.setInt(2, idMateria);
                    ps.setString(3, al.isPresente() ? "PRESENTE" : "AUSENTE");
                    ps.addBatch();
                }
                ps.executeBatch();
                con.commit();
                return true;
            } catch (SQLException e) {
                con.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new AsistenciaException("Error crítico en MySQL al guardar las asistencias: " + e.getMessage(), e);
        }
    }

    @Override
    public ArrayList<Object[]> obtenerHistorialAsistencias() throws AsistenciaException {
        ArrayList<Object[]> lista = new ArrayList<>();
        
        // Consultamos todas las columnas reales mapeadas
        String sql = "SELECT r.id_registro, a.apellido, a.nombre, m.nombre_materia, r.fecha_hora, r.estado_asistencia " +
                     "FROM registros_asistencia r " +
                     "JOIN alumnos a ON r.id_alumno = a.id_alumno " +
                     "JOIN materias m ON r.id_materia = m.id_materia " +
                     "ORDER BY r.fecha_hora DESC";

        try (Connection con = obtenerConexion();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                String estudianteCompleto = rs.getString("apellido") + ", " + rs.getString("nombre");
                
                lista.add(new Object[]{
                    rs.getInt("id_registro"),
                    estudianteCompleto,
                    rs.getString("nombre_materia"),
                    rs.getTimestamp("fecha_hora").toString(),
                    rs.getString("estado_asistencia")
                });
            }
        } catch (SQLException e) {
            throw new AsistenciaException("Error en MySQL al leer el historial de auditoría: " + e.getMessage(), e);
        }
        return lista;
    }

    @Override
    public boolean insertarCurso(String nombre, String division) throws AsistenciaException {
        String sql = "INSERT INTO cursos (nombre_curso, division) VALUES (?, ?)";
        try (Connection con = obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nombre);
            ps.setString(2, division);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new AsistenciaException("Error al registrar el nuevo curso.", e);
        }
    }

    @Override
    public boolean insertarMateria(String nombreMateria) throws AsistenciaException {
        String sql = "INSERT INTO materias (nombre_materia) VALUES (?)";
        try (Connection con = obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nombreMateria);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new AsistenciaException("Error al registrar la asignatura.", e);
        }
    }

    @Override
    public boolean insertarAlumno(int idManual, String apellido, String nombre, String dni, int idCurso) throws AsistenciaException {
        // 1. La consulta SQL ahora incluye explícitamente la columna id_alumno
        String sql = "INSERT INTO alumnos (id_alumno, apellido, nombre, dni, id_curso) VALUES (?, ?, ?, ?, ?)";
        
        try (Connection con = DriverManager.getConnection(URL, USER, PASS);
             PreparedStatement stmt = con.prepareStatement(sql)) {
            
            // 2. Pasamos el ID manual que escribió el profesor en la interfaz como primer parámetro
            stmt.setInt(1, idManual);
            stmt.setString(2, apellido);
            stmt.setString(3, nombre);
            stmt.setString(4, dni);
            stmt.setInt(5, idCurso);
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new AsistenciaException("Error al registrar alumno: " + e.getMessage());
        }
    }

    @Override
    public boolean eliminarAlumno(int idAlumno) throws AsistenciaException {
        String sql = "DELETE FROM alumnos WHERE id_alumno = ?";
        try (Connection con = obtenerConexion(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idAlumno);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new AsistenciaException("No se puede eliminar el alumno. Asegúrese de limpiar primero sus registros de asistencia.", e);
        }
    }

    @Override
    public boolean eliminarCurso(int idCurso) throws AsistenciaException {
        String sql = "DELETE FROM cursos WHERE id_curso = ?";
        try (Connection con = obtenerConexion(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idCurso);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new AsistenciaException("No se puede eliminar el curso. Tiene alumnos inscriptos.", e);
        }
    }

    @Override
    public boolean eliminarMateria(int idMateria) throws AsistenciaException {
        String sql = "DELETE FROM materias WHERE id_materia = ?";
        try (Connection con = obtenerConexion(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idMateria);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new AsistenciaException("No se puede eliminar la materia. Posee asistencias tomadas.", e);
        }
    }

    @Override
    public boolean vaciarHistorial() throws AsistenciaException {
        try (Connection con = obtenerConexion(); Statement st = con.createStatement()) {
            st.executeUpdate("DELETE FROM registros_asistencia");
            return true;
        } catch (SQLException e) {
            throw new AsistenciaException("Error crítico al intentar vaciar el historial.", e);
        }
    }
}
