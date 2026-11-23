package es.ucm.fdi.is1.apuestas.fantasy;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import es.ucm.fdi.is1.apuestas.cache.CacheDeConsulta;
import es.ucm.fdi.is1.apuestas.cache.CachesDeConsultas;
import es.ucm.fdi.is1.apuestas.equipos.Deporte;
import es.ucm.fdi.is1.apuestas.equipos.Equipo;
import es.ucm.fdi.is1.apuestas.equipos.Jugador;
import es.ucm.fdi.is1.apuestas.equipos.JugadorRepository;
import es.ucm.fdi.is1.apuestas.equipos.Posicion;
import es.ucm.fdi.is1.apuestas.eventos.EstadoEvento;
import es.ucm.fdi.is1.apuestas.eventos.Evento;
import es.ucm.fdi.is1.apuestas.eventos.EventoRepository;
import es.ucm.fdi.is1.apuestas.usuarios.Usuario;
import es.ucm.fdi.is1.apuestas.usuarios.UsuarioRepository;

/**
 * Liga fantasy (HU-81 a HU-83): cada usuario monta un equipo de siete futbolistas reales y suma puntos con los
 * partidos que juegan sus clubes. Los puntos no se guardan: se calculan cada vez con los partidos finalizados, así
 * que si el creador corrige un resultado, la clasificación se corrige sola.
 */
@Service
public class FantasyService {

    private final EquipoFantasyRepository equipos;
    private final JugadorRepository jugadores;
    private final EventoRepository eventos;
    private final UsuarioRepository usuarios;
    private final Clock reloj;
    /** Puntos de cada equipo (id → puntos): la parte cara de la clasificación. */
    private final CacheDeConsulta<String, Map<Long, Integer>> puntosPorEquipo;

    /**
     * Cuánto puede durar la caché de puntos si nadie la invalida. En esta instancia deja de valer en cuanto cambia un
     * partido (resultado, marcador, anulación…), un fichaje o un equipo; la duración solo limita lo vieja que puede
     * ser si cambian desde otra instancia.
     */
    static final Duration DURACION_CACHE = Duration.ofMinutes(5);

    public FantasyService(EquipoFantasyRepository equipos, JugadorRepository jugadores, EventoRepository eventos,
                          UsuarioRepository usuarios, Clock reloj, CachesDeConsultas caches) {
        this.equipos = equipos;
        this.jugadores = jugadores;
        this.eventos = eventos;
        this.usuarios = usuarios;
        this.reloj = reloj;
        this.puntosPorEquipo = caches.crear(DURACION_CACHE, Evento.class, FichajeFantasy.class, EquipoFantasy.class);
    }

    /** Un partido de un fichaje con los puntos que le dio. */
    public record PartidoPuntuado(Evento partido, int puntos) {
    }

    /** Un fichaje con los partidos que le han contado. */
    public record PuntosFichaje(FichajeFantasy fichaje, List<PartidoPuntuado> partidos) {

        public int getTotal() {
            return partidos.stream().mapToInt(PartidoPuntuado::puntos).sum();
        }
    }

    /**
     * La jornada de la liga fantasy: una semana, de lunes a lunes. Empieza con su primer partido de fútbol
     * ({@code plazo}). Hasta entonces, fichar y vender cuenta enseguida; después, cuenta desde la jornada siguiente:
     * el que se ficha empieza a sumar el lunes y el que se vende sigue sumando lo que queda de semana. Así cada hueco
     * suma como mucho a un jugador en cada jornada: no se puede vender a uno que ya ha jugado y fichar a otro que
     * juega más tarde para sumar los dos partidos (es la regla de las ligas fantasy más jugadas).
     *
     * @param inicio   lunes a las 00:00
     * @param fin      el lunes siguiente a las 00:00, cuando empieza la próxima
     * @param plazo    hora del primer partido de la semana; null si no hay ninguno
     * @param empezada si ya ha llegado el plazo
     */
    public record Jornada(LocalDateTime inicio, LocalDateTime fin, LocalDateTime plazo, boolean empezada) {

        /** Desde cuándo cuenta un cambio hecho {@code ahora}: enseguida o, con la jornada empezada, el lunes. */
        public LocalDateTime efecto(LocalDateTime ahora) {
            return empezada ? fin : ahora;
        }
    }

