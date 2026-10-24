package es.ucm.fdi.is1.apuestas.cripto;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.server.ResponseStatusException;

import es.ucm.fdi.is1.apuestas.apuesta.JuegoResponsableService;
import es.ucm.fdi.is1.apuestas.apuesta.LimitesService;
import es.ucm.fdi.is1.apuestas.usuarios.TipoMovimiento;
import es.ucm.fdi.is1.apuestas.usuarios.Usuario;
import es.ucm.fdi.is1.apuestas.usuarios.UsuarioRepository;
import es.ucm.fdi.is1.apuestas.web.GraficoLinea;

/**
 * Cartera de criptomonedas (HU-69 a HU-73): precios reales descargados de {@link FuentePrecios}, compras y ventas con
 * moneditas (1 monedita = 1 euro), la cartera de cada usuario y la configuración del creador.
 *
 * <p>Antes de cobrar se comprueba lo mismo que en el resto de juegos: sección abierta, el creador no juega, importe
 * dentro de los límites de apuesta (HU-07), precio al día y sin cambios, juego responsable (HU-10) y saldo. Cada cobro
 * y cada pago es un movimiento de saldo (HU-55). La comisión se redondea hacia arriba y lo que se recibe, hacia abajo:
 * nunca a favor del usuario.
 */
@Service
public class CriptoService {

    /** Días de precios que se guardan para el gráfico. */
    static final int DIAS_HISTORICO = 7;
    /** Cada cuánto se guarda un precio en el histórico: así el gráfico tiene unos 336 puntos por semana. */
    static final int MINUTOS_ENTRE_PUNTOS = 30;

    private final CriptomonedaRepository criptos;
    private final PrecioCriptoRepository historico;
    private final PosicionCriptoRepository posiciones;
    private final OperacionCriptoRepository operaciones;
    private final ConfiguracionCriptoRepository configuracion;
    private final UsuarioRepository usuarios;
    private final LimitesService limites;
    private final JuegoResponsableService juegoResponsable;
    private final FuentePrecios fuente;
    private final TransactionTemplate transaccion;
    private final Clock reloj;

    public CriptoService(CriptomonedaRepository criptos, PrecioCriptoRepository historico,
                         PosicionCriptoRepository posiciones, OperacionCriptoRepository operaciones,
                         ConfiguracionCriptoRepository configuracion, UsuarioRepository usuarios,
                         LimitesService limites, JuegoResponsableService juegoResponsable, FuentePrecios fuente,
                         PlatformTransactionManager transacciones, Clock reloj) {
        this.criptos = criptos;
        this.historico = historico;
        this.posiciones = posiciones;
        this.operaciones = operaciones;
        this.configuracion = configuracion;
        this.usuarios = usuarios;
        this.limites = limites;
        this.juegoResponsable = juegoResponsable;
        this.fuente = fuente;
        this.transaccion = new TransactionTemplate(transacciones);
        this.reloj = reloj;
    }

    // --- Precios ---

    /**
     * Descarga los precios de las criptomonedas activas y los guarda (HU-69). La llamada a la API va fuera de la
     * transacción: si tarda, no deja la base de datos esperando.
     *
     * @return cuántas criptomonedas tienen precio nuevo
     * @throws org.springframework.web.client.RestClientException si la API da error o no responde
     */
    public int actualizarPrecios() {
        List<String> ids = transaccion.execute(estado -> criptos.findByActivaTrue().stream()
                .map(Criptomoneda::getIdApi).toList());
        Map<String, FuentePrecios.Precio> precios = fuente.precios(ids);
        LocalDateTime ahora = LocalDateTime.now(reloj);
        Integer actualizadas = transaccion.execute(estado -> {
            int cuantas = 0;
            for (Criptomoneda cripto : criptos.findByActivaTrue()) {
                FuentePrecios.Precio precio = precios.get(cripto.getIdApi());
                if (precio == null || precio.eur() == null || precio.eur().signum() <= 0) {
                    continue;
                }
                BigDecimal eur = precio.eur().setScale(12, RoundingMode.HALF_UP);
                // Una cripto que valga menos de 0,000000000001 € se quedaría en 0: no se puede comprar a ese precio
                if (eur.signum() == 0) {
                    continue;
                }
                BigDecimal variacion = precio.variacion24h() == null ? null
                        : precio.variacion24h().setScale(4, RoundingMode.HALF_UP);
                cripto.actualizarPrecio(eur, variacion, ahora);
                guardarEnHistorico(cripto, eur, ahora);
                cuantas++;
            }
            historico.borrarAnteriores(ahora.minusDays(DIAS_HISTORICO));
            return cuantas;
        });
        return actualizadas == null ? 0 : actualizadas;
    }

