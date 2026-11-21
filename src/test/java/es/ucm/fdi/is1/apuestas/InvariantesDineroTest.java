package es.ucm.fdi.is1.apuestas;

import static org.assertj.core.api.Assertions.fail;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import es.ucm.fdi.is1.apuestas.apuesta.Apuesta;
import es.ucm.fdi.is1.apuestas.apuesta.ApuestaRepository;
import es.ucm.fdi.is1.apuestas.apuesta.ApuestaService;
import es.ucm.fdi.is1.apuestas.apuesta.BajaService;
import es.ucm.fdi.is1.apuestas.apuesta.BeneficioDiarioService;
import es.ucm.fdi.is1.apuestas.apuesta.CuentasCasa;
import es.ucm.fdi.is1.apuestas.apuesta.CuentasCasaService;
import es.ucm.fdi.is1.apuestas.apuesta.EstadoApuesta;
import es.ucm.fdi.is1.apuestas.apuesta.EstadoSeleccion;
import es.ucm.fdi.is1.apuestas.apuesta.ResolucionMercadosService;
import es.ucm.fdi.is1.apuestas.apuesta.ResolucionService;
import es.ucm.fdi.is1.apuestas.apuesta.Seleccion;
import es.ucm.fdi.is1.apuestas.apuesta.SeleccionPedida;
import es.ucm.fdi.is1.apuestas.apuesta.SupercuotaService;
import es.ucm.fdi.is1.apuestas.cuotas.CalculadoraCuotas;
import es.ucm.fdi.is1.apuestas.cuotas.Especial;
import es.ucm.fdi.is1.apuestas.cuotas.Resultado;
import es.ucm.fdi.is1.apuestas.equipos.CompeticionRepository;
import es.ucm.fdi.is1.apuestas.equipos.Deporte;
import es.ucm.fdi.is1.apuestas.equipos.EquipoRepository;
import es.ucm.fdi.is1.apuestas.eventos.EstadoEvento;
import es.ucm.fdi.is1.apuestas.eventos.Evento;
import es.ucm.fdi.is1.apuestas.eventos.EventoRepository;
import es.ucm.fdi.is1.apuestas.gestion.EdicionEventoForm;
import es.ucm.fdi.is1.apuestas.gestion.EventoForm;
import es.ucm.fdi.is1.apuestas.gestion.GestionService;
import es.ucm.fdi.is1.apuestas.ligas.BloqueoLiga;
import es.ucm.fdi.is1.apuestas.ligas.BloqueoLigaRepository;
import es.ucm.fdi.is1.apuestas.ligas.BloqueoLigaService;
import es.ucm.fdi.is1.apuestas.ligas.EstadoReto;
import es.ucm.fdi.is1.apuestas.ligas.Liga;
import es.ucm.fdi.is1.apuestas.ligas.LigaRepository;
import es.ucm.fdi.is1.apuestas.ligas.LigaService;
import es.ucm.fdi.is1.apuestas.ligas.Reto;
import es.ucm.fdi.is1.apuestas.ligas.RetoRepository;
import es.ucm.fdi.is1.apuestas.ligas.RetoService;
import es.ucm.fdi.is1.apuestas.mercados.Candidato;
import es.ucm.fdi.is1.apuestas.mercados.EstadoMercado;
import es.ucm.fdi.is1.apuestas.mercados.Mercado;
import es.ucm.fdi.is1.apuestas.mercados.MercadoRepository;
import es.ucm.fdi.is1.apuestas.mercados.MercadoService;
import es.ucm.fdi.is1.apuestas.quiniela.BoletoQuiniela;
import es.ucm.fdi.is1.apuestas.quiniela.BoletoQuinielaRepository;
import es.ucm.fdi.is1.apuestas.quiniela.EstadoQuiniela;
import es.ucm.fdi.is1.apuestas.quiniela.Quiniela;
import es.ucm.fdi.is1.apuestas.quiniela.QuinielaRepository;
import es.ucm.fdi.is1.apuestas.quiniela.QuinielaService;
import es.ucm.fdi.is1.apuestas.usuarios.MovimientoSaldo;
import es.ucm.fdi.is1.apuestas.usuarios.MovimientoSaldoRepository;
import es.ucm.fdi.is1.apuestas.usuarios.Rol;
import es.ucm.fdi.is1.apuestas.usuarios.TipoMovimiento;
import es.ucm.fdi.is1.apuestas.usuarios.Usuario;
import es.ucm.fdi.is1.apuestas.usuarios.UsuarioRepository;
import es.ucm.fdi.is1.apuestas.usuarios.UsuarioService;

/**
 * Prueba aleatoria ("property-based" a mano) de que el dinero siempre cuadra. Crea unos cuantos usuarios, partidos,
 * mercados, ligas y quinielas y hace cientos de operaciones al azar a través de los servicios (como la web), con una
 * semilla fija que se imprime. Después de cada operación comprueba las invariantes de abajo; si una falla, la prueba
 * falla con la semilla, el número de operación y las últimas operaciones hechas, para poder repetirla.
 *
 * <p>La hora la da un reloj de prueba que empieza en {@link Hora#ahora()} y avanza cuando lo decide la prueba (así los
 * partidos empiezan, las recargas gratuitas tocan, etc.). Toda la prueba va en una transacción que se deshace al final.
 *
 * <p>Semilla y número de operaciones: {@code -Dinvariantes.semilla=...} y {@code -Dinvariantes.operaciones=...}.
 *
 * <p>Invariantes comprobadas:
 * <ol>
 * <li>U1. Saldo de cada usuario = suma de sus movimientos (la bienvenida es el primer movimiento, 1000) y la cadena de
 * {@code saldoResultante} cuadra movimiento a movimiento; ningún movimiento de importe 0; el saldo no queda negativo
 * tras un cobro (APUESTA, RETO_APUESTA, QUINIELA_BOLETO).</li>
 * <li>U2. Por usuario, los movimientos de apuestas (APUESTA, PREMIO, DEVOLUCION, CORRECCION que no son de quiniela ni de
 * reto) = suma de (pagado − importe) de sus apuestas no canceladas.</li>
 * <li>U3. Por usuario no eliminado, los movimientos de retos = lo que le toca según el estado de sus retos.</li>
 * <li>U4. Por usuario no eliminado, los movimientos de quiniela = suma de (premio − pagado) de sus boletos.</li>
 * <li>U5. Recargas de 200 cada una y separadas al menos 7 días; una cuenta eliminada tiene saldo 0.</li>
 * <li>B1. Por usuario no eliminado, los movimientos de bloqueos de liga (coste y devolución) = − coste de sus
 * bloqueos que siguen en pie (50 a un compañero, 100 a toda la liga) y de los ya usados que se borraron con su liga;
 * ningún bloqueo de un partido anulado.</li>
 * <li>A1. pagado según el estado (GANADA → ganancia potencial, ANULADA → importe, PERDIDA/ACTIVA/CANCELADA → 0,
 * COBRADA → lo cobrado, entre 0 y la ganancia potencial), salvo cuentas eliminadas; una ACTIVA siempre tiene pagado 0.</li>
 * <li>A2. El estado de cada apuesta sale de sus selecciones y el de cada selección del estado de su evento o mercado
 * (ninguna ACTIVA con todo resuelto, ninguna selección pendiente en un evento finalizado o anulado, etc.).</li>
 * <li>R1. Retos: PENDIENTE/ACEPTADO solo con el partido programado o suspendido; PENDIENTE solo entre quienes comparten
 * liga; RESUELTO con el ganador que corresponde al resultado; DEVUELTO aceptado solo si se anuló o nadie acertó.</li>
 * <li>Q1. Quinielas: abiertas sin premios; anuladas con todo devuelto; repartidas con lo repartido ≤ bote y la
 * diferencia solo de redondeo.</li>
 * <li>G1. Conservación: el dinero total (saldos + importe de apuestas activas + retos pendientes y aceptados + boletos
 * de quinielas abiertas) solo cambia por bienvenidas, recargas, bajas, el resultado de la casa en apuestas cerradas
 * (importe − pagado), la comisión de las quinielas y lo que ya no se abona a cuentas eliminadas.</li>
 * <li>C1. Cuentas de la casa ({@link CuentasCasaService}): en juego, apostado y pagado cambian igual que las apuestas;
 * las filas por tipo suman el total y las de deporte suman las simples.</li>
 * <li>C2. Beneficio diario de la casa ({@link BeneficioDiarioService}) = resultado de la casa en apuestas (importe −
 * pagado de las no canceladas) + quinielas + bloqueos de liga; no incluye dinero que solo pasa de un jugador a otro
 * (retos).</li>
 * </ol>
 */
@SpringBootTest
@Transactional
// Contexto propio (otro reloj): se cierra al terminar para no quedarse con conexiones abiertas
@DirtiesContext
class InvariantesDineroTest {

    private static final String PASSWORD = "secreta123";
    private static final BigDecimal BIENVENIDA = new BigDecimal("1000");
    private static final BigDecimal RECARGA = new BigDecimal("200");
    private static final BigDecimal CENTIMO = new BigDecimal("0.01");
    /** El beneficio diario abarca 30 días: la simulación no pasa de 25. */
    private static final Duration DURACION_MAXIMA = Duration.ofDays(25);

    /** Reloj que solo avanza cuando lo dice la prueba. */
    static class RelojMovil extends Clock {
        private Instant ahora = Instant.now();

        @Override
        public ZoneId getZone() {
            return Hora.MADRID;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return Clock.fixed(ahora, zone);
        }

        @Override
        public Instant instant() {
            return ahora;
        }

        void poner(LocalDateTime hora) {
            ahora = hora.atZone(Hora.MADRID).toInstant();
        }

        void avanzar(Duration duracion) {
            ahora = ahora.plus(duracion);
        }
    }

