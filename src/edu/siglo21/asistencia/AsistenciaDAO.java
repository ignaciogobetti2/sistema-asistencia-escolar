package edu.siglo21.asistencia;

import java.util.ArrayList;

public interface AsistenciaDAO {
    boolean insertarCurso(String nombre, String division) throws AsistenciaException;
    boolean eliminarCurso(int idCurso) throws AsistenciaException;
    boolean insertarMateria(String nombreMateria) throws AsistenciaException;
    boolean eliminarMateria(int idMateria) throws AsistenciaException;
    
    
    boolean insertarAlumno(int idManual, String apellido, String nombre, String dni, int idCurso) throws AsistenciaException;
    
    boolean eliminarAlumno(int idAlumno) throws AsistenciaException;
    ArrayList<Alumno> obtenerAlumnosPorCurso(int idCurso) throws AsistenciaException;
    boolean guardarAsistencias(ArrayList<Alumno> alumnos, int idMateria) throws AsistenciaException;
    ArrayList<Object[]> obtenerHistorialAsistencias() throws AsistenciaException;
    boolean vaciarHistorial() throws AsistenciaException;
}
