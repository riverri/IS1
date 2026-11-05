package es.ucm.fdi.is1.apuestas.prediccion;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import es.ucm.fdi.is1.apuestas.apuesta.JuegoResponsableService;
import es.ucm.fdi.is1.apuestas.apuesta.LimitesService;
import es.ucm.fdi.is1.apuestas.ligas.Liga;
import es.ucm.fdi.is1.apuestas.ligas.LigaRepository;
import es.ucm.fdi.is1.apuestas.notificaciones.NotificacionService;
import es.ucm.fdi.is1.apuestas.usuarios.TipoMovimiento;
import es.ucm.fdi.is1.apuestas.usuarios.Usuario;
import es.ucm.fdi.is1.apuestas.usuarios.UsuarioRepository;
import es.ucm.fdi.is1.apuestas.web.GraficoLinea;

/**
 * Mercados de predicción (HU-63 a HU-68): crear, cerrar, anular y resolver (creador), comprar y vender
 * participaciones (usuarios), estadísticas y las posiciones de los compañeros de liga.
 *
 * <p>Antes de cobrar se comprueba lo mismo que en las apuestas: mercado abierto, el creador no juega, importe dentro de
 * los límites de apuesta (HU-07), precio sin cambios (CA-64.3), juego responsable (HU-10) y saldo. Cada cobro y cada
 * pago es un movimiento de saldo (HU-55). La comisión se redondea hacia arriba y lo que se paga, hacia abajo: nunca a
 * favor del usuario.
 */
@Service
public class PrediccionService {

    private final MercadoPrediccionRepository mercados;
    private final PosicionPrediccionRepository posiciones;
    private final OperacionPrediccionRepository operaciones;
    private final UsuarioRepository usuarios;
    private final LigaRepository ligas;
    private final LimitesService limites;
    private final JuegoResponsableService juegoResponsable;
    private final NotificacionService notificaciones;
    private final Clock reloj;

    public PrediccionService(MercadoPrediccionRepository mercados, PosicionPrediccionRepository posiciones,
                             OperacionPrediccionRepository operaciones, UsuarioRepository usuarios,
                             LigaRepository ligas, LimitesService limites, JuegoResponsableService juegoResponsable,
                             NotificacionService notificaciones, Clock reloj) {
        this.mercados = mercados;
        this.posiciones = posiciones;
        this.operaciones = operaciones;
        this.usuarios = usuarios;
        this.ligas = ligas;
        this.limites = limites;
        this.juegoResponsable = juegoResponsable;
        this.notificaciones = notificaciones;
        this.reloj = reloj;
    }

    // --- Consulta ---

    @Transactional(readOnly = true)
    public List<MercadoPrediccion> todos() {
        return mercados.findAllByOrderByCierreAsc();
    }

    /** @throws ResponseStatusException 404 si no existe */
    @Transactional(readOnly = true)
    public MercadoPrediccion mercado(Long id) {
        return mercados.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe el mercado " + id));
    }

    /** Lo que se recibiría al comprar, sin comprar todavía (CA-64.1). */
    @Transactional(readOnly = true)
    public Cotizacion cotizarCompra(Long id, Lado lado, BigDecimal importe) {
        MercadoPrediccion mercado = mercado(id);
        BigDecimal comision = comision(mercado, importe);
        BigDecimal participaciones = mercado.cotizarCompra(lado, importe.subtract(comision));
        double qSi = mercado.getQSi().doubleValue() + (lado == Lado.SI ? participaciones.doubleValue() : 0);
        double qNo = mercado.getQNo().doubleValue() + (lado == Lado.NO ? participaciones.doubleValue() : 0);
        BigDecimal precioDespues = BigDecimal.valueOf(Lmsr.precioSi(qSi, qNo, mercado.getLiquidez().doubleValue()))
                .setScale(4, RoundingMode.HALF_UP);
        return new Cotizacion(lado, importe, comision, participaciones, precioDespues);
    }

    /**
     * Previsión de una compra: cuánto se paga y cuántas participaciones se reciben.
     *
     * @param precioSiDespues precio del sí que quedaría después de comprar
     */
    public record Cotizacion(Lado lado, BigDecimal importe, BigDecimal comision, BigDecimal participaciones,
                             BigDecimal precioSiDespues) {

        /** Lo pagado por cada participación, comisión incluida. */
        public BigDecimal getPrecioMedio() {
            return participaciones.signum() == 0 ? BigDecimal.ZERO
                    : importe.divide(participaciones, 4, RoundingMode.HALF_UP);
        }
    }

