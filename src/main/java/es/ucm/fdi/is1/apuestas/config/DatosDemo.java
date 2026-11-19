package es.ucm.fdi.is1.apuestas.config;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import es.ucm.fdi.is1.apuestas.apuesta.ApuestaService;
import es.ucm.fdi.is1.apuestas.apuesta.ResolucionService;
import es.ucm.fdi.is1.apuestas.apuesta.SeleccionPedida;
import es.ucm.fdi.is1.apuestas.apuesta.SupercuotaService;
import es.ucm.fdi.is1.apuestas.cuotas.CalculadoraCuotas;
import es.ucm.fdi.is1.apuestas.cuotas.Resultado;
import es.ucm.fdi.is1.apuestas.equipos.Competicion;
import es.ucm.fdi.is1.apuestas.equipos.CompeticionRepository;
import es.ucm.fdi.is1.apuestas.equipos.Deporte;
import es.ucm.fdi.is1.apuestas.equipos.EquipoRepository;
import es.ucm.fdi.is1.apuestas.eventos.EstadoEvento;
import es.ucm.fdi.is1.apuestas.eventos.Evento;
import es.ucm.fdi.is1.apuestas.eventos.EventoRepository;
import es.ucm.fdi.is1.apuestas.ligas.Liga;
import es.ucm.fdi.is1.apuestas.ligas.LigaService;
import es.ucm.fdi.is1.apuestas.usuarios.Rol;
import es.ucm.fdi.is1.apuestas.usuarios.UsuarioRepository;
import es.ucm.fdi.is1.apuestas.usuarios.UsuarioService;

/**
 * Datos de demostración, para enseñar la web con algo dentro (por ejemplo, en Render el día de la defensa).
 * Solo con {@code apuestas.demo.activa=true} (variable de entorno {@code DEMO_DATOS=true}) y una sola vez: si ya
 * existen los jugadores de la demo, no hace nada.
 *
 * <p>Crea seis jugadores con apuestas ganadas, perdidas y activas, partidos próximos (si hay pocos), una combinada,
 * una supercuota y una liga privada. Todo pasa por los mismos servicios que usa la web, así que el saldo, el libro de
 * movimientos, las cuotas y el ranking cuadran como si hubieran jugado de verdad. Los jugadores no pueden entrar: su
 * contraseña es aleatoria y no se guarda en ningún sitio.
 *
 * <p>Se ejecuta con {@link ApplicationReadyEvent}, cuando ya ha terminado {@link DatosIniciales}, que crea los
 * equipos y las competiciones que usa.
 */
@Component
@ConditionalOnProperty(name = "apuestas.demo.activa", havingValue = "true")
public class DatosDemo {

    private static final Logger LOG = LoggerFactory.getLogger(DatosDemo.class);

    /** Si existe este jugador, la demo ya se cargó. */
    static final String MARCA = "lucia.demo@apuestas.es";

    private static final String[][] JUGADORES = {
            {MARCA, "Lucía"}, {"marcos.demo@apuestas.es", "Marcos"}, {"sofia.demo@apuestas.es", "Sofía"},
            {"pablo.demo@apuestas.es", "Pablo"}, {"elena.demo@apuestas.es", "Elena"}, {"hugo.demo@apuestas.es", "Hugo"}};

    /** Partidos ya jugados: local, visitante, goles de cada uno y hace cuántos días. */
    private static final Object[][] JUGADOS = {
            {"Real Madrid", "Getafe CF", 3, 1, 1}, {"FC Barcelona", "Sevilla FC", 2, 2, 1},
            {"Villarreal CF", "Real Betis", 0, 1, 2}, {"Athletic Club", "Real Sociedad", 1, 0, 3}};

    /** Partidos próximos que se crean si hay menos de {@link #MINIMO_PROXIMOS}: local, visitante y dentro de cuántos días. */
    private static final Object[][] PROXIMOS = {
            {"Atlético de Madrid", "Valencia CF", 1}, {"Real Betis", "Celta de Vigo", 2},
            {"CA Osasuna", "Rayo Vallecano", 3}, {"FC Barcelona", "Real Madrid", 4},
            {"Real Sociedad", "Villarreal CF", 6}, {"Getafe CF", "Elche CF", 8}};

    private static final int MINIMO_PROXIMOS = 6;

    private final UsuarioService usuarioService;
    private final UsuarioRepository usuarios;
    private final ApuestaService apuestas;
    private final ResolucionService resolucion;
    private final SupercuotaService supercuotas;
    private final CalculadoraCuotas calculadora;
    private final LigaService ligas;
    private final EventoRepository eventos;
    private final EquipoRepository equipos;
    private final CompeticionRepository competiciones;
    private final Clock reloj;