    /** Una fila de la clasificación. */
    public record PuestoFantasy(int puesto, String nombreEquipo, String nombreUsuario, int puntos, boolean mio) {
    }

    @Transactional(readOnly = true)
    public Optional<EquipoFantasy> equipoDe(String email) {
        return usuarios.findByEmail(email).flatMap(equipos::findByUsuario);
    }

    /**
     * Crea el equipo del usuario, todavía sin jugadores (HU-81).
     *
     * @throws IllegalStateException    si ya tiene uno
     * @throws IllegalArgumentException si el nombre no vale o es el creador
     */
    @Transactional
    public EquipoFantasy crear(String email, String nombre) {
        Usuario usuario = usuarios.findByEmail(email).orElseThrow();
        if (equipos.findByUsuario(usuario).isPresent()) {
            throw new IllegalStateException("Ya tienes un equipo");
        }
        return equipos.save(new EquipoFantasy(usuario, nombre, LocalDateTime.now(reloj)));
    }

    /**
     * Ficha a un jugador (CA-81.1, CA-81.2): cuenta para los partidos que empiecen a partir de ahora.
     *
     * @throws IllegalStateException    si el usuario aún no tiene equipo
     * @throws IllegalArgumentException si el jugador no existe o no se puede fichar (posición llena, presupuesto…)
     */
    @Transactional
    public FichajeFantasy fichar(String email, Long jugadorId) {
        return fichar(email, jugadorId, LocalDateTime.now(reloj));
    }

    @Transactional
    FichajeFantasy fichar(String email, Long jugadorId, LocalDateTime ahora) {
        EquipoFantasy equipo = paraCambiar(email);
        Jugador jugador = jugadores.findById(jugadorId)
                .orElseThrow(() -> new IllegalArgumentException("Ese jugador no existe"));
        return equipo.fichar(jugador, jornada(ahora).efecto(ahora));
    }

    /**
     * Vende a un jugador: se recupera su precio y deja de sumar (con la jornada empezada, al acabar la jornada); lo
     * que ya sumó se queda.
     *
     * @throws IllegalStateException    si el usuario aún no tiene equipo
     * @throws IllegalArgumentException si el jugador no está en su equipo
     */
    @Transactional
    public FichajeFantasy vender(String email, Long jugadorId) {
        return vender(email, jugadorId, LocalDateTime.now(reloj));
    }

    @Transactional
    FichajeFantasy vender(String email, Long jugadorId, LocalDateTime ahora) {
        return paraCambiar(email).vender(jugadorId, jornada(ahora).efecto(ahora));
    }

    /** Vende un fichaje por su id (también si el jugador ya no existe). */
    @Transactional
    public FichajeFantasy venderFichaje(String email, Long fichajeId) {
        return venderFichaje(email, fichajeId, LocalDateTime.now(reloj));
    }

    @Transactional
    FichajeFantasy venderFichaje(String email, Long fichajeId, LocalDateTime ahora) {
        return paraCambiar(email).venderFichaje(fichajeId, jornada(ahora).efecto(ahora));
    }

    /** La jornada fantasy de ahora. */
    @Transactional(readOnly = true)
    public Jornada jornada() {
        return jornada(LocalDateTime.now(reloj));
    }

    /** La jornada fantasy en la que cae {@code ahora}. */
    Jornada jornada(LocalDateTime ahora) {
        LocalDateTime inicio = ahora.toLocalDate().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                .atStartOfDay();
        LocalDateTime fin = inicio.plusWeeks(1);
        // Solo cuentan los partidos que pueden jugarse o ya se jugaron; los suspendidos y anulados no cierran cambios.
        LocalDateTime plazo = eventos.primerPartidoEntre(Deporte.FUTBOL,
                List.of(EstadoEvento.PROGRAMADO, EstadoEvento.FINALIZADO), inicio, fin);
        return new Jornada(inicio, fin, plazo, plazo != null && !ahora.isBefore(plazo));
    }

    /** El equipo del usuario, bloqueado para cambiarlo (ver {@link EquipoFantasyRepository#paraCambiar}). */
    private EquipoFantasy paraCambiar(String email) {
        return usuarios.findByEmail(email).flatMap(equipos::paraCambiar)
                .flatMap(equipo -> equipos.conFichajes(equipo.getId()))
                .orElseThrow(() -> new IllegalStateException("Primero crea tu equipo"));
    }