    private void guardarEnHistorico(Criptomoneda cripto, BigDecimal precio, LocalDateTime ahora) {
        boolean toca = historico.findTopByCriptoOrderByFechaDesc(cripto)
                .map(ultimo -> !ultimo.getFecha().plusMinutes(MINUTOS_ENTRE_PUNTOS).isAfter(ahora))
                .orElse(true);
        if (toca) {
            historico.save(new PrecioCripto(cripto, precio, ahora));
        }
    }

    // --- Consulta ---

    /** Las criptomonedas que se ofrecen: primero las memecoins (CA-69.1) y dentro de cada tipo, por su orden. */
    @Transactional(readOnly = true)
    public List<Criptomoneda> criptos() {
        return criptos.findByActivaTrue().stream().sorted(ORDEN).toList();
    }

    /** Todas, también las retiradas: para Gestión. */
    @Transactional(readOnly = true)
    public List<Criptomoneda> todas() {
        return criptos.findAll().stream().sorted(ORDEN).toList();
    }

    private static final Comparator<Criptomoneda> ORDEN = Comparator.comparing(Criptomoneda::getCategoria)
            .thenComparingInt(Criptomoneda::getOrden);

    /** @throws ResponseStatusException 404 si no existe */
    @Transactional(readOnly = true)
    public Criptomoneda cripto(String simbolo) {
        return criptos.findBySimbolo(simbolo == null ? "" : simbolo.toUpperCase())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe la cripto " + simbolo));
    }

    /** Gráfico de la última semana (CA-69.2); null si aún no hay al menos dos precios. */
    @Transactional(readOnly = true)
    public GraficoLinea grafico(String simbolo) {
        Criptomoneda cripto = cripto(simbolo);
        LocalDateTime desde = LocalDateTime.now(reloj).minusDays(DIAS_HISTORICO);
        List<Double> precios = historico.findByCriptoAndFechaAfterOrderByFechaAsc(cripto, desde).stream()
                .map(p -> p.getPrecio().doubleValue()).toList();
        return GraficoLinea.de(precios, null, null);
    }

    /** Si el precio de una cripto sirve ahora para comprar o vender. */
    @Transactional(readOnly = true)
    public boolean precioAlDia(Criptomoneda cripto) {
        return cripto.precioAlDia(LocalDateTime.now(reloj), configuracion().getMinutosValidez());
    }

    /** Lo que se recibiría al comprar con {@code importe} moneditas, sin comprar todavía (CA-70.1). */
    @Transactional(readOnly = true)
    public Cotizacion cotizarCompra(String simbolo, BigDecimal importe) {
        Criptomoneda cripto = cripto(simbolo);
        if (cripto.getPrecio() == null) {
            return null;
        }
        BigDecimal comision = comision(importe);
        return new Cotizacion(importe, comision, cantidad(importe.subtract(comision), cripto.getPrecio()),
                cripto.getPrecio());
    }

    /** Previsión de una compra: lo que se paga, la comisión y cuánta cripto se recibe a ese precio. */
    public record Cotizacion(BigDecimal importe, BigDecimal comision, BigDecimal cantidad, BigDecimal precio) {
    }

    /** La cartera del usuario: lo que tiene de cada cripto, su valor y lo que gana o pierde (HU-72). */
    @Transactional(readOnly = true)
    public Cartera cartera(String email) {
        List<PosicionCripto> mias = posiciones.findByUsuario(usuario(email)).stream()
                .filter(PosicionCripto::isConCantidad)
                .sorted(Comparator.comparing(PosicionCripto::getValor).reversed())
                .toList();
        return new Cartera(mias);
    }

    /** Lo que tiene de una cripto; null si nada. */
    @Transactional(readOnly = true)
    public PosicionCripto posicion(String email, String simbolo) {
        return posiciones.findByUsuarioAndCripto(usuario(email), cripto(simbolo))
                .filter(PosicionCripto::isConCantidad).orElse(null);
    }

    /** Las últimas operaciones del usuario (CA-72.2). */
    @Transactional(readOnly = true)
    public List<OperacionCripto> operaciones(String email) {
        return operaciones.findTop50ByUsuarioOrderByFechaDescIdDesc(usuario(email));
    }

    /** Las posiciones de un usuario con sus totales. */
    public record Cartera(List<PosicionCripto> posiciones) {

        public BigDecimal getValor() {
            return posiciones.stream().map(PosicionCripto::getValor).reduce(BigDecimal.ZERO, BigDecimal::add);
        }

        public BigDecimal getCoste() {
            return posiciones.stream().map(PosicionCripto::getCoste).reduce(BigDecimal.ZERO, BigDecimal::add);
        }