    /** Estadísticas del mercado (HU-67): precio, volumen, participantes y evolución del precio del sí. */
    @Transactional(readOnly = true)
    public Estadisticas estadisticas(Long id) {
        MercadoPrediccion mercado = mercado(id);
        List<Double> precios = new ArrayList<>();
        // El mercado empieza en 0,50
        precios.add(0.5);
        operaciones.findByMercadoOrderByFechaAscIdAsc(mercado)
                .forEach(o -> precios.add(o.getPrecioSiDespues().doubleValue()));
        return new Estadisticas(operaciones.volumen(mercado), operaciones.participantes(mercado),
                precios.size() - 1, GraficoLinea.de(precios, 0.0, 1.0));
    }

    /** @param grafico null si aún no hay operaciones */
    public record Estadisticas(BigDecimal volumen, long participantes, int operaciones, GraficoLinea grafico) {
    }

    /** Las posiciones del usuario en un mercado. */
    @Transactional(readOnly = true)
    public List<PosicionPrediccion> mias(String email, Long id) {
        return posiciones.findByUsuarioAndMercado(usuario(email), mercado(id)).stream()
                .filter(PosicionPrediccion::isConParticipaciones).toList();
    }

    /**
     * Posiciones de quienes comparten alguna liga privada con el usuario (HU-68, CA-68.1). Solo se ven entre
     * compañeros de liga (CA-68.2): unirse a una liga es aceptar compartir con ellos. El perfil público no las enseña,
     * igual que no enseña las apuestas (HU-48).
     */
    @Transactional(readOnly = true)
    public List<PosicionPrediccion> deMisLigas(String email, Long id) {
        Usuario yo = usuario(email);
        Set<Long> companeros = new HashSet<>();
        for (Liga liga : ligas.deMiembro(yo)) {
            companeros.addAll(liga.getIdsMiembros());
        }
        companeros.remove(yo.getId());
        if (companeros.isEmpty()) {
            return List.of();
        }
        return posiciones.findByMercadoAndUsuarioIdIn(mercado(id), companeros).stream()
                .filter(PosicionPrediccion::isConParticipaciones)
                .filter(p -> !p.getUsuario().isEliminado())
                .toList();
    }

    // --- Comprar y vender ---

    /**
     * Compra participaciones con {@code importe} moneditas, comisión incluida (HU-64).
     *
     * @param precioVisto precio del sí que tenía delante el usuario; null para no comprobarlo
     * @throws PrecioCambiadoException si el precio ha cambiado desde entonces (CA-64.3)
     * @throws IllegalStateException   si el mercado no admite operaciones (CA-64.2)
     * @throws IllegalArgumentException si el importe no es válido o no respeta los límites, por el juego responsable
     *                                  o si es el creador (CA-64.2)
     * @throws es.ucm.fdi.is1.apuestas.usuarios.SaldoInsuficienteException si no hay saldo (CA-64.2)
     */
    @Transactional
    public PosicionPrediccion comprar(String email, Long id, Lado lado, BigDecimal importe, BigDecimal precioVisto) {
        LocalDateTime ahora = LocalDateTime.now(reloj);
        MercadoPrediccion mercado = abierto(id, ahora);
        Usuario usuario = jugador(email);
        comprobarImporte(lado, importe);
        limites.actuales().comprobarImporte(importe);
        comprobarPrecio(mercado, precioVisto);
        juegoResponsable.comprobar(usuario, importe, ahora);
        usuario.cargar(importe, TipoMovimiento.PREDICCION_COMPRA,
                "Compra de «" + lado.getNombre() + "»: " + mercado.getPregunta(), ahora);

        BigDecimal comision = comision(mercado, importe);
        BigDecimal participaciones = mercado.comprar(lado, importe.subtract(comision));
        PosicionPrediccion posicion = posicion(usuario, mercado, lado);
        posicion.sumar(participaciones, importe);
        operaciones.save(new OperacionPrediccion(usuario, mercado, lado, OperacionPrediccion.Tipo.COMPRA,
                participaciones, importe, comision, ahora));
        return posicion;
    }

    /** Lo que se recibiría al vender, sin vender todavía. */
    @Transactional(readOnly = true)
    public BigDecimal cotizarVenta(Long id, Lado lado, BigDecimal participaciones) {
        MercadoPrediccion mercado = mercado(id);
        BigDecimal bruto = mercado.cotizarVenta(lado, participaciones);
        return bruto.subtract(comision(mercado, bruto));
    }

