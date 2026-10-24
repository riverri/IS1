package es.ucm.fdi.is1.apuestas.gestion;

import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.Set;

import org.springframework.format.annotation.DateTimeFormat;

import es.ucm.fdi.is1.apuestas.anuncios.Anuncio;
import es.ucm.fdi.is1.apuestas.anuncios.Ubicacion;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Formulario de alta y edición de un anuncio interno (HU-61). Las anotaciones comprueban cada campo; que el destino
 * sea una sección de la web y que las fechas tengan sentido lo comprueba {@link Anuncio#cambiar}.
 */
public class AnuncioForm {

    @NotBlank(message = "Escribe el título")
    @Size(max = 60, message = "Máximo 60 caracteres")
    private String titulo;

    @NotBlank(message = "Escribe el texto")
    @Size(max = 160, message = "Máximo 160 caracteres")
    private String texto;

    /** Un emoji, opcional. */
    @Size(max = 8, message = "Solo un emoji")
    private String icono;

    @NotBlank(message = "Escribe a qué sección lleva, por ejemplo /mercados")
    @Size(max = 200, message = "Máximo 200 caracteres")
    private String destino;

    @NotEmpty(message = "Elige al menos una sección donde mostrarlo")
    private Set<Ubicacion> ubicaciones = EnumSet.noneOf(Ubicacion.class);

    @NotNull(message = "Indica la prioridad")
    @Min(value = 1, message = "La prioridad va de 1 a 10")
    @Max(value = 10, message = "La prioridad va de 1 a 10")
    private Integer prioridad = 5;

    /** Opcionales; formato del campo datetime-local de HTML. */
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime desde;

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime hasta;

    /** Formulario relleno con los datos de un anuncio, para editarlo. */
    static AnuncioForm de(Anuncio anuncio) {
        AnuncioForm form = new AnuncioForm();
        form.titulo = anuncio.getTitulo();
        form.texto = anuncio.getTexto();
        form.icono = anuncio.getIcono();
        form.destino = anuncio.getDestino();
        form.ubicaciones = anuncio.getUbicaciones();
        form.prioridad = anuncio.getPrioridad();
        form.desde = anuncio.getDesde();
        form.hasta = anuncio.getHasta();
        return form;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getTexto() {
        return texto;
    }

    public void setTexto(String texto) {
        this.texto = texto;
    }

    public String getIcono() {
        return icono;
    }

    public void setIcono(String icono) {
        this.icono = icono;
    }

    public String getDestino() {
        return destino;
    }

    public void setDestino(String destino) {
        this.destino = destino;
    }

    public Set<Ubicacion> getUbicaciones() {
        return ubicaciones;
    }

    public void setUbicaciones(Set<Ubicacion> ubicaciones) {
        this.ubicaciones = ubicaciones;
    }

    public Integer getPrioridad() {
        return prioridad;
    }

    public void setPrioridad(Integer prioridad) {
        this.prioridad = prioridad;
    }

    public LocalDateTime getDesde() {
        return desde;
    }

    public void setDesde(LocalDateTime desde) {
        this.desde = desde;
    }

    public LocalDateTime getHasta() {
        return hasta;
    }

    public void setHasta(LocalDateTime hasta) {
        this.hasta = hasta;
    }
}
