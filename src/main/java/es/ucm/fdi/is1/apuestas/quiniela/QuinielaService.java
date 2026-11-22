package es.ucm.fdi.is1.apuestas.quiniela;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.data.domain.PageRequest;

import es.ucm.fdi.is1.apuestas.apuesta.JuegoResponsableService;
import es.ucm.fdi.is1.apuestas.cuotas.Resultado;
import es.ucm.fdi.is1.apuestas.eventos.EstadoEvento;
import es.ucm.fdi.is1.apuestas.eventos.Evento;
import es.ucm.fdi.is1.apuestas.eventos.EventoRepository;
import es.ucm.fdi.is1.apuestas.notificaciones.NotificacionService;
import es.ucm.fdi.is1.apuestas.usuarios.TipoMovimiento;
import es.ucm.fdi.is1.apuestas.usuarios.Usuario;
import es.ucm.fdi.is1.apuestas.usuarios.UsuarioRepository;
import es.ucm.fdi.is1.apuestas.web.Monedas;

/**
 * Quinielas (HU-78 a HU-80): el creador las monta, los usuarios echan un boleto y, cuando se han jugado todos los
 * partidos, el bote se reparte entre los que más aciertan. La casa no arriesga nada: solo se queda la comisión.
 */
@Service
public class QuinielaService {

    private final QuinielaRepository quinielas;
    private final BoletoQuinielaRepository boletos;
    private final EventoRepository eventos;
    private final UsuarioRepository usuarios;
    private final JuegoResponsableService juegoResponsable;
    private final NotificacionService notificaciones;
    private final Clock reloj;

    public QuinielaService(QuinielaRepository quinielas, BoletoQuinielaRepository boletos, EventoRepository eventos,
                           UsuarioRepository usuarios, JuegoResponsableService juegoResponsable,
                           NotificacionService notificaciones, Clock reloj) {
        this.quinielas = quinielas;
        this.boletos = boletos;
        this.eventos = eventos;
        this.usuarios = usuarios;
        this.juegoResponsable = juegoResponsable;
        this.notificaciones = notificaciones;
        this.reloj = reloj;
    }

    /** Lo que se muestra de una quiniela en las listas: cuántos boletos hay y el bote que se lleva acumulado. */
    public record Resumen(Quiniela quiniela, long boletos, BigDecimal bote, boolean admiteBoletos) {
    }

    /** Página acotada de la lista pública, con indicadores de navegación sin COUNT(*) global. */
    public record Pagina(List<Resumen> resumenes, int numero, boolean anterior, boolean siguiente) {
    }

    private static final int TAMANO_PAGINA = 20;

    @Transactional(readOnly = true)
    public Pagina pagina(int numero) {
        int seguro = Math.min(Math.max(0, numero), 500);
        List<Quiniela> consultadas = quinielas.findAllByOrderByCreadaDescIdDesc(
                PageRequest.of(seguro, TAMANO_PAGINA + 1));
        boolean siguiente = consultadas.size() > TAMANO_PAGINA;
        List<Quiniela> visibles = siguiente ? consultadas.subList(0, TAMANO_PAGINA) : consultadas;
        if (visibles.isEmpty()) {
            return new Pagina(List.of(), seguro, seguro > 0, false);
        }
        Map<Long, Long> boletosPorQuiniela = new HashMap<>();
        for (Object[] fila : boletos.contarPorQuiniela(visibles.stream().map(Quiniela::getId).toList())) {
            boletosPorQuiniela.put((Long) fila[0], ((Number) fila[1]).longValue());
        }
        LocalDateTime ahora = LocalDateTime.now(reloj);
        List<Resumen> resumenes = visibles.stream()
                .map(q -> resumen(q, ahora, boletosPorQuiniela.getOrDefault(q.getId(), 0L))).toList();
        return new Pagina(resumenes, seguro, seguro > 0, siguiente);
    }

    @Transactional(readOnly = true)
    public List<Resumen> todas() {
        LocalDateTime ahora = LocalDateTime.now(reloj);
        List<Quiniela> lista = quinielas.findAllByOrderByCreadaDesc();
        if (lista.isEmpty()) {
            return List.of();
        }
        Map<Long, Long> boletosPorQuiniela = new HashMap<>();
        for (Object[] fila : boletos.contarPorQuiniela(lista.stream().map(Quiniela::getId).toList())) {
            boletosPorQuiniela.put((Long) fila[0], ((Number) fila[1]).longValue());
        }
        return lista.stream().map(q -> resumen(q, ahora, boletosPorQuiniela.getOrDefault(q.getId(), 0L))).toList();
    }

