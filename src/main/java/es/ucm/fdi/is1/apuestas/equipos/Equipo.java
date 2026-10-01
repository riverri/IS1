package es.ucm.fdi.is1.apuestas.equipos;

import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Equipo o deportista sobre el que se puede apostar (HU-01).
 * La calificación de calidad (0-10) es la base del cálculo de cuotas.
 */
@Entity
public class Equipo {

    @Id
    @GeneratedValue
    private Long id;

    @NotBlank
    private String nombre;

    @NotNull
    @Enumerated(EnumType.STRING)
    private Deporte deporte;

    @NotNull
    @DecimalMin("0.0")
    @DecimalMax("10.0")
    private Double calidad;

    @ManyToMany(fetch = FetchType.EAGER)
    private Set<Competicion> competiciones = new HashSet<>();

    /** Identificador del equipo en la API de datos deportivos (football-data.org). */
    @Column(unique = true)
    private Integer idExterno;

    /** Forma reciente (HU-02); ajusta la calificación al calcular las cuotas. */
    @NotNull
    @Enumerated(EnumType.STRING)
    private Forma forma = Forma.NORMAL;

    /** Dirección de la imagen del escudo; si es null se muestran las iniciales. */
    @Column(length = 500)
    private String escudoUrl;

    protected Equipo() {
        // requerido por JPA
    }

    public Equipo(String nombre, Deporte deporte, Double calidad) {
        this.nombre = nombre;
        this.deporte = deporte;
        this.calidad = calidad;
    }

    public void participaEn(Competicion competicion) {
        competiciones.add(competicion);
    }

    public boolean participaEn(Long competicionId) {
        return competiciones.stream().anyMatch(c -> c.getId().equals(competicionId));
    }

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public Deporte getDeporte() {
        return deporte;
    }

    public Double getCalidad() {
        return calidad;
    }

    public void setCalidad(Double calidad) {
        this.calidad = calidad;
    }

    public Forma getForma() {
        return forma;
    }

    public void setForma(Forma forma) {
        this.forma = forma;
    }

    public Set<Competicion> getCompeticiones() {
        return competiciones;
    }

    public Integer getIdExterno() {
        return idExterno;
    }

    public void setIdExterno(Integer idExterno) {
        this.idExterno = idExterno;
    }

    public String getEscudoUrl() {
        return escudoUrl;
    }

    public void setEscudoUrl(String escudoUrl) {
        this.escudoUrl = escudoUrl;
    }
}