    /**
     * Vende participaciones antes del cierre (HU-65): recibe su valor menos la comisión y el precio baja (CA-65.1).
     *
     * @throws IllegalArgumentException si vende más de las que tiene (CA-65.2) o la cantidad no es válida
     * @throws IllegalStateException    si el mercado no admite operaciones
     * @throws PrecioCambiadoException  si el precio ha cambiado desde que lo vio
     */
    @Transactional
    public BigDecimal vender(String email, Long id, Lado lado, BigDecimal participaciones, BigDecimal precioVisto) {
        LocalDateTime ahora = LocalDateTime.now(reloj);
        MercadoPrediccion mercado = abierto(id, ahora);
        Usuario usuario = jugador(email);
        if (lado == null || participaciones == null || participaciones.signum() <= 0) {
            throw new IllegalArgumentException("Indica cuántas participaciones quieres vender");
        }
        participaciones = participaciones.setScale(MercadoPrediccion.DECIMALES_PARTICIPACIONES, RoundingMode.DOWN);
        PosicionPrediccion posicion = posiciones.findByUsuarioAndMercadoAndLado(usuario, mercado, lado)
                .orElseThrow(() -> new IllegalArgumentException("No tienes participaciones de «" + lado.getNombre()
                        + "» en este mercado"));
        // Primero se comprueba que las tiene (CA-65.2); después se mueve el precio
        if (participaciones.compareTo(posicion.getParticipaciones()) > 0) {
            throw new IllegalArgumentException("No tienes tantas participaciones: tienes "
                    + posicion.getParticipaciones().stripTrailingZeros().toPlainString().replace('.', ','));
        }
        comprobarPrecio(mercado, precioVisto);
        BigDecimal bruto = mercado.vender(lado, participaciones);
        BigDecimal comision = comision(mercado, bruto);
        BigDecimal neto = bruto.subtract(comision);
        posicion.restar(participaciones, neto);
        usuario.abonar(neto, TipoMovimiento.PREDICCION_VENTA,
                "Venta de «" + lado.getNombre() + "»: " + mercado.getPregunta(), ahora);
        operaciones.save(new OperacionPrediccion(usuario, mercado, lado, OperacionPrediccion.Tipo.VENTA,
                participaciones, neto, comision, ahora));
        return neto;
    }

    // --- Gestión (creador) ---

    /** Crea un mercado abierto con el precio en 0,50 (CA-63.1). */
    @Transactional
    public MercadoPrediccion crear(String pregunta, String descripcion, LocalDateTime cierre, BigDecimal liquidez,
                                   BigDecimal comisionPorcentaje) {
        LocalDateTime ahora = LocalDateTime.now(reloj);
        if (cierre == null || !cierre.isAfter(ahora)) {
            throw new IllegalArgumentException("La fecha de cierre tiene que ser futura");
        }
        BigDecimal comision = comisionPorcentaje.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
        return mercados.save(new MercadoPrediccion(pregunta, descripcion, cierre, liquidez, comision, ahora));
    }

    /** Deja de admitir compras y ventas antes de la fecha (CA-63.2). */
    @Transactional
    public void cerrar(Long id) {
        mercado(id).cerrar();
    }

    /**
     * Marca (o corrige) la respuesta y paga 1 monedita por participación ganadora (CA-66.1). Si se corrige, cada saldo
     * se ajusta por la diferencia con lo ya pagado, como en las apuestas (CA-66.2, ADR 0002).
     *
     * @return cuántas posiciones se han liquidado
     */
    @Transactional
    public int resolver(Long id, Lado respuesta) {
        MercadoPrediccion mercado = mercado(id);
        boolean correccion = mercado.resolver(respuesta);
        LocalDateTime ahora = LocalDateTime.now(reloj);
        int liquidadas = 0;
        for (PosicionPrediccion posicion : posiciones.findByMercado(mercado)) {
            BigDecimal debe = posicion.debeCobrar(respuesta);
            BigDecimal diferencia = debe.subtract(posicion.getPagado());
            if (diferencia.signum() != 0) {
                posicion.getUsuario().ajustar(diferencia,
                        correccion ? TipoMovimiento.CORRECCION : TipoMovimiento.PREDICCION_PAGO,
                        "Respuesta «" + respuesta.getNombre() + "»: " + mercado.getPregunta(), ahora);
            }
            posicion.marcarPagado(debe);
            if (posicion.isConParticipaciones()) {
                avisar(posicion, respuesta, correccion, debe);
                liquidadas++;
            }
        }
        return liquidadas;
    }