        public BigDecimal getGanancia() {
            return getValor().subtract(getCoste());
        }
    }

    // --- Comprar y vender ---

    /**
     * Compra una criptomoneda con {@code importe} moneditas, comisión incluida (HU-70).
     *
     * @param precioVisto precio que tenía delante el usuario; null para no comprobarlo
     * @throws PrecioCambiadoException  si el precio ha cambiado desde entonces (CA-70.3)
     * @throws IllegalStateException    si la sección está cerrada, la cripto retirada o el precio no está al día
     *                                  (CA-70.4)
     * @throws IllegalArgumentException si el importe no es válido o no respeta los límites, por el juego responsable
     *                                  o si es el creador (CA-70.2)
     * @throws es.ucm.fdi.is1.apuestas.usuarios.SaldoInsuficienteException si no hay saldo (CA-70.2)
     */
    @Transactional
    public PosicionCripto comprar(String email, String simbolo, BigDecimal importe, BigDecimal precioVisto) {
        LocalDateTime ahora = LocalDateTime.now(reloj);
        ConfiguracionCripto config = abierta();
        Criptomoneda cripto = cripto(simbolo);
        if (!cripto.isActiva()) {
            throw new IllegalStateException(cripto.getNombre() + " ya no se puede comprar");
        }
        Usuario usuario = jugador(email);
        if (importe == null || importe.signum() <= 0 || importe.stripTrailingZeros().scale() > 2) {
            throw new IllegalArgumentException("Introduce un importe válido, con 2 decimales como mucho");
        }
        limites.actuales().comprobarImporte(importe);
        comprobarPrecio(cripto, config, precioVisto, ahora);
        juegoResponsable.comprobar(usuario, importe, ahora);

        BigDecimal comision = comision(importe);
        BigDecimal cantidad = cantidad(importe.subtract(comision), cripto.getPrecio());
        if (cantidad.signum() == 0) {
            throw new IllegalArgumentException("Con ese importe no llega ni a la unidad más pequeña");
        }
        usuario.cargar(importe, TipoMovimiento.CRIPTO_COMPRA, "Compra de " + cripto.getSimbolo(), ahora);
        PosicionCripto posicion = posiciones.findByUsuarioAndCripto(usuario, cripto)
                .orElseGet(() -> posiciones.save(new PosicionCripto(usuario, cripto)));
        posicion.sumar(cantidad, importe);
        operaciones.save(new OperacionCripto(usuario, cripto, OperacionCripto.Tipo.COMPRA, cantidad,
                cripto.getPrecio(), importe, comision, ahora));
        return posicion;
    }

    /**
     * Vende una cantidad de una criptomoneda al precio actual, menos la comisión (HU-71). Se puede vender aunque el
     * creador la haya retirado: nadie se queda atrapado (CA-73.2).
     *
     * @param cantidad lo que se vende; null para venderlo todo
     * @return las moneditas que recibe
     * @throws IllegalArgumentException si vende más de lo que tiene (CA-71.2) o la cantidad no es válida
     * @throws IllegalStateException    si la sección está cerrada o el precio no está al día
     * @throws PrecioCambiadoException  si el precio ha cambiado desde que lo vio
     */
    @Transactional
    public BigDecimal vender(String email, String simbolo, BigDecimal cantidad, BigDecimal precioVisto) {
        LocalDateTime ahora = LocalDateTime.now(reloj);
        ConfiguracionCripto config = abierta();
        Criptomoneda cripto = cripto(simbolo);
        Usuario usuario = jugador(email);
        PosicionCripto posicion = posiciones.findByUsuarioAndCripto(usuario, cripto)
                .filter(PosicionCripto::isConCantidad)
                .orElseThrow(() -> new IllegalArgumentException("No tienes " + cripto.getSimbolo()));
        BigDecimal vendida = cantidad == null ? posicion.getCantidad()
                : cantidad.setScale(PosicionCripto.DECIMALES_CANTIDAD, RoundingMode.DOWN);
        if (vendida.signum() <= 0) {
            throw new IllegalArgumentException("Indica cuánto quieres vender");
        }
        if (vendida.compareTo(posicion.getCantidad()) > 0) {
            throw new IllegalArgumentException("No tienes tanto: tienes "
                    + posicion.getCantidad().stripTrailingZeros().toPlainString().replace('.', ',') + " "
                    + cripto.getSimbolo());
        }
        comprobarPrecio(cripto, config, precioVisto, ahora);
        BigDecimal bruto = vendida.multiply(cripto.getPrecio()).setScale(2, RoundingMode.DOWN);
        BigDecimal comision = comision(bruto);
        BigDecimal neto = bruto.subtract(comision);
        if (neto.signum() <= 0) {
            throw new IllegalArgumentException("Es demasiado poco para vender: no llega a un céntimo");
        }
        posicion.restar(vendida);
        usuario.abonar(neto, TipoMovimiento.CRIPTO_VENTA, "Venta de " + cripto.getSimbolo(), ahora);
        operaciones.save(new OperacionCripto(usuario, cripto, OperacionCripto.Tipo.VENTA, vendida,
                cripto.getPrecio(), neto, comision, ahora));
        return neto;
    }