    @TestConfiguration
    static class ConReloj {
        @Bean
        @Primary
        RelojMovil relojMovil() {
            return new RelojMovil();
        }
    }

    @Autowired private RelojMovil reloj;
    @Autowired private UsuarioService usuarioService;
    @Autowired private UsuarioRepository usuarios;
    @Autowired private MovimientoSaldoRepository movimientos;
    @Autowired private ApuestaService apuestaService;
    @Autowired private ApuestaRepository apuestas;
    @Autowired private ResolucionService resolucion;
    @Autowired private ResolucionMercadosService resolucionMercados;
    @Autowired private MercadoService mercadoService;
    @Autowired private MercadoRepository mercadoRepository;
    @Autowired private SupercuotaService supercuotas;
    @Autowired private CalculadoraCuotas calculadora;
    @Autowired private GestionService gestion;
    @Autowired private EventoRepository eventos;
    @Autowired private CompeticionRepository competiciones;
    @Autowired private EquipoRepository equipos;
    @Autowired private LigaService ligaService;
    @Autowired private LigaRepository ligaRepository;
    @Autowired private RetoService retoService;
    @Autowired private RetoRepository retoRepository;
    @Autowired private QuinielaService quinielaService;
    @Autowired private QuinielaRepository quinielaRepository;
    @Autowired private BoletoQuinielaRepository boletoRepository;
    @Autowired private BajaService bajas;
    @Autowired private CuentasCasaService cuentasCasa;
    @Autowired private BeneficioDiarioService beneficioDiario;
    @Autowired private BloqueoLigaService bloqueos;
    @Autowired private BloqueoLigaRepository bloqueoRepository;

    /** Tipos de operación, con su peso al sortear. */
    enum Op {
        AVANZAR_RELOJ(6), CREAR_EVENTO(8), APOSTAR_SIMPLE(14), APOSTAR_ESPECIAL(6), APOSTAR_COMBINADA(9),
        APOSTAR_MERCADO(5), CANCELAR(5), CAMBIAR_IMPORTE(6), INTRODUCIR_RESULTADO(6), INTRODUCIR_MARCADOR(6),
        ANULAR_EVENTO(2), SUSPENDER(2), REACTIVAR(2), CAMBIAR_FECHA(2), CREAR_MERCADO(2), CERRAR_MERCADO(1),
        CAMBIAR_CUOTA_MERCADO(1), RESOLVER_MERCADO(2), ANULAR_MERCADO(1), SUPERCUOTA(2), CREAR_LIGA(1), UNIRSE_LIGA(3),
        SALIR_LIGA(2), BORRAR_LIGA(1), RETAR(10), ACEPTAR_RETO(8), RECHAZAR_RETO(2), CREAR_QUINIELA(2),
        ECHAR_BOLETO(8), REPARTIR_QUINIELA(3), ANULAR_QUINIELA(1), RECARGA(2), ELIMINAR_CUENTA(1), NUEVO_USUARIO(1),
        COBRAR_ANTES(5), BLOQUEAR(3);

        final int peso;

        Op(int peso) {
            this.peso = peso;
        }
    }

    // Estado de la simulación
    private Random azar;
    private final List<Long> misUsuarios = new ArrayList<>();
    private final Map<Long, String> emailDe = new HashMap<>();
    private final Set<Long> eliminados = new HashSet<>();
    private final List<Long> misEventos = new ArrayList<>();
    private final List<Long> misMercados = new ArrayList<>();
    private final List<Long> misLigas = new ArrayList<>();
    private final List<Long> misQuinielas = new ArrayList<>();
    private final Deque<String> ultimas = new ArrayDeque<>();
    private final Map<Op, int[]> cobertura = new EnumMap<>(Op.class);
    private final Map<String, Integer> excepciones = new TreeMap<>();
    private LocalDateTime inicio;
    private int usuariosCreados;
    private int mercadosCreados;
    private CuentasCasa.Fila casaInicial;
    private BigDecimal beneficioInicial;
    private int fase = 0;
    /** Coste de bloqueos que ya se usaron (su partido había empezado) y se borraron con su liga: no se devuelven. */
    private final Map<Long, BigDecimal> bloqueosUsados = new HashMap<>();

    /** Los bloqueos que desaparecen sin devolverse: ya jugados cuando se borró su liga. */
    private void apuntarBloqueosUsados(Map<Long, BloqueoLiga> antes, LocalDateTime hora) {
        for (BloqueoLiga b : antes.values()) {
            boolean jugado = b.getEvento().getEstado() != EstadoEvento.PROGRAMADO || !b.getEvento().getFechaHora().isAfter(hora);
            if (jugado && b.getEvento().getEstado() != EstadoEvento.ANULADO && !bloqueoRepository.existsById(b.getId())) {
                bloqueosUsados.merge(b.getCreador().getId(), coste(b), BigDecimal::add);
            }
        }
    }

    @Test
    void elDineroSiempreCuadra() {
        long semilla = Long.getLong("invariantes.semilla", 20261119L);
        int operaciones = Integer.getInteger("invariantes.operaciones", 800);
        System.out.println("InvariantesDineroTest: semilla=" + semilla + " operaciones=" + operaciones);
        azar = new Random(semilla);
        reloj.poner(Hora.ahora().withNano(0));
        inicio = LocalDateTime.now(reloj);
        casaInicial = cuentasCasa.cuentas().total();
        beneficioInicial = beneficioDiario.ultimosDias().total();
        preparar();
        Foto antes = foto();
        comprobar(antes, null, "preparación");
        for (int i = 1; i <= operaciones; i++) {
            Op op = sortear();
            Map<Long, BloqueoLiga> bloqueosAntes = new HashMap<>();
            bloqueoRepository.findAll().forEach(b -> bloqueosAntes.put(b.getId(), b));
            LocalDateTime horaAntes = ahora();
            String descripcion = ejecutar(op);
            apuntarBloqueosUsados(bloqueosAntes, horaAntes);
            ultimas.addLast("#" + i + " " + descripcion);
            if (ultimas.size() > 40) {
                ultimas.removeFirst();
            }
            Foto despues = foto();
            String fallo = comprobar(despues, antes, descripcion);
            if (fallo != null) {
                fail("Semilla " + semilla + ", operación " + i + ": " + fallo + "\nÚltimas operaciones:\n"
                        + String.join("\n", ultimas));
            }
            antes = despues;
        }
        StringBuilder informe = new StringBuilder("InvariantesDineroTest: " + operaciones
                + " operaciones sin fallos (semilla " + semilla + "). Cobertura (bien / excepción de negocio):\n");
        cobertura.forEach((op, c) -> informe.append(String.format("  %-22s %4d / %4d%n", op, c[0], c[1])));
        informe.append("Excepciones de negocio:\n");
        excepciones.forEach((e, n) -> informe.append(String.format("  %5d  %s%n", n, e)));
        informe.append(resumenFinal());
        System.out.println(informe);
    }

    /**
     * El único fallo que encontró esta prueba (C2 y conservación del dinero en retos): se corregía el resultado de un reto
     * aceptado después de que uno de los dos hubiera eliminado su cuenta. A la cuenta eliminada no se le retiraba nada
     * ({@code Usuario.ajustar}) y al otro sí se le abonaba: aparecían 20 monedas de la nada. Además el ajuste iba con el
     * tipo CORRECCION, que el beneficio diario cuenta como dinero de la casa. Ahora el reto se queda como se cerró.
     */
    @Test
    void corregirUnRetoConUnaCuentaEliminadaNoCreaDinero() {
        reloj.poner(Hora.ahora().withNano(0));
        inicio = LocalDateTime.now(reloj);
        azar = new Random(1);
        beneficioInicial = beneficioDiario.ultimosDias().total();
        String ana = nuevoUsuario();
        String luis = nuevoUsuario();
        Long idLuis = misUsuarios.get(1);
        ligaService.unirse(luis, ligaService.crear(ana, "Reto corregido").getCodigo());
        crearEvento(false);
        Long partido = misEventos.get(0);
        // 1. Ana reta a Luis a que gana el local, 10 cada uno; Luis acepta con el visitante
        Reto reto = retoService.retar(ana, idLuis, partido, Resultado.LOCAL, BigDecimal.TEN);
        retoService.aceptar(luis, reto.getId(), Resultado.VISITANTE);
        // 2. Gana el local: Ana cobra 20
        resolucion.introducirResultado(partido, Resultado.LOCAL);
        // 3. Ana elimina su cuenta (su saldo, con esos 20, pasa a 0)
        bajas.eliminarCuenta(ana, PASSWORD);
        eliminados.add(misUsuarios.get(0));
        BigDecimal luisAntes = usuarios.findById(idLuis).orElseThrow().getSaldo();
        BigDecimal beneficioAntes = beneficioDiario.ultimosDias().total();
        // 4. Se corrige: ganó el visitante. A Ana no se le puede retirar nada, pero Luis cobra 20
        resolucion.introducirResultado(partido, Resultado.VISITANTE);
        BigDecimal luisCobra = usuarios.findById(idLuis).orElseThrow().getSaldo().subtract(luisAntes);
        BigDecimal casa = beneficioDiario.ultimosDias().total().subtract(beneficioAntes);
        if (luisCobra.signum() != 0 || casa.signum() != 0) {
            fail("Al corregir el reto Luis cobra " + luisCobra + " (Ana ya cobró esos 20 y no se le retira nada: dinero "
                    + "creado) y el beneficio diario de la casa cambia " + casa + " por un reto entre jugadores");
        }
    }

    // ------------------------------------------------------------------ preparación

