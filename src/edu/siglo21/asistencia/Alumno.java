package edu.siglo21.asistencia;

public class Alumno {
    private int id;
    private String nombre;
    private String apellido;
    private String dni;
    private boolean presente;

    // Constructor completo
    public Alumno(int id, String apellido, String nombre, String dni) {
        this.id = id;
        this.apellido = apellido;
        this.nombre = nombre;
        this.dni = dni;
        this.presente = false; // Por defecto arranca ausente
    }

    // Getters y Setters
    public int getId() { return id; }
    public String getNombre() { return nombre; }
    public String getApellido() { return apellido; }
    public String getDni() { return dni; }
    public boolean isPresente() { return presente; }
    public void setPresente(boolean presente) { this.presente = presente; }
}
