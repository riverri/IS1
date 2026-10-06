package es.ucm.fdi.is1.apuestas.equipos;

import java.time.LocalDate;
import java.time.Period;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

/**
 * Jugador de la plantilla de un equipo. Llega de la API (con su identificador) o lo da de alta el creador.
 * La nota (0-10) la pone el creador y decide quién sale en la alineación probable.
 */
@Entity
public class Jugador {

    public static final double NOTA_INICIAL = 6.0;

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne(optional = false)
    private Equipo equipo;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Posicion posicion;

    private Integer dorsal;

    private String nacionalidad;

    private LocalDate fechaNacimiento;

    @Column(nullable = false)
    private double nota = NOTA_INICIAL;

    /** Identificador del jugador en football-data.org; null si lo ha creado el creador. */
    @Column(unique = true)
    private Integer idExterno;

    protected Jugador() {
        // requerido por JPA
    }

    public Jugador(Equipo equipo, String nombre, Posicion posicion) {
        this.equipo = equipo;
        this.nombre = nombre;
        this.posicion = posicion;
    }

    /** Datos que vienen de la API; la nota no se toca, es del creador. */
    public void actualizar(Equipo nuevoEquipo, String nuevoNombre, Posicion nuevaPosicion, Integer nuevoDorsal,
                           String nuevaNacionalidad, LocalDate nuevaFecha) {
        equipo = nuevoEquipo;
        nombre = nuevoNombre;
        posicion = nuevaPosicion;
        dorsal = nuevoDorsal;
        nacionalidad = nuevaNacionalidad;
        fechaNacimiento = nuevaFecha;
    }

    public void cambiarNota(double nueva) {
        if (Double.isNaN(nueva) || nueva < 0 || nueva > 10) {
            throw new IllegalArgumentException("La nota debe estar entre 0 y 10");
        }
        nota = Math.round(nueva * 10) / 10.0;
    }

    /** Años cumplidos, o null si no se conoce la fecha de nacimiento. */
    public Integer getEdad() {
        return fechaNacimiento == null ? null : Period.between(fechaNacimiento, LocalDate.now()).getYears();
    }

    public boolean isDeLaApi() {
        return idExterno != null;
    }

    public Long getId() {
        return id;
    }

    public Equipo getEquipo() {
        return equipo;
    }

    public String getNombre() {
        return nombre;
    }

    public Posicion getPosicion() {
        return posicion;
    }

    public Integer getDorsal() {
        return dorsal;
    }

    public void setDorsal(Integer dorsal) {
        this.dorsal = dorsal;
    }

    public String getNacionalidad() {
        return nacionalidad;
    }

    public void setNacionalidad(String nacionalidad) {
        this.nacionalidad = nacionalidad;
    }

    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }

    public double getNota() {
        return nota;
    }

    public Integer getIdExterno() {
        return idExterno;
    }

    public void setIdExterno(Integer idExterno) {
        this.idExterno = idExterno;
    }
}