    private void preparar() {
        for (int i = 0; i < 6; i++) {
            nuevoUsuario();
        }
        for (int i = 0; i < 8; i++) {
            crearEvento(false);
        }
        crearEvento(true);
        crearEvento(true);
        crearMercado();
        String codigo = ligaService.crear(emailDe.get(misUsuarios.get(0)), "Liga inicial").getCodigo();
        misLigas.add(ligaRepository.findByCodigo(codigo).orElseThrow().getId());
        for (int i = 1; i < 6; i++) {
            ligaService.unirse(emailDe.get(misUsuarios.get(i)), codigo);
        }
    }

    private String nuevoUsuario() {
        String email = "inv-" + (usuariosCreados++) + "@ucm.es";
        Usuario u = usuarioService.crear(email, "Jugador " + usuariosCreados, PASSWORD, Rol.USUARIO);
        misUsuarios.add(u.getId());
        emailDe.put(u.getId(), email);
        return email;
    }

    private static final String[] FUTBOL = {"Real Madrid", "FC Barcelona", "Atlético de Madrid", "Villarreal CF",
            "Athletic Club", "Real Sociedad", "Real Betis", "Celta de Vigo", "Valencia CF", "Sevilla FC", "CA Osasuna",
            "Rayo Vallecano", "RCD Espanyol", "Getafe CF", "Deportivo Alavés", "Elche CF"};
    private static final String[] BALONCESTO = {"Real Madrid Baloncesto", "Barça Basket", "Unicaja", "Valencia Basket"};

    private String crearEvento(boolean baloncesto) {
        String[] nombres = baloncesto ? BALONCESTO : FUTBOL;
        int a = azar.nextInt(nombres.length);
        int b = (a + 1 + azar.nextInt(nombres.length - 1)) % nombres.length;
        EventoForm form = new EventoForm();
        form.setCompeticionId(competiciones.findByNombre(baloncesto ? "Liga ACB" : "LaLiga").orElseThrow().getId());
        form.setLocalId(equipos.findByNombre(nombres[a]).orElseThrow().getId());
        form.setVisitanteId(equipos.findByNombre(nombres[b]).orElseThrow().getId());
        form.setFechaHora(ahora().plusMinutes(30 + azar.nextInt(60 * 24 * 5)));
        Evento e = gestion.crearEvento(form);
        misEventos.add(e.getId());
        return "crear evento " + e.getId() + " " + nombres[a] + "–" + nombres[b] + " " + e.getFechaHora();
    }

    private String crearMercado() {
        int n = mercadosCreados++;
        Mercado m = mercadoService.crear("Mercado " + n, Deporte.FUTBOL, ahora().plusHours(6 + azar.nextInt(24 * 8)),
                "Candidata A" + n + "; 2,50\nCandidata B" + n + "; 3.10\nCandidata C" + n + "; 6");
        misMercados.add(m.getId());
        return "crear mercado " + m.getId();
    }

    // ------------------------------------------------------------------ operaciones

    private Op sortear() {
        int total = 0;
        for (Op op : Op.values()) {
            total += op.peso;
        }
        int r = azar.nextInt(total);
        for (Op op : Op.values()) {
            r -= op.peso;
            if (r < 0) {
                return op;
            }
        }
        throw new IllegalStateException();
    }

    private String ejecutar(Op op) {
        int[] c = cobertura.computeIfAbsent(op, o -> new int[2]);
        try {
            String hecho = hacer(op);
            c[0]++;
            return hecho;
        } catch (RuntimeException e) {
            if (!deNegocio(e)) {
                throw new AssertionError("Excepción inesperada en " + op + " (" + d + "):\n"
                        + String.join("\n", ultimas), e);
            }
            c[1]++;
            String clave = op + " → " + e.getClass().getSimpleName() + ": " + recortar(e.getMessage());
            excepciones.merge(clave, 1, Integer::sum);
            return d + " ✗ " + e.getClass().getSimpleName() + ": " + e.getMessage();
        }
    }

    private static String recortar(String s) {
        if (s == null) {
            return "";
        }
        s = s.replaceAll("\\d+", "N");
        return s.length() > 90 ? s.substring(0, 90) : s;
    }

    private static boolean deNegocio(RuntimeException e) {
        return e.getClass().getName().startsWith("es.ucm.fdi.is1.apuestas.")
                || e.getClass() == IllegalArgumentException.class || e.getClass() == IllegalStateException.class
                || e instanceof ResponseStatusException;
    }

    /** Descripción que se va completando: si la operación lanza, se ve hasta dónde llegó. */
    private String d;

    private String hacer(Op op) {
        d = op.name();
        return switch (op) {
            case AVANZAR_RELOJ -> avanzar();
            case CREAR_EVENTO -> crearEvento(azar.nextInt(5) == 0);
            case APOSTAR_SIMPLE -> apostar(1, false);
            case APOSTAR_ESPECIAL -> apostar(1, true);
            case APOSTAR_COMBINADA -> apostar(2 + azar.nextInt(3), azar.nextBoolean());
            case APOSTAR_MERCADO -> apostarMercado();
            case CANCELAR -> cancelar();
            case CAMBIAR_IMPORTE -> cambiarImporte();
            case INTRODUCIR_RESULTADO -> resultado();
            case INTRODUCIR_MARCADOR -> marcador();
            case ANULAR_EVENTO -> conEvento(null, id -> "anular evento " + id + " (" + resolucion.anular(id) + " sel.)");
            case SUSPENDER -> conEvento(e -> e.getEstado() == EstadoEvento.PROGRAMADO, id -> {
                resolucion.suspender(id);
                return "suspender evento " + id;
            });
            case REACTIVAR -> conEvento(e -> e.getEstado() == EstadoEvento.SUSPENDIDO, id -> {
                resolucion.reactivar(id);
                return "reactivar evento " + id;
            });
            case CAMBIAR_FECHA -> cambiarFecha();
            case CREAR_MERCADO -> crearMercado();
            case CERRAR_MERCADO -> conMercado(id -> {
                mercadoService.cerrar(id);
                return "cerrar mercado " + id;
            });
            case CAMBIAR_CUOTA_MERCADO -> conMercado(id -> {
                Candidato c = elegir(mercadoRepository.findById(id).orElseThrow().getCandidatos());
                BigDecimal cuota = BigDecimal.valueOf(150 + azar.nextInt(700), 2);
                mercadoService.cambiarCuota(id, c.getId(), cuota);
                return "cuota mercado " + id + " candidato " + c.getId() + " → " + cuota;
            });
            case RESOLVER_MERCADO -> conMercado(id -> {
                Candidato c = elegir(mercadoRepository.findById(id).orElseThrow().getCandidatos());
                d = "resolver mercado " + id + " gana " + c.getId();
                return d + " (" + resolucionMercados.resolver(id, c.getId()) + ")";
            });
            case ANULAR_MERCADO -> conMercado(id -> "anular mercado " + id + " (" + resolucionMercados.anular(id) + ")");
            case SUPERCUOTA -> supercuota();
            case CREAR_LIGA -> crearLiga();
            case UNIRSE_LIGA -> unirse();
            case SALIR_LIGA -> salir();
            case BORRAR_LIGA -> borrarLiga();
            case RETAR -> retar();
            case ACEPTAR_RETO -> aceptar();
            case RECHAZAR_RETO -> rechazar();
            case CREAR_QUINIELA -> crearQuiniela();
            case ECHAR_BOLETO -> echarBoleto();
            case REPARTIR_QUINIELA -> conQuiniela(Quiniela::isListaParaRepartir, id -> {
                quinielaService.repartir(id);
                return "repartir quiniela " + id;
            });
            case ANULAR_QUINIELA -> conQuiniela(q -> q.getEstado() == EstadoQuiniela.ABIERTA, id -> {
                quinielaService.anular(id);
                return "anular quiniela " + id;
            });
            case COBRAR_ANTES -> cobrarAntes();
            case BLOQUEAR -> bloquear();
            case RECARGA -> {
                String email = jugador();
                usuarioService.consultarConRecarga(email);
                yield "recarga " + email;
            }
            case ELIMINAR_CUENTA -> eliminar();
            case NUEVO_USUARIO -> {
                String email = nuevoUsuario();
                Liga l = liga();
                ligaService.unirse(email, l.getCodigo());
                yield "nuevo usuario " + email + " en liga " + l.getId();
            }
        };
    }

    private LocalDateTime ahora() {
        return LocalDateTime.now(reloj);
    }

    private String avanzar() {
        Duration paso = azar.nextInt(8) == 0 ? Duration.ofHours(12 + azar.nextInt(36))
                : Duration.ofMinutes(10 + azar.nextInt(60 * 4));
        if (Duration.between(inicio, ahora().plus(paso)).compareTo(DURACION_MAXIMA) > 0) {
            return "avanzar reloj (tope alcanzado)";
        }
        reloj.avanzar(paso);
        return "avanzar reloj " + paso + " → " + ahora();
    }

    private <T> T elegir(List<T> lista) {
        if (lista.isEmpty()) {
            throw new IllegalStateException("[prueba] nada que elegir");
        }
        return lista.get(azar.nextInt(lista.size()));
    }

    private List<Long> activos() {
        return misUsuarios.stream().filter(id -> !eliminados.contains(id)).toList();
    }

    private String jugador() {
        return emailDe.get(elegir(activos()));
    }

    private BigDecimal importe() {
        int r = azar.nextInt(20);
        if (r == 0) {
            return BigDecimal.valueOf(300 + azar.nextInt(300)); // a veces más de lo que se tiene o del límite
        }
        if (r < 6) {
            return BigDecimal.valueOf(100 + azar.nextInt(9900), 2); // con céntimos
        }
        return BigDecimal.valueOf(1 + azar.nextInt(60));
    }

    /** Un evento; con abiertos = true casi siempre uno que admite apuestas. */
    private Evento evento(boolean preferirAbiertos) {
        List<Evento> todos = misEventos.stream().map(id -> eventos.findById(id).orElseThrow()).toList();
        if (preferirAbiertos && azar.nextInt(10) < 8) {
            List<Evento> abiertos = todos.stream().filter(e -> e.admiteApuestas(ahora())).toList();
            if (!abiertos.isEmpty()) {
                return elegir(abiertos);
            }
        }
        return elegir(todos);
    }