    @Transactional(readOnly = true)
    public Resumen resumen(Long id) {
        return resumen(quiniela(id), LocalDateTime.now(reloj));
    }

    private Resumen resumen(Quiniela quiniela, LocalDateTime ahora) {
        return resumen(quiniela, ahora, boletos.countByQuiniela(quiniela));
    }

    private Resumen resumen(Quiniela quiniela, LocalDateTime ahora, long n) {
        // Los partidos se cargan aquí: la página se pinta fuera de la transacción y los cuenta. Antes solo se
        // cargaban de paso al mirar si admite boletos, que en una repartida o anulada no los recorre (error 500)
        quiniela.getPartidos().forEach(p -> p.getEvento().getLocal().getNombre());
        return new Resumen(quiniela, n, quiniela.bote(quiniela.getPrecio().multiply(BigDecimal.valueOf(n))),
                quiniela.admiteBoletos(ahora));
    }

    /** @throws ResponseStatusException 404 si no existe */
    @Transactional(readOnly = true)
    public Quiniela quiniela(Long id) {
        Quiniela quiniela = quinielas.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe esa quiniela"));
        // Se cargan aquí los partidos y sus eventos: la página se pinta fuera de la transacción
        quiniela.getPartidos().forEach(p -> p.getEvento().getLocal().getNombre());
        return quiniela;
    }

    /** Partidos que se pueden poner en una quiniela: programados, sin empezar y de deportes con empate (CA-78.1). */
    @Transactional(readOnly = true)
    public List<Evento> partidosDisponibles() {
        return eventos.findByEstadoAndFechaHoraAfterOrderByFechaHoraAsc(EstadoEvento.PROGRAMADO, LocalDateTime.now(reloj))
                .stream().filter(e -> e.getDeporte().isAdmiteEmpate()).toList();
    }

    /**
     * Crea una quiniela con los partidos en orden de fecha (HU-78).
     *
     * @throws IllegalArgumentException si no hay de 3 a 15 partidos válidos, falta el nombre o el precio no es válido
     */
    @Transactional
    public Quiniela crear(String nombre, BigDecimal precio, List<Long> eventoIds) {
        LocalDateTime ahora = LocalDateTime.now(reloj);
        if (nombre == null || nombre.isBlank() || nombre.strip().length() > 100) {
            throw new IllegalArgumentException("Ponle un nombre de 100 caracteres como mucho");
        }
        if (precio == null || precio.signum() <= 0 || precio.stripTrailingZeros().scale() > 2
                || precio.compareTo(new BigDecimal("100000")) > 0) {
            throw new IllegalArgumentException(
                    "El precio del boleto tiene que ser mayor que 0, hasta 100.000 y con 2 decimales como mucho");
        }
        // LinkedHashSet quita los repetidos
        List<Long> ids = eventoIds == null ? List.of() : List.copyOf(new LinkedHashSet<>(eventoIds));
        if (ids.size() < Quiniela.MIN_PARTIDOS || ids.size() > Quiniela.MAX_PARTIDOS) {
            throw new IllegalArgumentException("Una quiniela lleva de " + Quiniela.MIN_PARTIDOS + " a "
                    + Quiniela.MAX_PARTIDOS + " partidos; has elegido " + ids.size());
        }
        List<Evento> elegidos = eventos.findAllById(ids).stream()
                .sorted(Comparator.comparing(Evento::getFechaHora).thenComparing(Evento::getId)).toList();
        if (elegidos.size() != ids.size()) {
            throw new IllegalArgumentException("Alguno de los partidos ya no existe");
        }
        for (Evento evento : elegidos) {
            if (evento.getEstado() != EstadoEvento.PROGRAMADO || evento.haEmpezado(ahora)) {
                throw new IllegalArgumentException(titulo(evento) + " ya ha empezado o no está programado");
            }
            if (!evento.getDeporte().isAdmiteEmpate()) {
                throw new IllegalArgumentException("En " + evento.getDeporte().getNombre()
                        + " no hay empate: no vale para una quiniela");
            }
        }
        return quinielas.save(new Quiniela(nombre.strip(), precio, elegidos, ahora));
    }