    /** Futbolistas que se pueden fichar, del más caro al más barato; posición y club son filtros opcionales. */
    @Transactional(readOnly = true)
    public List<Jugador> mercado(Posicion posicion, Long clubId) {
        return paginaMercado(posicion, clubId, 0).jugadores();
    }

    /** Página acotada del mercado, con orden de precio equivalente al cálculo de {@link EquipoFantasy#precio}. */
    @Transactional(readOnly = true)
    public PaginaMercado paginaMercado(Posicion posicion, Long clubId, int numero) {
        int pagina = Math.min(Math.max(0, numero), 10000);
        Slice<Jugador> resultado = jugadores.paginaMercado(posicion, clubId, PageRequest.of(pagina, 50));
        return new PaginaMercado(resultado.getContent(), pagina, pagina > 0, resultado.hasNext());
    }

    public record PaginaMercado(List<Jugador> jugadores, int numero, boolean anterior, boolean siguiente) {
    }

    /** Clubes de fútbol que tienen jugadores, para el filtro del mercado. */
    @Transactional(readOnly = true)
    public List<Equipo> clubes() {
        return jugadores.clubesConJugadores();
    }

    /** Puntos de cada fichaje del equipo, también de los ya vendidos (CA-82.1). */
    @Transactional(readOnly = true)
    public List<PuntosFichaje> puntos(EquipoFantasy equipo) {
        return puntos(equipo, new HashMap<>());
    }

    /**
     * Clasificación de todos los equipos, de más a menos puntos (HU-83). Con los mismos puntos, va delante el que
     * se creó antes.
     */
    @Transactional(readOnly = true)
    public List<PuestoFantasy> clasificacion(String emailActual) {
        Map<Long, Integer> puntos = puntosPorEquipo.obtener("puntos", () -> {
            Map<Long, Integer> calculados = new HashMap<>();
            equipos.puntosPorEquipo().forEach(fila -> calculados.put(fila.getId(), fila.getPuntos()));
            return Map.copyOf(calculados);
        });
        // Los nombres no se guardan con los puntos: cambiar de nombre o darse de baja se ve al momento.
        // Mismo orden que antes en SQL: puntos, el equipo creado antes y, por último, el id
        record Fila(EquipoFantasyRepository.FilaClasificacion equipo, int puntos) {
        }
        List<Fila> filas = equipos.filasClasificacion().stream()
                .map(equipo -> new Fila(equipo, puntos.getOrDefault(equipo.getId(), 0)))
                .sorted(Comparator.comparingInt(Fila::puntos).reversed()
                        .thenComparing(fila -> fila.equipo().getCreado())
                        .thenComparing(fila -> fila.equipo().getId()))
                .toList();
        List<PuestoFantasy> puestos = new ArrayList<>();
        for (int i = 0; i < filas.size(); i++) {
            EquipoFantasyRepository.FilaClasificacion equipo = filas.get(i).equipo();
            boolean mio = equipo.getEmail().equals(emailActual);
            if (i < 20 || mio) {
                puestos.add(new PuestoFantasy(i + 1, equipo.getNombreEquipo(), equipo.getNombreUsuario(),
                        filas.get(i).puntos(), mio));
            }
        }
        return puestos;
    }

    /** {@code partidosPorClub}: los partidos finalizados de cada club, para no pedirlos más de una vez. */
    private List<PuntosFichaje> puntos(EquipoFantasy equipo, Map<Long, List<Evento>> partidosPorClub) {
        List<PuntosFichaje> resultado = new ArrayList<>();
        for (FichajeFantasy fichaje : equipo.getFichajes()) {
            // Con el club y la posición de cuando se fichó (no los de ahora: ver FichajeFantasy)
            Equipo club = fichaje.getClub();
            List<Evento> partidos = partidosPorClub.computeIfAbsent(club.getId(),
                    id -> eventos.partidosDe(club, EstadoEvento.FINALIZADO));
            List<PartidoPuntuado> cuentan = partidos.stream().filter(fichaje::cuenta)
                    .map(p -> new PartidoPuntuado(p, PuntosFantasy.puntos(fichaje.getPosicion(), club, p)))
                    .toList();
            resultado.add(new PuntosFichaje(fichaje, cuentan));
        }
        return resultado;
    }
}