    /** Casi siempre uno que cumple la condición, si lo hay. */
    private Evento eventoQue(java.util.function.Predicate<Evento> condicion) {
        List<Evento> todos = misEventos.stream().map(id -> eventos.findById(id).orElseThrow()).toList();
        List<Evento> cumplen = todos.stream().filter(condicion).toList();
        return azar.nextInt(10) < 8 && !cumplen.isEmpty() ? elegir(cumplen) : elegir(todos);
    }

    /** Para resolver: sobre todo eventos ya empezados que no están anulados. */
    private Evento eventoParaResolver() {
        List<Evento> todos = misEventos.stream().map(id -> eventos.findById(id).orElseThrow()).toList();
        int r = azar.nextInt(10);
        if (r < 7) {
            List<Evento> empezados = todos.stream()
                    .filter(e -> e.haEmpezado(ahora()) && e.getEstado() != EstadoEvento.ANULADO).toList();
            if (!empezados.isEmpty()) {
                return elegir(empezados);
            }
        }
        return elegir(todos);
    }

    private Resultado resultadoPara(Evento e) {
        if (e.getDeporte().isAdmiteEmpate() || azar.nextInt(10) == 0) {
            return Resultado.values()[azar.nextInt(3)];
        }
        return azar.nextBoolean() ? Resultado.LOCAL : Resultado.VISITANTE;
    }