    /** El boleto del usuario en la quiniela, si lo ha echado. */
    @Transactional(readOnly = true)
    public Optional<BoletoQuiniela> boletoDe(String email, Long id) {
        return usuarios.findByEmail(email).flatMap(u -> boletos.findByQuinielaAndUsuario(quiniela(id), u));
    }

    /** Todos los boletos, con su usuario: para ver el reparto. */
    @Transactional(readOnly = true)
    public List<BoletoQuiniela> boletos(Long id) {
        List<BoletoQuiniela> lista = boletos.findByQuinielaOrderByIdAsc(quiniela(id));
        lista.forEach(b -> b.getUsuario().getNombre());
        return lista;
    }

    /**
     * Echa un boleto (HU-79): se cobra el precio. Uno por usuario.
     *
     * @throws IllegalStateException    si la quiniela ya no admite boletos (CA-79.2)
     * @throws IllegalArgumentException si es el creador (CA-79.3), ya tiene boleto o falta algún pronóstico
     */
    @Transactional
    public BoletoQuiniela echarBoleto(String email, Long id, List<Resultado> pronosticos) {
        LocalDateTime ahora = LocalDateTime.now(reloj);
        // Con bloqueo optimista: si a la vez se anula o se reparte, falla una de las dos (ver paraEcharBoleto)
        Quiniela quiniela = quinielas.paraEcharBoleto(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe esa quiniela"));
        Usuario usuario = usuarios.findByEmail(email).orElseThrow();
        if (usuario.isCreador()) {
            throw new IllegalArgumentException("El creador de apuestas no puede jugar a la quiniela");
        }
        if (!quiniela.admiteBoletos(ahora)) {
            throw new IllegalStateException("La quiniela está cerrada: ya ha empezado algún partido o alguno está suspendido o anulado");
        }
        if (boletos.findByQuinielaAndUsuario(quiniela, usuario).isPresent()) {
            throw new IllegalArgumentException("Ya has echado tu boleto en esta quiniela");
        }
        if (pronosticos == null || pronosticos.size() != quiniela.getPartidos().size()
                || pronosticos.stream().anyMatch(p -> p == null)) {
            throw new IllegalArgumentException("Elige 1, X o 2 en cada partido");
        }
        juegoResponsable.comprobar(usuario, quiniela.getPrecio(), ahora);
        usuario.cargar(quiniela.getPrecio(), TipoMovimiento.QUINIELA_BOLETO,
                "Boleto de la quiniela «" + quiniela.getNombre() + "»", ahora);
        return boletos.save(new BoletoQuiniela(quiniela, usuario, pronosticos, ahora));
    }

    /**
     * Reparte el bote a partes iguales entre los boletos con más aciertos (HU-80). Si nadie ha acertado ninguno, se
     * devuelve el precio de todos (CA-80.3). Cada jugador recibe un aviso.
     *
     * @throws IllegalStateException si no está abierta o falta algún partido por jugar
     */
    @Transactional
    public Quiniela repartir(Long id) {
        LocalDateTime ahora = LocalDateTime.now(reloj);
        Quiniela quiniela = quiniela(id);
        if (!quiniela.isListaParaRepartir()) {
            throw new IllegalStateException(quiniela.getEstado() == EstadoQuiniela.ABIERTA
                    ? "Faltan partidos por jugar" : "Esta quiniela ya está " + quiniela.getEstado().name().toLowerCase(Locale.ROOT));
        }
        List<BoletoQuiniela> todos = boletos.findByQuinielaOrderByIdAsc(quiniela);
        todos.forEach(BoletoQuiniela::contarAciertos);
        // Los boletos de cuentas eliminadas no ganan: su parte no se le podría pagar a nadie y se la quitaría a los
        // que sí han acertado (el bote sí incluye lo que pagaron)
        List<BoletoQuiniela> vivos = todos.stream().filter(b -> !b.getUsuario().isEliminado()).toList();
        int maximo = vivos.stream().mapToInt(BoletoQuiniela::getAciertos).max().orElse(0);
        int partidos = quiniela.getPartidos().size();
        // Con todos los partidos anulados no se ha jugado nada: se devuelve el precio entero, sin comisión (si no,
        // todos "acertarían" todo y cobrarían menos de lo que pagaron)
        boolean todosAnulados = quiniela.getPartidos().stream()
                .allMatch(p -> p.getEvento().getEstado() == EstadoEvento.ANULADO);
        if (todosAnulados) {
            todos.forEach(b -> devolver(b, "Se han anulado todos los partidos de la quiniela «" + quiniela.getNombre()
                    + "»: te hemos devuelto " + Monedas.texto(b.getPagado()) + " monedas.", ahora));
        } else if (vivos.stream().noneMatch(BoletoQuiniela::aciertaAlgunJugado)) {
            // Los anulados cuentan como acierto para todos (CA-80.2), pero si nadie acierta ningún partido jugado
            // es como si nadie acertara nada (CA-80.3): si no, todos "ganarían" y perderían la comisión
            todos.forEach(b -> devolver(b, "Nadie ha acertado ningún partido de la quiniela «" + quiniela.getNombre()
                    + "»: te hemos devuelto " + Monedas.texto(b.getPagado()) + " monedas.", ahora));
        } else {
            List<BoletoQuiniela> ganadores = vivos.stream().filter(b -> b.getAciertos() == maximo).toList();
            BigDecimal bote = quiniela.bote(todos.stream().map(BoletoQuiniela::getPagado).reduce(BigDecimal.ZERO, BigDecimal::add));
            // Hacia abajo: los céntimos que no se pueden repartir se quedan en la casa
            BigDecimal parte = bote.divide(BigDecimal.valueOf(ganadores.size()), 2, RoundingMode.DOWN);
            for (BoletoQuiniela boleto : todos) {
                String aciertos = boleto.getAciertos() + " de " + partidos;
                if (ganadores.contains(boleto)) {
                    boleto.premiar(parte);
                    boleto.getUsuario().abonar(parte, TipoMovimiento.QUINIELA_PREMIO,
                            "Premio de la quiniela «" + quiniela.getNombre() + "»", ahora);
                    notificaciones.avisar(boleto.getUsuario(), "ganada", "¡Has ganado " + Monedas.texto(parte)
                            + " monedas en la quiniela «" + quiniela.getNombre() + "» con " + aciertos + " aciertos!");
                } else {
                    notificaciones.avisar(boleto.getUsuario(), "perdida", "Se ha repartido la quiniela «"
                            + quiniela.getNombre() + "»: has acertado " + aciertos + " y el bote ha sido para los que acertaron "
                            + maximo + ".");
                }
            }
        }
        quiniela.repartida();
        return quiniela;
    }

    /**
     * Anula la quiniela antes de repartirla y devuelve el precio de todos los boletos (CA-80.3).
     *
     * @throws IllegalStateException si ya se repartió o se anuló
     */
    @Transactional
    public Quiniela anular(Long id) {
        LocalDateTime ahora = LocalDateTime.now(reloj);
        Quiniela quiniela = quiniela(id);
        if (quiniela.getEstado() != EstadoQuiniela.ABIERTA) {
            throw new IllegalStateException("Esta quiniela ya está " + quiniela.getEstado().name().toLowerCase(Locale.ROOT));
        }
        boletos.findByQuinielaOrderByIdAsc(quiniela).forEach(b -> devolver(b, "Se ha anulado la quiniela «"
                + quiniela.getNombre() + "»: te hemos devuelto " + Monedas.texto(b.getPagado()) + " monedas.", ahora));
        quiniela.anular();
        return quiniela;
    }

    private void devolver(BoletoQuiniela boleto, String aviso, LocalDateTime ahora) {
        boleto.devolver();
        boleto.getUsuario().abonar(boleto.getPagado(), TipoMovimiento.DEVOLUCION,
                "Devolución de la quiniela «" + boleto.getQuiniela().getNombre() + "»", ahora);
        notificaciones.avisar(boleto.getUsuario(), "anulada", aviso);
    }

    private static String titulo(Evento evento) {
        return evento.getLocal().getNombre() + " – " + evento.getVisitante().getNombre();
    }

}
