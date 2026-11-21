package es.ucm.fdi.is1.apuestas.apuesta;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

/**
 * Cuándo se hizo por última vez una tarea programada (una fila por tarea). Sirve para no saltarse ninguna si la
 * aplicación estaba parada a la hora en punto: al volver, ve que la última vez es anterior a la que tocaba.
 */
@Entity
public class TareaRealizada {

    @Id
    @Column(length = 60)
    private String nombre;

    @Column(nullable = false)
    private LocalDateTime ultima;

    protected TareaRealizada() {
        // Para JPA
    }

    public TareaRealizada(String nombre, LocalDateTime ultima) {
        this.nombre = nombre;
        this.ultima = ultima;
    }

    public void marcar(LocalDateTime cuando) {
        ultima = cuando;
    }

    public String getNombre() {
        return nombre;
    }

    public LocalDateTime getUltima() {
        return ultima;
    }
}