    // --- Gestión (creador) ---

    @Transactional(readOnly = true)
    public ConfiguracionCripto configuracion() {
        return configuracion.findById(ConfiguracionCripto.ID).orElseThrow();
    }

    /**
     * Abre o cierra la sección y cambia la comisión y la validez de los precios (CA-73.1).
     *
     * @param comisionPorcentaje en tanto por ciento (1 = 1 %)
     * @throws IllegalArgumentException si algún valor está fuera de rango
     */
    @Transactional
    public void configurar(boolean activo, BigDecimal comisionPorcentaje, int minutosValidez) {
        if (comisionPorcentaje == null) {
            throw new IllegalArgumentException("Indica la comisión");
        }
        configuracion().cambiar(activo, comisionPorcentaje.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP),
                minutosValidez);
    }

    /** Ofrece o retira una cripto; las carteras no se tocan (CA-73.2). */
    @Transactional
    public void activar(String simbolo, boolean activa) {
        cripto(simbolo).activar(activa);
    }

    /** Para el panel de la casa: lo cobrado en compras, lo pagado en ventas y lo que valen las carteras ahora. */
    @Transactional(readOnly = true)
    public CuentasCripto cuentas() {
        BigDecimal compras = BigDecimal.ZERO;
        BigDecimal ventas = BigDecimal.ZERO;
        BigDecimal comisiones = BigDecimal.ZERO;
        for (Object[] fila : operaciones.importesPorTipo()) {
            if (fila[0] == OperacionCripto.Tipo.COMPRA) {
                compras = (BigDecimal) fila[1];
            } else {
                ventas = (BigDecimal) fila[1];
            }
            comisiones = comisiones.add((BigDecimal) fila[2]);
        }
        BigDecimal carteras = posiciones.findAll().stream().map(PosicionCripto::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new CuentasCripto(compras, ventas, comisiones, carteras);
    }

    /**
     * Cuentas de la sección. La casa hace de contrapartida: si las criptos suben, lo que valen las carteras es lo que
     * tendría que pagar si todos vendieran.
     */
    public record CuentasCripto(BigDecimal compras, BigDecimal ventas, BigDecimal comisiones, BigDecimal carteras) {

        /** Compras − ventas − valor de las carteras: lo que gana la casa si todos vendieran ahora. */
        public BigDecimal getBeneficio() {
            return compras.subtract(ventas).subtract(carteras);
        }
    }

    // --- Ayudas ---

    private ConfiguracionCripto abierta() {
        ConfiguracionCripto config = configuracion();
        if (!config.isActivo()) {
            throw new IllegalStateException("La sección de criptomonedas está cerrada ahora mismo");
        }
        return config;
    }

    private Usuario usuario(String email) {
        return usuarios.findByEmail(email).orElseThrow();
    }

    private Usuario jugador(String email) {
        Usuario usuario = usuario(email);
        if (usuario.isCreador()) {
            throw new IllegalArgumentException("El creador de apuestas no puede comprar ni vender criptomonedas");
        }
        return usuario;
    }

    /** Precio al día (CA-70.4) y el mismo que vio el usuario (CA-70.3). */
    private static void comprobarPrecio(Criptomoneda cripto, ConfiguracionCripto config, BigDecimal precioVisto,
                                        LocalDateTime ahora) {
        if (!cripto.precioAlDia(ahora, config.getMinutosValidez())) {
            throw new IllegalStateException("Ahora mismo no tenemos un precio al día de " + cripto.getNombre()
                    + ". Prueba dentro de un momento.");
        }
        if (precioVisto != null && precioVisto.compareTo(cripto.getPrecio()) != 0) {
            throw new PrecioCambiadoException();
        }
    }

    /** Comisión de la casa sobre un importe, redondeada hacia arriba a céntimos. */
    private BigDecimal comision(BigDecimal importe) {
        return importe.multiply(configuracion().getComision()).setScale(2, RoundingMode.CEILING);
    }

    /** Cuánta cripto se compra con un importe, redondeando hacia abajo. */
    private static BigDecimal cantidad(BigDecimal neto, BigDecimal precio) {
        return neto.divide(precio, PosicionCripto.DECIMALES_CANTIDAD, RoundingMode.DOWN);
    }
}
