package edu.siglo21.asistencia;

public class Materia {
    private int idMateria;
    private String nombreMateria;

    public Materia(int idMateria, String nombreMateria) {
        this.idMateria = idMateria;
        this.nombreMateria = nombreMateria;
    }

    public int getIdMateria() { return idMateria; }
    public String getNombreMateria() { return nombreMateria; }

    @Override
    public String toString() {
        return idMateria + " - " + nombreMateria;
    }
}