    private String apostar(int selecciones, boolean especiales) {
        String email = jugador();
        List<SeleccionPedida> pedidas = new ArrayList<>();
        Set<Long> usados = new HashSet<>();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < selecciones; i++) {
            Evento e = evento(true);
            if (!usados.add(e.getId()) && azar.nextInt(4) != 0) {
                continue; // casi siempre sin repetir evento
            }
            if (especiales && azar.nextBoolean()) {
                Especial esp = Especial.values()[azar.nextInt(Especial.values().length)];
                pedidas.add(new SeleccionPedida(e.getId(), null, esp, null));
                sb.append(' ').append(e.getId()).append(':').append(esp);
            } else {
                Resultado r = resultadoPara(e);
                pedidas.add(new SeleccionPedida(e.getId(), r, null));
                sb.append(' ').append(e.getId()).append(':').append(r);
            }
        }
        BigDecimal importe = importe();
        d = "apostar " + email + " " + importe + " a" + sb;
        Apuesta a = apuestaService.apostar(email, pedidas, importe);
        return d + " → apuesta " + a.getId() + " cuota " + a.getCuota();
    }

    private String apostarMercado() {
        Mercado m = mercadoRepository.findById(elegir(misMercados)).orElseThrow();
        Candidato c = elegir(m.getCandidatos());
        String email = jugador();
        BigDecimal importe = importe();
        d = "apostar mercado " + email + " " + importe + " a " + m.getId() + "/" + c.getId();
        Apuesta a = apuestaService.apostarMercado(email, m.getId(), c.getId(), null, importe);
        return d + " → apuesta " + a.getId();
    }

    /** Una apuesta activa de un usuario activo (o, a veces, cualquiera suya). */
    private Apuesta apuestaDe(Long usuarioId) {
        Usuario u = usuarios.findById(usuarioId).orElseThrow();
        List<Apuesta> suyas = apuestas.findAll().stream().filter(a -> a.getUsuario().getId().equals(u.getId())).toList();
        List<Apuesta> activas = suyas.stream().filter(a -> a.getEstado() == EstadoApuesta.ACTIVA).toList();
        return azar.nextInt(10) < 8 && !activas.isEmpty() ? elegir(activas) : elegir(suyas);
    }

    private String cancelar() {
        Long id = elegir(activos());
        Apuesta a = apuestaDe(id);
        d = "cancelar apuesta " + a.getId() + " de " + emailDe.get(id);
        apuestaService.cancelar(emailDe.get(id), a.getId());
        return d;
    }

    /** Cobro anticipado (HU-75) de una apuesta activa con oferta, a veces con una oferta que ya no vale. */
    private String cobrarAntes() {
        Long id = elegir(activos());
        Usuario u = usuarios.findById(id).orElseThrow();
        List<Apuesta> activas = apuestaService.activas(emailDe.get(id));
        Map<Long, BigDecimal> ofertas = apuestaService.ofertasCashOut(activas, ahora());
        if (ofertas.isEmpty()) {
            throw new IllegalStateException("[prueba] " + u.getEmail() + " no tiene ofertas de cobro anticipado");
        }
        Long apuesta = elegir(new ArrayList<>(ofertas.keySet()));
        BigDecimal oferta = azar.nextInt(10) == 0 ? ofertas.get(apuesta).add(CENTIMO) : ofertas.get(apuesta);
        d = "cobrar antes apuesta " + apuesta + " de " + emailDe.get(id) + " por " + oferta;
        apuestaService.cobrarAnticipado(emailDe.get(id), apuesta, oferta);
        return d;
    }

    /** Bloqueo de liga (HU-105, HU-106): a un compañero o a toda la liga, en un partido. */
    private String bloquear() {
        List<Long> ligas = misLigas.stream().filter(ligaRepository::existsById).toList();
        Liga liga = ligaRepository.findById(elegir(ligas)).orElseThrow();
        List<Long> miembros = liga.getIdsMiembros().stream().filter(misUsuarios::contains)
                .filter(m -> !eliminados.contains(m)).toList();
        Long quien = azar.nextInt(10) == 0 ? elegir(activos()) : elegir(miembros);
        Evento e = evento(true);
        String alcance = elegir(List.of("PARTIDO", "LOCAL", "VISITANTE", "EMPATE"));
        if (azar.nextInt(3) == 0) {
            d = "bloquear a toda la liga " + liga.getId() + " por " + emailDe.get(quien) + " en " + e.getId() + " " + alcance;
            bloqueos.crearParaLiga(liga.getId(), emailDe.get(quien), e.getId(), alcance);
        } else {
            List<Long> otros = miembros.stream().filter(m -> !m.equals(quien)).toList();
            Long objetivo = otros.isEmpty() ? quien : elegir(otros);
            d = "bloquear a " + emailDe.get(objetivo) + " en la liga " + liga.getId() + " por " + emailDe.get(quien)
                    + " en " + e.getId() + " " + alcance;
            bloqueos.crear(liga.getId(), emailDe.get(quien), objetivo, e.getId(), alcance);
        }
        return d;
    }

    private String cambiarImporte() {
        Long id = elegir(activos());
        Apuesta a = apuestaDe(id);
        BigDecimal nuevo = importe();
        d = "cambiar importe apuesta " + a.getId() + " de " + emailDe.get(id) + " " + a.getImporte() + " → " + nuevo;
        Apuesta cambiada = apuestaService.modificarImporte(emailDe.get(id), a.getId(), nuevo);
        return d + " (cuota " + cambiada.getCuota() + ")";
    }

    private String resultado() {
        Evento e = eventoParaResolver();
        Resultado r = resultadoPara(e);
        d = "resultado evento " + e.getId() + " (" + e.getEstado() + ") = " + r;
        return d + " (" + resolucion.introducirResultado(e.getId(), r) + " sel.)";
    }

    private String marcador() {
        Evento e = eventoParaResolver();
        int l = azar.nextInt(4);
        int v = azar.nextInt(4);
        d = "marcador evento " + e.getId() + " (" + e.getEstado() + ") = " + l + "-" + v;
        return d + " (" + resolucion.introducirMarcador(e.getId(), l, v) + " sel.)";
    }

    private String conEvento(java.util.function.Predicate<Evento> preferido,
                             java.util.function.Function<Long, String> accion) {
        Evento e = preferido == null ? eventoParaResolver() : eventoQue(preferido);
        d = "evento " + e.getId() + " (" + e.getEstado() + ")";
        return accion.apply(e.getId());
    }

    private String cambiarFecha() {
        Evento e = eventoQue(ev -> ev.editable(ahora()));
        EdicionEventoForm form = new EdicionEventoForm();
        form.setLocalId(e.getLocal().getId());
        form.setVisitanteId(e.getVisitante().getId());
        form.setFechaHora(ahora().plusMinutes(30 + azar.nextInt(60 * 48)));
        d = "cambiar fecha evento " + e.getId() + " (" + e.getEstado() + ") → " + form.getFechaHora();
        gestion.editarEvento(e.getId(), form);
        return d;
    }

    private String conMercado(java.util.function.Function<Long, String> accion) {
        Long id = elegir(misMercados);
        d = "mercado " + id;
        return accion.apply(id);
    }

    private String supercuota() {
        Evento e = evento(true);
        Resultado r = resultadoPara(e);
        BigDecimal normal = calculadora.calcular(e).de(r);
        BigDecimal cuota = normal == null ? BigDecimal.TEN
                : normal.multiply(new BigDecimal("1.2")).setScale(2, RoundingMode.DOWN);
        d = "supercuota evento " + e.getId() + " " + r + " " + cuota;
        supercuotas.crear(e.getId(), r, cuota, new BigDecimal("50"), 1 + azar.nextInt(48));
        return d;
    }

    private String crearLiga() {
        String email = jugador();
        Liga l = ligaService.crear(email, "Liga " + misLigas.size());
        misLigas.add(l.getId());
        return "crear liga " + l.getId() + " por " + email;
    }

    private Liga liga() {
        List<Liga> existentes = misLigas.stream().map(id -> ligaRepository.findById(id).orElse(null))
                .filter(Objects::nonNull).toList();
        return elegir(existentes);
    }

    private String unirse() {
        Liga l = liga();
        String email = jugador();
        d = "unirse " + email + " a liga " + l.getId();
        ligaService.unirse(email, l.getCodigo());
        return d;
    }

    private String salir() {
        Liga l = liga();
        List<Long> miembros = l.getIdsMiembros().stream().filter(misUsuarios::contains)
                .filter(id -> !eliminados.contains(id)).toList();
        String email = emailDe.get(elegir(miembros));
        d = "salir " + email + " de liga " + l.getId();
        ligaService.salir(l.getId(), email);
        return d;
    }

    private String borrarLiga() {
        Liga l = liga();
        String email = azar.nextInt(5) == 0 ? jugador() : emailDe.get(l.getCreador().getId());
        d = "borrar liga " + l.getId() + " por " + email;
        ligaService.borrar(l.getId(), email);
        return d;
    }

    private String retar() {
        List<Long> conLiga = activos().stream()
                .filter(id -> !ligaRepository.deMiembro(usuarios.findById(id).orElseThrow()).isEmpty()).toList();
        Long retador = azar.nextInt(10) < 9 && !conLiga.isEmpty() ? elegir(conLiga) : elegir(activos());
        Usuario u = usuarios.findById(retador).orElseThrow();
        List<Long> companeros = new ArrayList<>(ligaRepository.deMiembro(u).stream()
                .flatMap(l -> l.getIdsMiembros().stream()).filter(id -> !id.equals(retador))
                .filter(misUsuarios::contains).filter(id -> !eliminados.contains(id)).distinct().toList());
        Long retado = companeros.isEmpty() || azar.nextInt(8) == 0 ? elegir(misUsuarios) : elegir(companeros);
        Evento e = evento(true);
        Resultado r = resultadoPara(e);
        BigDecimal importe = importe();
        d = "retar " + emailDe.get(retador) + " → " + emailDe.get(retado) + " evento " + e.getId() + " " + r + " " + importe;
        Reto reto = retoService.retar(emailDe.get(retador), retado, e.getId(), r, importe);
        d = d + " → reto " + reto.getId();
        // Muchas veces el retado contesta enseguida, para que haya retos aceptados que resolver y corregir
        if (azar.nextInt(10) < 6) {
            Resultado otro = resultadoPara(e);
            if (otro != r) {
                retoService.aceptar(emailDe.get(retado), reto.getId(), otro);
                d = d + ", aceptado con " + otro;
            }
        }
        return d;
    }

    private List<Reto> misRetos() {
        return retoRepository.findAll().stream().filter(r -> misUsuarios.contains(r.getRetador().getId())).toList();
    }

    private String aceptar() {
        List<Reto> pendientes = misRetos().stream().filter(r -> r.getEstado() == EstadoReto.PENDIENTE).toList();
        Reto reto = azar.nextInt(10) < 9 && !pendientes.isEmpty() ? elegir(pendientes) : elegir(misRetos());
        String email = eliminados.contains(reto.getRetado().getId()) || azar.nextInt(10) == 0
                ? jugador() : emailDe.get(reto.getRetado().getId());
        Resultado r = azar.nextInt(6) == 0 ? reto.getPronosticoRetador() : resultadoPara(reto.getEvento());
        d = "aceptar reto " + reto.getId() + " (" + reto.getEstado() + ") por " + email + " con " + r;
        retoService.aceptar(email, reto.getId(), r);
        return d;
    }

    private String rechazar() {
        List<Reto> pendientes = misRetos().stream().filter(r -> r.getEstado() == EstadoReto.PENDIENTE).toList();
        Reto reto = elegir(pendientes);
        Usuario quien = azar.nextBoolean() ? reto.getRetador() : reto.getRetado();
        String email = eliminados.contains(quien.getId()) ? jugador() : emailDe.get(quien.getId());
        d = "rechazar reto " + reto.getId() + " por " + email;
        retoService.rechazar(email, reto.getId());
        return d;
    }

    private String crearQuiniela() {
        List<Long> candidatos = new ArrayList<>(misEventos.stream().map(id -> eventos.findById(id).orElseThrow())
                .filter(e -> e.getDeporte().isAdmiteEmpate() && e.admiteApuestas(ahora())).map(Evento::getId).toList());
        java.util.Collections.shuffle(candidatos, azar);
        List<Long> elegidos = candidatos.subList(0, Math.min(candidatos.size(), 3 + azar.nextInt(3)));
        BigDecimal precio = List.of(BigDecimal.ONE, new BigDecimal("2.5"), BigDecimal.TEN, new BigDecimal("3.33"))
                .get(azar.nextInt(4));
        d = "crear quiniela con " + elegidos + " precio " + precio;
        Quiniela q = quinielaService.crear("Quiniela " + misQuinielas.size(), precio, elegidos);
        misQuinielas.add(q.getId());
        return d + " → " + q.getId();
    }

    private String echarBoleto() {
        List<Long> abiertas = misQuinielas.stream()
                .filter(q -> quinielaRepository.findById(q).orElseThrow().admiteBoletos(ahora())).toList();
        Long id = azar.nextInt(10) < 9 && !abiertas.isEmpty() ? elegir(abiertas) : elegir(misQuinielas);
        Quiniela q = quinielaRepository.findById(id).orElseThrow();
        String email = jugador();
        List<Resultado> pronosticos = new ArrayList<>();
        for (int i = 0; i < q.getPartidos().size(); i++) {
            pronosticos.add(Resultado.values()[azar.nextInt(3)]);
        }
        d = "boleto " + email + " quiniela " + id + " " + pronosticos;
        quinielaService.echarBoleto(email, id, pronosticos);
        return d;
    }

    private String conQuiniela(java.util.function.Predicate<Quiniela> preferida,
                               java.util.function.Function<Long, String> accion) {
        List<Long> cumplen = misQuinielas.stream()
                .filter(q -> preferida.test(quinielaRepository.findById(q).orElseThrow())).toList();
        Long id = azar.nextInt(10) < 8 && !cumplen.isEmpty() ? elegir(cumplen) : elegir(misQuinielas);
        d = "quiniela " + id;
        return accion.apply(id);
    }

    private String eliminar() {
        if (activos().size() <= 5 || azar.nextInt(3) != 0) {
            return "recarga en lugar de eliminar: " + usuarioService.consultarConRecarga(jugador()).getId();
        }
        Long id = elegir(activos());
        String email = emailDe.get(id);
        d = "eliminar cuenta " + email + " (id " + id + ")";
        bajas.eliminarCuenta(email, PASSWORD);
        eliminados.add(id);
        return d;
    }

    // ------------------------------------------------------------------ foto del estado e invariantes

    /** Lo que hace falta del estado para comparar antes y después de una operación. */
    record Foto(BigDecimal total, BigDecimal fuentes, BigDecimal casaApuestas, BigDecimal casaQuinielas,
                Map<Long, BigDecimal> cobroRetoEliminados, Map<Long, BigDecimal> premioBoletoEliminados,
                BigDecimal enJuego, BigDecimal apostadoCerradas, BigDecimal pagadoCerradas, BigDecimal beneficioEntidades,
                BigDecimal beneficioDiario, Set<Long> eliminados) {
    }

    private List<Apuesta> misApuestas() {
        return apuestas.findAll().stream().filter(a -> misUsuarios.contains(a.getUsuario().getId())).toList();
    }

    private List<BoletoQuiniela> misBoletos() {
        return boletoRepository.findAll().stream().filter(b -> misQuinielas.contains(b.getQuiniela().getId())).toList();
    }

    private Map<Long, List<MovimientoSaldo>> misMovimientos() {
        Map<Long, List<MovimientoSaldo>> porUsuario = new LinkedHashMap<>();
        misUsuarios.forEach(id -> porUsuario.put(id, new ArrayList<>()));
        movimientos.findAll().stream().filter(m -> porUsuario.containsKey(m.getUsuario().getId()))
                .sorted((a, b) -> Long.compare(a.getId(), b.getId()))
                .forEach(m -> porUsuario.get(m.getUsuario().getId()).add(m));
        return porUsuario;
    }

    private static BigDecimal suma(java.util.stream.Stream<BigDecimal> valores) {
        return valores.reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private static boolean cerrada(Apuesta a) {
        return a.getEstado() == EstadoApuesta.GANADA || a.getEstado() == EstadoApuesta.PERDIDA
                || a.getEstado() == EstadoApuesta.ANULADA || a.getEstado() == EstadoApuesta.COBRADA;
    }

    /** Lo que un jugador ha cobrado en total de un reto según su estado (sin contar lo que puso). */
    private static BigDecimal cobroReto(Reto r, Long jugador) {
        boolean esRetador = r.getRetador().getId().equals(jugador);
        return switch (r.getEstado()) {
            case PENDIENTE, ACEPTADO -> BigDecimal.ZERO;
            case RECHAZADO -> esRetador ? r.getImporte() : BigDecimal.ZERO;
            case DEVUELTO -> esRetador || r.getPronosticoRetado() != null ? r.getImporte() : BigDecimal.ZERO;
            case RESUELTO -> r.getGanador() != null && r.getGanador().getId().equals(jugador) ? r.getBote()
                    : BigDecimal.ZERO;
        };
    }

    /** Lo que un jugador ha puesto en un reto. */
    private static BigDecimal aportadoReto(Reto r, Long jugador) {
        boolean esRetador = r.getRetador().getId().equals(jugador);
        return esRetador || r.getPronosticoRetado() != null ? r.getImporte() : BigDecimal.ZERO;
    }

    private static BigDecimal retenidoReto(Reto r) {
        return switch (r.getEstado()) {
            case PENDIENTE -> r.getImporte();
            case ACEPTADO -> r.getBote();
            default -> BigDecimal.ZERO;
        };
    }

    private Foto foto() {
        List<Usuario> todos = misUsuarios.stream().map(id -> usuarios.findById(id).orElseThrow()).toList();
        List<Apuesta> lasApuestas = misApuestas();
        List<Reto> retos = misRetos();
        List<BoletoQuiniela> boletos = misBoletos();
        Map<Long, List<MovimientoSaldo>> movs = misMovimientos();

        BigDecimal saldos = suma(todos.stream().map(Usuario::getSaldo));
        BigDecimal enJuego = suma(lasApuestas.stream().filter(a -> a.getEstado() == EstadoApuesta.ACTIVA)
                .map(Apuesta::getImporte));
        BigDecimal retenidoRetos = suma(retos.stream().map(InvariantesDineroTest::retenidoReto));
        BigDecimal enQuinielas = suma(boletos.stream().filter(b -> b.getQuiniela().getEstado() == EstadoQuiniela.ABIERTA)
                .map(BoletoQuiniela::getPagado));
        BigDecimal total = saldos.add(enJuego).add(retenidoRetos).add(enQuinielas);

        BigDecimal fuentes = suma(movs.values().stream().flatMap(List::stream)
                .filter(m -> m.getTipo() == TipoMovimiento.BIENVENIDA || m.getTipo() == TipoMovimiento.RECARGA
                        || m.getTipo() == TipoMovimiento.BAJA)
                .map(MovimientoSaldo::getImporte));
        List<Apuesta> cerradas = lasApuestas.stream().filter(InvariantesDineroTest::cerrada).toList();
        BigDecimal apostadoCerradas = suma(cerradas.stream().map(Apuesta::getImporte));
        BigDecimal pagadoCerradas = suma(cerradas.stream().map(Apuesta::getPagado));
        BigDecimal casaQuinielas = suma(boletos.stream().filter(b -> b.getQuiniela().getEstado() != EstadoQuiniela.ABIERTA)
                .map(b -> b.getPagado().subtract(b.getPremio())));

        Map<Long, BigDecimal> cobroRetoEliminados = new HashMap<>();
        for (Reto r : retos) {
            for (Usuario jugador : List.of(r.getRetador(), r.getRetado())) {
                if (eliminados.contains(jugador.getId())) {
                    cobroRetoEliminados.merge(r.getId(), cobroReto(r, jugador.getId()), BigDecimal::add);
                }
            }
        }
        Map<Long, BigDecimal> premioBoletoEliminados = new HashMap<>();
        for (BoletoQuiniela b : boletos) {
            if (eliminados.contains(b.getUsuario().getId())) {
                premioBoletoEliminados.put(b.getId(), b.getPremio());
            }
        }
        // C2: lo que gana la casa según las entidades. Apuestas: importe − pagado de las no canceladas (las activas
        // también: su importe ya lo tiene la casa). Quinielas: del libro, porque el premio de un boleto de una cuenta
        // eliminada no se llega a abonar
        BigDecimal casaApuestasTodas = suma(lasApuestas.stream().filter(a -> a.getEstado() != EstadoApuesta.CANCELADA)
                .map(a -> a.getImporte().subtract(a.getPagado())));
        BigDecimal casaBloqueos = suma(movs.values().stream().flatMap(List::stream)
                .filter(InvariantesDineroTest::esDeBloqueo).map(m -> m.getImporte().negate()));
        BigDecimal quinielasLibro = suma(movs.values().stream().flatMap(List::stream)
                .filter(m -> m.getTipo() == TipoMovimiento.QUINIELA_BOLETO || m.getTipo() == TipoMovimiento.QUINIELA_PREMIO
                        || (m.getTipo() == TipoMovimiento.DEVOLUCION && m.getConcepto().startsWith("Devolución de la quiniela")))
                .map(m -> m.getImporte().negate()));
        return new Foto(total, fuentes, suma(cerradas.stream().map(a -> a.getImporte().subtract(a.getPagado()))),
                casaQuinielas.add(casaBloqueos), cobroRetoEliminados, premioBoletoEliminados, enJuego, apostadoCerradas,
                pagadoCerradas, casaApuestasTodas.add(quinielasLibro).add(casaBloqueos),
                beneficioDiario.ultimosDias().total(), Set.copyOf(eliminados));
    }

    /** Devuelve la primera invariante que no se cumple, o null. */
    private String comprobar(Foto f, Foto antes, String operacion) {
        List<String> fallos = new ArrayList<>();
        comprobarUsuarios(fallos);
        comprobarApuestas(fallos);
        comprobarRetos(fallos);
        comprobarQuinielas(fallos);
        comprobarCasa(f, fallos);
        if (antes != null) {
            comprobarConservacion(f, antes, fallos);
        }
        return fallos.isEmpty() ? null : "tras «" + operacion + "»:\n  " + String.join("\n  ", fallos);
    }

    private void comprobarUsuarios(List<String> fallos) {
        Map<Long, List<MovimientoSaldo>> movs = misMovimientos();
        List<Apuesta> lasApuestas = misApuestas();
        List<Reto> retos = misRetos();
        List<BoletoQuiniela> boletos = misBoletos();
        for (Long id : misUsuarios) {
            Usuario u = usuarios.findById(id).orElseThrow();
            List<MovimientoSaldo> suyos = movs.get(id);
            String quien = "usuario " + id + (u.isEliminado() ? " (eliminado)" : "");
            // U1
            BigDecimal acumulado = BigDecimal.ZERO;
            for (int i = 0; i < suyos.size(); i++) {
                MovimientoSaldo m = suyos.get(i);
                if (i == 0 && (m.getTipo() != TipoMovimiento.BIENVENIDA || m.getImporte().compareTo(BIENVENIDA) != 0)) {
                    fallos.add("U1 " + quien + ": el primer movimiento no es la bienvenida de 1000: " + m.getTipo());
                }
                if (m.getImporte().signum() == 0) {
                    fallos.add("U1 " + quien + ": movimiento de importe 0 (" + m.getConcepto() + ")");
                }
                acumulado = acumulado.add(m.getImporte());
                if (acumulado.compareTo(m.getSaldoResultante()) != 0) {
                    fallos.add("U1 " + quien + ": saldoResultante " + m.getSaldoResultante() + " ≠ acumulado " + acumulado
                            + " en «" + m.getConcepto() + "»");
                }
                if ((m.getTipo() == TipoMovimiento.APUESTA || m.getTipo() == TipoMovimiento.RETO_APUESTA
                        || m.getTipo() == TipoMovimiento.QUINIELA_BOLETO) && m.getSaldoResultante().signum() < 0) {
                    fallos.add("U1 " + quien + ": saldo negativo tras un cobro: " + m.getConcepto());
                }
            }
            if (acumulado.compareTo(u.getSaldo()) != 0) {
                fallos.add("U1 " + quien + ": saldo " + u.getSaldo() + " ≠ suma de movimientos " + acumulado);
            }
            // U2
            BigDecimal libroApuestas = suma(suyos.stream().filter(m -> esDeApuesta(m)).map(MovimientoSaldo::getImporte));
            BigDecimal esperadoApuestas = suma(lasApuestas.stream().filter(a -> a.getUsuario().getId().equals(id))
                    .filter(a -> a.getEstado() != EstadoApuesta.CANCELADA)
                    .map(a -> a.getPagado().subtract(a.getImporte())));
            if (libroApuestas.compareTo(esperadoApuestas) != 0) {
                fallos.add("U2 " + quien + ": movimientos de apuestas " + libroApuestas + " ≠ Σ(pagado − importe) "
                        + esperadoApuestas);
            }
            if (!u.isEliminado()) {
                // U3
                BigDecimal libroRetos = suma(suyos.stream().filter(m -> esDeReto(m)).map(MovimientoSaldo::getImporte));
                BigDecimal esperadoRetos = suma(retos.stream().filter(r -> r.esDe(u))
                        .map(r -> cobroReto(r, id).subtract(aportadoReto(r, id))));
                if (libroRetos.compareTo(esperadoRetos) != 0) {
                    fallos.add("U3 " + quien + ": movimientos de retos " + libroRetos + " ≠ lo que dicen sus retos "
                            + esperadoRetos);
                }
                // U4
                BigDecimal libroQuinielas = suma(suyos.stream().filter(m -> esDeQuiniela(m)).map(MovimientoSaldo::getImporte));
                BigDecimal esperadoQuinielas = suma(boletos.stream().filter(b -> b.getUsuario().getId().equals(id))
                        .map(b -> b.getPremio().subtract(b.getPagado())));
                if (libroQuinielas.compareTo(esperadoQuinielas) != 0) {
                    fallos.add("U4 " + quien + ": movimientos de quinielas " + libroQuinielas + " ≠ Σ(premio − pagado) "
                            + esperadoQuinielas);
                }
            } else if (u.getSaldo().signum() != 0) {
                fallos.add("U5 " + quien + ": una cuenta eliminada tiene saldo " + u.getSaldo());
            }
            // B1
            if (!u.isEliminado()) {
                BigDecimal libroBloqueos = suma(suyos.stream().filter(InvariantesDineroTest::esDeBloqueo)
                        .map(MovimientoSaldo::getImporte));
                BigDecimal enPie = suma(bloqueoRepository.findAll().stream()
                        .filter(b -> b.getCreador().getId().equals(u.getId())).map(InvariantesDineroTest::coste))
                        .add(bloqueosUsados.getOrDefault(u.getId(), BigDecimal.ZERO));
                if (libroBloqueos.compareTo(enPie.negate()) != 0) {
                    fallos.add("B1 " + quien + ": movimientos de bloqueos " + libroBloqueos + " ≠ − coste de sus bloqueos "
                            + enPie);
                }
            }
            // U5
            LocalDateTime anterior = null;
            for (MovimientoSaldo m : suyos) {
                if (m.getTipo() == TipoMovimiento.RECARGA) {
                    if (m.getImporte().compareTo(RECARGA) != 0) {
                        fallos.add("U5 " + quien + ": recarga de " + m.getImporte());
                    }
                    if (anterior != null && anterior.plusDays(7).isAfter(m.getFecha())) {
                        fallos.add("U5 " + quien + ": dos recargas en menos de 7 días");
                    }
                    anterior = m.getFecha();
                } else if (m.getTipo() == TipoMovimiento.BIENVENIDA) {
                    anterior = m.getFecha();
                }
                if (!esDeApuesta(m) && !esDeReto(m) && !esDeQuiniela(m) && !esDeBloqueo(m)
                        && m.getTipo() != TipoMovimiento.BIENVENIDA
                        && m.getTipo() != TipoMovimiento.RECARGA && m.getTipo() != TipoMovimiento.BAJA) {
                    fallos.add("U1 " + quien + ": movimiento inesperado " + m.getTipo() + " «" + m.getConcepto() + "»");
                }
            }
        }
    }

    private static boolean esDeQuiniela(MovimientoSaldo m) {
        return m.getTipo() == TipoMovimiento.QUINIELA_BOLETO || m.getTipo() == TipoMovimiento.QUINIELA_PREMIO
                || (m.getTipo() == TipoMovimiento.DEVOLUCION && m.getConcepto().startsWith("Devolución de la quiniela"));
    }

    private static boolean esDeReto(MovimientoSaldo m) {
        return m.getTipo() == TipoMovimiento.RETO_APUESTA || m.getTipo() == TipoMovimiento.RETO_PREMIO
                || m.getTipo() == TipoMovimiento.RETO_DEVOLUCION
                || (m.getTipo() == TipoMovimiento.CORRECCION && m.getConcepto().startsWith("Reto corregido"));
    }

    private static boolean esDeBloqueo(MovimientoSaldo m) {
        return m.getTipo() == TipoMovimiento.BLOQUEO_LIGA
                || (m.getTipo() == TipoMovimiento.DEVOLUCION && m.getConcepto().startsWith("Devolución del bloqueo"));
    }

    private static boolean esDeApuesta(MovimientoSaldo m) {
        return (m.getTipo() == TipoMovimiento.APUESTA || m.getTipo() == TipoMovimiento.PREMIO
                || m.getTipo() == TipoMovimiento.DEVOLUCION || m.getTipo() == TipoMovimiento.CORRECCION
                || m.getTipo() == TipoMovimiento.COBRO_ANTICIPADO)
                && !esDeQuiniela(m) && !esDeReto(m) && !esDeBloqueo(m);
    }

    private static BigDecimal coste(BloqueoLiga b) {
        return b.getObjetivo() == null ? BloqueoLigaService.COSTE_LIGA : BloqueoLigaService.COSTE;
    }

    private void comprobarApuestas(List<String> fallos) {
        // B1: un partido anulado no conserva bloqueos (se devuelven y se borran)
        for (BloqueoLiga b : bloqueoRepository.findAll()) {
            if (b.getEvento().getEstado() == EstadoEvento.ANULADO) {
                fallos.add("B1 bloqueo " + b.getId() + " en el partido anulado " + b.getEvento().getId());
            }
        }
        for (Apuesta a : misApuestas()) {
            String cual = "apuesta " + a.getId() + " (" + a.getEstado() + ", usuario " + a.getUsuario().getId() + ")";
            // A1
            if (a.getEstado() == EstadoApuesta.ACTIVA && a.getPagado().signum() != 0) {
                fallos.add("A1 " + cual + ": activa con pagado " + a.getPagado());
            }
            if (!a.getUsuario().isEliminado()) {
                BigDecimal debe = switch (a.getEstado()) {
                    case GANADA -> a.getGananciaPotencial();
                    case ANULADA -> a.getImporte();
                    case COBRADA -> a.getPagado().signum() >= 0 && a.getPagado().compareTo(a.getGananciaPotencial()) <= 0
                            ? a.getPagado() : BigDecimal.valueOf(-1);
                    default -> BigDecimal.ZERO;
                };
                if (debe.compareTo(a.getPagado()) != 0) {
                    fallos.add("A1 " + cual + ": pagado " + a.getPagado() + " y según su estado debería ser " + debe);
                }
            }
            if (a.getEstado() == EstadoApuesta.CANCELADA || a.getEstado() == EstadoApuesta.COBRADA) {
                continue; // sus selecciones se quedan como estaban al cancelarla o cobrarla antes (HU-75)
            }
            // A2: cada selección según su evento o mercado
            boolean fallada = false;
            boolean pendiente = false;
            boolean todasAnuladas = true;
            for (Seleccion s : a.getSelecciones()) {
                EstadoSeleccion esperado = esperado(s);
                if (esperado != s.getEstado()) {
                    fallos.add("A2 " + cual + ": selección " + s.getId() + " está " + s.getEstado() + " y debería estar "
                            + esperado + " (" + describir(s) + ")");
                }
                fallada |= s.getEstado() == EstadoSeleccion.FALLADA;
                pendiente |= s.getEstado() == EstadoSeleccion.PENDIENTE;
                todasAnuladas &= s.getEstado() == EstadoSeleccion.ANULADA;
            }
            EstadoApuesta debe = fallada ? EstadoApuesta.PERDIDA : pendiente ? EstadoApuesta.ACTIVA
                    : todasAnuladas ? EstadoApuesta.ANULADA : EstadoApuesta.GANADA;
            if (debe != a.getEstado()) {
                fallos.add("A2 " + cual + ": según sus selecciones debería estar " + debe);
            }
        }
    }

    private static String describir(Seleccion s) {
        if (s.isLargoPlazo()) {
            Mercado m = s.getCandidato().getMercado();
            return "mercado " + m.getId() + " " + m.getEstado();
        }
        Evento e = s.getEvento();
        return "evento " + e.getId() + " " + e.getEstado() + " " + e.getResultado() + " " + e.getGolesLocal() + "-"
                + e.getGolesVisitante() + ", pronóstico " + (s.getEspecial() != null ? s.getEspecial() : s.getPronostico());
    }

    private static EstadoSeleccion esperado(Seleccion s) {
        if (s.isLargoPlazo()) {
            Mercado m = s.getCandidato().getMercado();
            if (m.getEstado() == EstadoMercado.ANULADO) {
                return EstadoSeleccion.ANULADA;
            }
            if (m.getEstado() == EstadoMercado.RESUELTO) {
                return m.getGanador().getId().equals(s.getCandidato().getId()) ? EstadoSeleccion.ACERTADA
                        : EstadoSeleccion.FALLADA;
            }
            return EstadoSeleccion.PENDIENTE;
        }
        Evento e = s.getEvento();
        if (e.getEstado() == EstadoEvento.ANULADO) {
            return EstadoSeleccion.ANULADA;
        }
        if (e.getEstado() != EstadoEvento.FINALIZADO) {
            return EstadoSeleccion.PENDIENTE;
        }
        Boolean acierto = s.getEspecial() != null
                ? s.getEspecial().acierta(e.getResultado(), e.getGolesLocal(), e.getGolesVisitante())
                : Boolean.valueOf(s.getPronostico() == e.getResultado());
        return acierto == null ? EstadoSeleccion.ANULADA : acierto ? EstadoSeleccion.ACERTADA : EstadoSeleccion.FALLADA;
    }

    private void comprobarRetos(List<String> fallos) {
        for (Reto r : misRetos()) {
            Evento e = r.getEvento();
            String cual = "reto " + r.getId() + " (" + r.getEstado() + ", evento " + e.getId() + " " + e.getEstado() + " "
                    + e.getResultado() + ", " + r.getPronosticoRetador() + " vs " + r.getPronosticoRetado() + ")";
            boolean abierto = r.getEstado() == EstadoReto.PENDIENTE || r.getEstado() == EstadoReto.ACEPTADO;
            if (abierto && e.getEstado() != EstadoEvento.PROGRAMADO && e.getEstado() != EstadoEvento.SUSPENDIDO) {
                fallos.add("R1 " + cual + ": sigue abierto con el partido " + e.getEstado());
            }
            if (r.getEstado() == EstadoReto.PENDIENTE) {
                boolean comparten = ligaRepository.deMiembro(r.getRetador()).stream().anyMatch(l -> l.esMiembro(r.getRetado()));
                if (!comparten) {
                    fallos.add("R1 " + cual + ": pendiente entre dos que ya no comparten liga");
                }
                if (r.getPronosticoRetado() != null) {
                    fallos.add("R1 " + cual + ": pendiente con pronóstico del retado");
                }
            }
            if (r.getEstado() == EstadoReto.ACEPTADO
                    && (r.getPronosticoRetado() == null || r.getPronosticoRetado() == r.getPronosticoRetador())) {
                fallos.add("R1 " + cual + ": aceptado sin pronóstico distinto");
            }
            // Con una cuenta eliminada, un reto ya cerrado se queda como estaba aunque se corrija el resultado
            // (RetoService.corregir): no se comprueba que cuadre con el resultado actual
            boolean conCuentaEliminada = r.getRetador().isEliminado() || r.getRetado().isEliminado();
            if (!conCuentaEliminada && (r.getEstado() == EstadoReto.RESUELTO
                    || (r.getEstado() == EstadoReto.DEVUELTO && r.getPronosticoRetado() != null))) {
                Resultado res = e.getEstado() == EstadoEvento.FINALIZADO ? e.getResultado() : null;
                Usuario gana = res == null ? null : res == r.getPronosticoRetador() ? r.getRetador()
                        : res == r.getPronosticoRetado() ? r.getRetado() : null;
                if (r.getEstado() == EstadoReto.RESUELTO
                        && (gana == null || r.getGanador() == null || !gana.getId().equals(r.getGanador().getId()))) {
                    fallos.add("R1 " + cual + ": resuelto con ganador " + (r.getGanador() == null ? null : r.getGanador().getId())
                            + " y debería " + (gana == null ? "devolverse" : "ganarlo " + gana.getId()));
                }
                if (r.getEstado() == EstadoReto.DEVUELTO && gana != null) {
                    fallos.add("R1 " + cual + ": devuelto pero lo gana " + gana.getId());
                }
            }
        }
    }

    private void comprobarQuinielas(List<String> fallos) {
        List<BoletoQuiniela> boletos = misBoletos();
        for (Long id : misQuinielas) {
            Quiniela q = quinielaRepository.findById(id).orElseThrow();
            List<BoletoQuiniela> suyos = boletos.stream().filter(b -> b.getQuiniela().getId().equals(id)).toList();
            String cual = "quiniela " + id + " (" + q.getEstado() + ")";
            for (BoletoQuiniela b : suyos) {
                if (b.getPagado().compareTo(q.getPrecio()) != 0) {
                    fallos.add("Q1 " + cual + ": boleto " + b.getId() + " pagó " + b.getPagado());
                }
                if (q.getEstado() == EstadoQuiniela.ABIERTA && (b.getPremio().signum() != 0 || b.isDevuelto())) {
                    fallos.add("Q1 " + cual + ": boleto " + b.getId() + " con premio antes de repartir");
                }
                if (q.getEstado() == EstadoQuiniela.ANULADA && (!b.isDevuelto() || b.getPremio().compareTo(b.getPagado()) != 0)) {
                    fallos.add("Q1 " + cual + ": boleto " + b.getId() + " sin devolver");
                }
                if (b.isDevuelto() && b.getPremio().compareTo(b.getPagado()) != 0) {
                    fallos.add("Q1 " + cual + ": boleto " + b.getId() + " devuelto con " + b.getPremio());
                }
            }
            if (q.getEstado() == EstadoQuiniela.REPARTIDA && suyos.stream().noneMatch(BoletoQuiniela::isDevuelto)) {
                BigDecimal pagado = suma(suyos.stream().map(BoletoQuiniela::getPagado));
                BigDecimal repartido = suma(suyos.stream().map(BoletoQuiniela::getPremio));
                BigDecimal bote = q.bote(pagado);
                long ganadores = suyos.stream().filter(BoletoQuiniela::isGanador).count();
                if (repartido.compareTo(bote) > 0
                        || bote.subtract(repartido).compareTo(CENTIMO.multiply(BigDecimal.valueOf(ganadores))) >= 0
                        && ganadores > 0) {
                    fallos.add("Q1 " + cual + ": repartido " + repartido + " con un bote de " + bote + " y " + ganadores
                            + " ganadores");
                }
            }
        }
    }

    private void comprobarCasa(Foto f, List<String> fallos) {
        CuentasCasa casa = cuentasCasa.cuentas();
        CuentasCasa.Fila total = casa.total();
        // C1: lo que cambia el panel es lo que cambian mis apuestas (las demás no se tocan en esta transacción)
        if (total.enJuego().subtract(casaInicial.enJuego()).compareTo(f.enJuego()) != 0
                || total.apostado().subtract(casaInicial.apostado()).compareTo(f.apostadoCerradas()) != 0
                || total.pagado().subtract(casaInicial.pagado()).compareTo(f.pagadoCerradas()) != 0) {
            fallos.add("C1 cuentas de la casa: en juego/apostado/pagado " + total.enJuego() + "/" + total.apostado() + "/"
                    + total.pagado() + " no cuadran con las apuestas (" + f.enJuego() + "/" + f.apostadoCerradas() + "/"
                    + f.pagadoCerradas() + " más lo inicial)");
        }
        sumanTotal("C1 filas por tipo", casa.porTipo(), total, fallos);
        List<CuentasCasa.Fila> simples = casa.porTipo().subList(0, 2);
        BigDecimal enJuegoSimples = suma(simples.stream().map(CuentasCasa.Fila::enJuego));
        BigDecimal apostadoSimples = suma(simples.stream().map(CuentasCasa.Fila::apostado));
        BigDecimal pagadoSimples = suma(simples.stream().map(CuentasCasa.Fila::pagado));
        if (suma(casa.porDeporte().stream().map(CuentasCasa.Fila::enJuego)).compareTo(enJuegoSimples) != 0
                || suma(casa.porDeporte().stream().map(CuentasCasa.Fila::apostado)).compareTo(apostadoSimples) != 0
                || suma(casa.porDeporte().stream().map(CuentasCasa.Fila::pagado)).compareTo(pagadoSimples) != 0) {
            fallos.add("C1 las filas por deporte no suman las simples");
        }
        // C2
        BigDecimal beneficio = f.beneficioDiario().subtract(beneficioInicial);
        if (beneficio.compareTo(f.beneficioEntidades()) != 0) {
            fallos.add("C2 beneficio diario de la casa " + beneficio + " ≠ apuestas (importe − pagado) + quinielas "
                    + f.beneficioEntidades() + " (diferencia " + beneficio.subtract(f.beneficioEntidades()) + ")");
        }
    }

    private static void sumanTotal(String que, List<CuentasCasa.Fila> filas, CuentasCasa.Fila total, List<String> fallos) {
        int n = filas.stream().mapToInt(CuentasCasa.Fila::apuestas).sum();
        if (n != total.apuestas()
                || suma(filas.stream().map(CuentasCasa.Fila::enJuego)).compareTo(total.enJuego()) != 0
                || suma(filas.stream().map(CuentasCasa.Fila::apostado)).compareTo(total.apostado()) != 0
                || suma(filas.stream().map(CuentasCasa.Fila::pagado)).compareTo(total.pagado()) != 0) {
            fallos.add(que + " no suman el total");
        }
    }

    private void comprobarConservacion(Foto f, Foto antes, List<String> fallos) {
        BigDecimal desaparecido = BigDecimal.ZERO;
        // Lo que les tocaría cobrar a cuentas ya eliminadas antes de esta operación no se abona: sale del sistema
        for (Map.Entry<Long, BigDecimal> e : f.cobroRetoEliminados().entrySet()) {
            Long reto = e.getKey();
            BigDecimal ahora = cobroEliminadosAntes(reto, antes.eliminados());
            BigDecimal previo = antes.cobroRetoEliminados().getOrDefault(reto, BigDecimal.ZERO);
            desaparecido = desaparecido.add(ahora.subtract(previo));
        }
        for (Map.Entry<Long, BigDecimal> e : f.premioBoletoEliminados().entrySet()) {
            Long boleto = e.getKey();
            BoletoQuiniela b = boletoRepository.findById(boleto).orElseThrow();
            if (antes.eliminados().contains(b.getUsuario().getId())) {
                desaparecido = desaparecido.add(e.getValue()
                        .subtract(antes.premioBoletoEliminados().getOrDefault(boleto, BigDecimal.ZERO)));
            }
        }
        BigDecimal esperado = f.fuentes().subtract(antes.fuentes())
                .subtract(f.casaApuestas().subtract(antes.casaApuestas()))
                .subtract(f.casaQuinielas().subtract(antes.casaQuinielas()))
                .subtract(desaparecido);
        BigDecimal cambio = f.total().subtract(antes.total());
        if (cambio.compareTo(esperado) != 0) {
            fallos.add("G1 el dinero total ha cambiado " + cambio + " y solo se explica " + esperado + " (bienvenidas, "
                    + "recargas y bajas " + f.fuentes().subtract(antes.fuentes()) + "; casa en apuestas "
                    + f.casaApuestas().subtract(antes.casaApuestas()) + "; casa en quinielas "
                    + f.casaQuinielas().subtract(antes.casaQuinielas()) + "; no abonado a eliminados " + desaparecido
                    + "). Diferencia: " + cambio.subtract(esperado));
        }
    }

    /** Cobro según el estado de un reto, solo de las partes que ya estaban eliminadas antes de la operación. */
    private BigDecimal cobroEliminadosAntes(Long retoId, Set<Long> eliminadosAntes) {
        Reto r = retoRepository.findById(retoId).orElseThrow();
        BigDecimal cobro = BigDecimal.ZERO;
        for (Usuario jugador : List.of(r.getRetador(), r.getRetado())) {
            if (eliminadosAntes.contains(jugador.getId())) {
                cobro = cobro.add(cobroReto(r, jugador.getId()));
            }
        }
        return cobro;
    }

    private String resumenFinal() {
        List<Apuesta> lasApuestas = misApuestas();
        Map<EstadoApuesta, Long> porEstado = new EnumMap<>(EstadoApuesta.class);
        lasApuestas.forEach(a -> porEstado.merge(a.getEstado(), 1L, Long::sum));
        Map<EstadoReto, Long> retos = new EnumMap<>(EstadoReto.class);
        misRetos().forEach(r -> retos.merge(r.getEstado(), 1L, Long::sum));
        Map<EstadoQuiniela, Long> quinielas = new EnumMap<>(EstadoQuiniela.class);
        misQuinielas.forEach(id -> quinielas.merge(quinielaRepository.findById(id).orElseThrow().getEstado(), 1L, Long::sum));
        long combinadas = lasApuestas.stream().filter(Apuesta::isCombinada).count();
        long largoPlazo = lasApuestas.stream().filter(Apuesta::isLargoPlazo).count();
        long correcciones = misMovimientos().values().stream().flatMap(List::stream)
                .filter(m -> m.getTipo() == TipoMovimiento.CORRECCION).count();
        return "Estado final: apuestas " + porEstado + " (combinadas " + combinadas + ", largo plazo " + largoPlazo
                + "), retos " + retos + ", quinielas " + quinielas + ", boletos " + misBoletos().size()
                + ", usuarios " + misUsuarios.size() + " (eliminados " + eliminados.size() + "), movimientos CORRECCION "
                + correcciones + ", reloj " + Duration.between(inicio, ahora()) + "\n";
    }
}
