package edu.siglo21.asistencia;

public class AsistenciaException extends Exception {
    
    public AsistenciaException(String mensaje) {
        super(mensaje);
    }
    
    public AsistenciaException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
