package edu.siglo21.asistencia;

public class Curso {
    private int idCurso;
    private String nombreCurso;
    private String division;

    public Curso(int idCurso, String nombreCurso, String division) {
        this.idCurso = idCurso;
        this.nombreCurso = nombreCurso;
        this.division = division;
    }

    public int getIdCurso() { return idCurso; }
    public String getNombreCurso() { return nombreCurso; }
    public String getDivision() { return division; }

    @Override
    public String toString() {
        return idCurso + " - " + nombreCurso + " " + division;
    }
}
