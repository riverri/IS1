package es.ucm.fdi.is1.apuestas.cripto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

/**
 * Entidad JPA: una criptomoneda real que se puede comprar y vender con moneditas (HU-69 a HU-71). Solo el precio es
 * real: se descarga de una API pública ({@link FuentePrecios}) con 1 monedita = 1 euro. Nadie tiene criptomonedas de
 * verdad ni hay carteras, exchanges o blockchain: todo es saldo virtual, como el resto de la web.
 */
@Entity
public class Criptomoneda {

    @Id
    @GeneratedValue
    private Long id;

    /** "DOGE", "PEPE"… Único: es lo que va en la dirección (/cripto/DOGE). */
    @Column(nullable = false, unique = true, length = 10)
    private String simbolo;

    @Column(nullable = false, length = 60)
    private String nombre;

    /** Identificador en CoinGecko: "dogecoin", "pepe"… */
    @Column(nullable = false, unique = true, length = 60)
    private String idApi;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private CategoriaCripto categoria;

    /** Si se ofrece en la web; el creador puede quitar una sin borrar las carteras (HU-73). */
    @Column(nullable = false)
    private boolean activa = true;

    /** Orden dentro de su categoría. */
    @Column(nullable = false)
    private int orden;

    /** Precio en moneditas (= euros); null hasta la primera descarga. Muchas memecoins valen menos de 0,0001 €. */
    @Column(precision = 30, scale = 12)
    private BigDecimal precio;

    /** Variación del precio en las últimas 24 horas, en %. */
    @Column(precision = 10, scale = 4)
    private BigDecimal variacion24h;

    /** Cuándo se descargó el precio. */
    private LocalDateTime actualizado;

    protected Criptomoneda() {
        // requerido por JPA
    }

    public Criptomoneda(String simbolo, String nombre, String idApi, CategoriaCripto categoria, int orden) {
        this.simbolo = simbolo;
        this.nombre = nombre;
        this.idApi = idApi;
        this.categoria = categoria;
        this.orden = orden;
    }

    /** Nuevo precio descargado de la API. */
    void actualizarPrecio(BigDecimal nuevo, BigDecimal variacion, LocalDateTime cuando) {
        precio = nuevo;
        variacion24h = variacion;
        actualizado = cuando;
    }

    /**
     * Si el precio sirve para comprar o vender: existe y no tiene más de {@code minutos} minutos. Con un precio viejo
     * alguien podría comprar sabiendo ya que ha subido (HU-69).
     */
    public boolean precioAlDia(LocalDateTime ahora, int minutos) {
        return precio != null && actualizado != null && !actualizado.plusMinutes(minutos).isBefore(ahora);
    }

    void activar(boolean activar) {
        activa = activar;
    }

    public Long getId() {
        return id;
    }

    public String getSimbolo() {
        return simbolo;
    }

    public String getNombre() {
        return nombre;
    }

    public String getIdApi() {
        return idApi;
    }

    public CategoriaCripto getCategoria() {
        return categoria;
    }

    public boolean isActiva() {
        return activa;
    }

    public int getOrden() {
        return orden;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public BigDecimal getVariacion24h() {
        return variacion24h;
    }

    public LocalDateTime getActualizado() {
        return actualizado;
    }
}