    public DatosDemo(UsuarioService usuarioService, UsuarioRepository usuarios, ApuestaService apuestas,
                     ResolucionService resolucion, SupercuotaService supercuotas, CalculadoraCuotas calculadora,
                     LigaService ligas, EventoRepository eventos, EquipoRepository equipos,
                     CompeticionRepository competiciones, Clock reloj) {
        this.usuarioService = usuarioService;
        this.usuarios = usuarios;
        this.apuestas = apuestas;
        this.resolucion = resolucion;
        this.supercuotas = supercuotas;
        this.calculadora = calculadora;
        this.ligas = ligas;
        this.eventos = eventos;
        this.equipos = equipos;
        this.competiciones = competiciones;
        this.reloj = reloj;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void cargar() {
        if (usuarios.findByEmail(MARCA).isPresent()) {
            LOG.info("Datos de demostración: ya estaban cargados");
            return;
        }
        for (String[] j : JUGADORES) {
            usuarioService.crear(j[0], j[1], UUID.randomUUID().toString(), Rol.USUARIO);
        }
        boolean conUsuarioDePrueba = usuarios.findByEmail("usuario@apuestas.es").isPresent();
        Competicion laLiga = competiciones.findByNombre("LaLiga").orElseThrow();

        // Partidos ya jugados: se apuesta mientras están abiertos y después se resuelven con su marcador
        Resultado[][] pronosticos = {
                {Resultado.LOCAL, Resultado.EMPATE, Resultado.VISITANTE, Resultado.LOCAL},
                {Resultado.LOCAL, Resultado.LOCAL, Resultado.VISITANTE, Resultado.VISITANTE},
                {Resultado.VISITANTE, Resultado.EMPATE, Resultado.LOCAL, Resultado.LOCAL},
                {Resultado.EMPATE, Resultado.LOCAL, Resultado.VISITANTE, Resultado.EMPATE},
                {Resultado.LOCAL, Resultado.VISITANTE, Resultado.VISITANTE, Resultado.LOCAL},
                {Resultado.VISITANTE, Resultado.EMPATE, Resultado.LOCAL, Resultado.VISITANTE}};
        for (int p = 0; p < JUGADOS.length; p++) {
            Object[] j = JUGADOS[p];
            Evento partido = partido(laLiga, (String) j[0], (String) j[1], LocalDateTime.now(reloj).plusDays(1));
            for (int i = 0; i < JUGADORES.length; i++) {
                apuestas.apostar(JUGADORES[i][0], partido.getId(), pronosticos[i][p], importe(i, p));
            }
            if (conUsuarioDePrueba && p < 2) {
                apuestas.apostar("usuario@apuestas.es", partido.getId(), Resultado.LOCAL, new BigDecimal("20"));
            }
            // Se jugó hace unos días, a las 21:00
            partido.cambiarFecha(LocalDate.now(reloj).minusDays((Integer) j[4]).atTime(LocalTime.of(21, 0)));
            eventos.save(partido);
            resolucion.introducirMarcador(partido.getId(), (Integer) j[2], (Integer) j[3]);
        }

        // Partidos próximos con apuestas activas, una combinada y una supercuota
        List<Evento> proximos = proximos(laLiga);
        for (int i = 0; i < JUGADORES.length && i < proximos.size(); i++) {
            apuestas.apostar(JUGADORES[i][0], proximos.get(i).getId(), i % 3 == 0 ? Resultado.VISITANTE : Resultado.LOCAL,
                    new BigDecimal(15 + 5 * i));
        }
        if (proximos.size() >= 2) {
            apuestas.apostar(JUGADORES[1][0], List.of(new SeleccionPedida(proximos.get(0).getId(), Resultado.LOCAL, null),
                    new SeleccionPedida(proximos.get(1).getId(), Resultado.LOCAL, null)), new BigDecimal("25"));
            if (conUsuarioDePrueba) {
                apuestas.apostar("usuario@apuestas.es", proximos.get(1).getId(), Resultado.LOCAL, new BigDecimal("15"));
            }
            Evento destacado = proximos.get(proximos.size() - 1);
            BigDecimal mejorada = calculadora.calcular(destacado).local().multiply(new BigDecimal("1.25"))
                    .setScale(2, java.math.RoundingMode.DOWN);
            supercuotas.crear(destacado.getId(), Resultado.LOCAL, mejorada, new BigDecimal("50"), 48);
        }

        // Una liga privada con casi todos
        Liga liga = ligas.crear(JUGADORES[0][0], "Liga de la clase");
        for (int i = 1; i < 5; i++) {
            ligas.unirse(JUGADORES[i][0], liga.getCodigo());
        }
        if (conUsuarioDePrueba) {
            ligas.unirse("usuario@apuestas.es", liga.getCodigo());
        }
        LOG.info("Datos de demostración cargados: {} jugadores, {} partidos jugados y {} próximos",
                JUGADORES.length, JUGADOS.length, proximos.size());
    }

    /** Importes variados, del mínimo razonable a 80 monedas, para que el ranking salga repartido. */
    private static BigDecimal importe(int jugador, int partido) {
        return new BigDecimal(10 + ((jugador * 7 + partido * 13) % 8) * 10);
    }

    private Evento partido(Competicion competicion, String local, String visitante, LocalDateTime fecha) {
        return eventos.save(new Evento(competicion, equipos.findByNombre(local).orElseThrow(),
                equipos.findByNombre(visitante).orElseThrow(), fecha));
    }

    /** Los próximos partidos de fútbol que admiten apuestas; si hay pocos (sin la API de partidos), se crean los de la demo. */
    private List<Evento> proximos(Competicion competicion) {
        LocalDateTime ahora = LocalDateTime.now(reloj);
        List<Evento> hay = eventos.findByEstadoAndFechaHoraAfterOrderByFechaHoraAsc(EstadoEvento.PROGRAMADO, ahora)
                .stream().filter(e -> e.admiteApuestas(ahora) && e.getDeporte() == Deporte.FUTBOL).toList();
        if (hay.size() < MINIMO_PROXIMOS) {
            for (Object[] p : PROXIMOS) {
                partido(competicion, (String) p[0], (String) p[1],
                        LocalDate.now(reloj).plusDays((Integer) p[2]).atTime(LocalTime.of(21, 0)));
            }
            hay = eventos.findByEstadoAndFechaHoraAfterOrderByFechaHoraAsc(EstadoEvento.PROGRAMADO, ahora)
                    .stream().filter(e -> e.admiteApuestas(ahora) && e.getDeporte() == Deporte.FUTBOL).toList();
        }
        return hay.stream().limit(8).toList();
    }
}