    /**
     * Anula el mercado y devuelve a cada uno lo que puso menos lo que recuperó vendiendo (CA-63.3). Si ya había
     * recuperado más de lo que puso, no se le quita nada.
     */
    @Transactional
    public int anular(Long id) {
        MercadoPrediccion mercado = mercado(id);
        mercado.anular();
        LocalDateTime ahora = LocalDateTime.now(reloj);
        int devueltas = 0;
        for (PosicionPrediccion posicion : posiciones.findByMercado(mercado)) {
            if (posicion.getInvertido().signum() > 0) {
                posicion.getUsuario().abonar(posicion.getInvertido(), TipoMovimiento.DEVOLUCION,
                        "Mercado anulado: " + mercado.getPregunta(), ahora);
                notificaciones.avisar(posicion.getUsuario(), "anulada", "Se ha anulado el mercado «"
                        + mercado.getPregunta() + "». Te hemos devuelto " + monedas(posicion.getInvertido())
                        + " monedas.");
                devueltas++;
            }
        }
        return devueltas;
    }

    /** Para el panel de la casa: lo cobrado en compras, lo pagado en ventas y lo pagado al resolver. */
    @Transactional(readOnly = true)
    public CuentasPrediccion cuentas() {
        BigDecimal compras = BigDecimal.ZERO;
        BigDecimal ventas = BigDecimal.ZERO;
        for (Object[] fila : operaciones.importesPorTipo()) {
            if (fila[0] == OperacionPrediccion.Tipo.COMPRA) {
                compras = (BigDecimal) fila[1];
            } else {
                ventas = (BigDecimal) fila[1];
            }
        }
        BigDecimal pagos = posiciones.findAll().stream().map(PosicionPrediccion::getPagado)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new CuentasPrediccion(mercados.count(), compras, ventas, pagos);
    }

    /** Lo que se queda la casa: compras − ventas − pagos (con los mercados aún abiertos, provisional). */
    public record CuentasPrediccion(long mercados, BigDecimal compras, BigDecimal ventas, BigDecimal pagos) {

        public BigDecimal getBeneficio() {
            return compras.subtract(ventas).subtract(pagos);
        }
    }

    // --- Ayudas ---

    private MercadoPrediccion abierto(Long id, LocalDateTime ahora) {
        MercadoPrediccion mercado = mercado(id);
        if (!mercado.admiteOperaciones(ahora)) {
            throw new IllegalStateException("Este mercado ya no admite compras ni ventas");
        }
        return mercado;
    }

    private Usuario usuario(String email) {
        return usuarios.findByEmail(email).orElseThrow();
    }

    private Usuario jugador(String email) {
        Usuario usuario = usuario(email);
        if (usuario.isCreador()) {
            throw new IllegalArgumentException("El creador de apuestas no puede operar en los mercados");
        }
        return usuario;
    }

    private static void comprobarImporte(Lado lado, BigDecimal importe) {
        if (lado == null) {
            throw new IllegalArgumentException("Elige «Sí» o «No»");
        }
        if (importe == null || importe.signum() <= 0 || importe.stripTrailingZeros().scale() > 2) {
            throw new IllegalArgumentException("Introduce un importe válido, con 2 decimales como mucho");
        }
    }

    private static void comprobarPrecio(MercadoPrediccion mercado, BigDecimal precioVisto) {
        if (precioVisto != null && precioVisto.compareTo(mercado.getPrecioSi()) != 0) {
            throw new PrecioCambiadoException();
        }
    }

    /** Comisión de la casa sobre un importe, redondeada hacia arriba a céntimos. */
    private static BigDecimal comision(MercadoPrediccion mercado, BigDecimal importe) {
        return importe.multiply(mercado.getComision()).setScale(2, RoundingMode.CEILING);
    }

    private PosicionPrediccion posicion(Usuario usuario, MercadoPrediccion mercado, Lado lado) {
        return posiciones.findByUsuarioAndMercadoAndLado(usuario, mercado, lado)
                .orElseGet(() -> posiciones.save(new PosicionPrediccion(usuario, mercado, lado)));
    }

    private void avisar(PosicionPrediccion posicion, Lado respuesta, boolean correccion, BigDecimal cobrado) {
        String prefijo = correccion ? "Respuesta corregida. " : "";
        if (posicion.getLado() == respuesta) {
            notificaciones.avisar(posicion.getUsuario(), "ganada", prefijo + "¡Has acertado «" + respuesta.getNombre()
                    + "» en «" + posicion.getMercado().getPregunta() + "»! Cobras " + monedas(cobrado) + " monedas.");
        } else {
            notificaciones.avisar(posicion.getUsuario(), "perdida", prefijo + "La respuesta a «"
                    + posicion.getMercado().getPregunta() + "» ha sido «" + respuesta.getNombre() + "».");
        }
    }

    private static String monedas(BigDecimal cantidad) {
        return cantidad.setScale(2, RoundingMode.DOWN).toPlainString().replace('.', ',');
    }
}
