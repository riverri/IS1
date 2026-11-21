package es.ucm.fdi.is1.apuestas.fantasy;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    public FantasyService(EquipoFantasyRepository equipos, JugadorRepository jugadores, EventoRepository eventos,
                          UsuarioRepository usuarios, Clock reloj) {
        this.equipos = equipos;
        this.jugadores = jugadores;
        this.eventos = eventos;
        this.usuarios = usuarios;
        this.reloj = reloj;
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

    /** Una fila de la clasificación. */
    public record PuestoFantasy(int puesto, EquipoFantasy equipo, int puntos, boolean mio) {
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
        EquipoFantasy equipo = equipoDe(email).orElseThrow(() -> new IllegalStateException("Primero crea tu equipo"));
        Jugador jugador = jugadores.findById(jugadorId)
                .orElseThrow(() -> new IllegalArgumentException("Ese jugador no existe"));
        return equipo.fichar(jugador, LocalDateTime.now(reloj));
    }

    /**
     * Vende a un jugador: se recupera su precio y deja de sumar; lo que ya sumó se queda.
     *
     * @throws IllegalStateException    si el usuario aún no tiene equipo
     * @throws IllegalArgumentException si el jugador no está en su equipo
     */
    @Transactional
    public FichajeFantasy vender(String email, Long jugadorId) {
        EquipoFantasy equipo = equipoDe(email).orElseThrow(() -> new IllegalStateException("Primero crea tu equipo"));
        return equipo.vender(jugadorId, LocalDateTime.now(reloj));
    }

    /** Futbolistas que se pueden fichar, del más caro al más barato; posición y club son filtros opcionales. */
    @Transactional(readOnly = true)
    public List<Jugador> mercado(Posicion posicion, Long clubId) {
        return futbolistas().stream()
                .filter(j -> posicion == null || j.getPosicion() == posicion)
                .filter(j -> clubId == null || j.getEquipo().getId().equals(clubId))
                .sorted(Comparator.comparing(EquipoFantasy::precio).reversed().thenComparing(Jugador::getNombre))
                .toList();
    }

    /** Clubes de fútbol que tienen jugadores, para el filtro del mercado. */
    @Transactional(readOnly = true)
    public List<Equipo> clubes() {
        return futbolistas().stream().map(Jugador::getEquipo).distinct()
                .sorted(Comparator.comparing(Equipo::getNombre)).toList();
    }

    private List<Jugador> futbolistas() {
        return jugadores.findAll().stream().filter(j -> j.getEquipo().getDeporte() == Deporte.FUTBOL).toList();
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
        Map<Long, List<Evento>> partidosPorClub = new HashMap<>();
        record Fila(EquipoFantasy equipo, int puntos) {
        }
        List<Fila> filas = new ArrayList<>();
        for (EquipoFantasy equipo : equipos.findAll()) {
            int total = puntos(equipo, partidosPorClub).stream().mapToInt(PuntosFichaje::getTotal).sum();
            filas.add(new Fila(equipo, total));
        }
        filas.sort(Comparator.comparingInt(Fila::puntos).reversed()
                .thenComparing(f -> f.equipo().getCreado()).thenComparing(f -> f.equipo().getId()));
        List<PuestoFantasy> puestos = new ArrayList<>();
        for (int i = 0; i < filas.size(); i++) {
            EquipoFantasy equipo = filas.get(i).equipo();
            puestos.add(new PuestoFantasy(i + 1, equipo, filas.get(i).puntos(),
                    equipo.getUsuario().getEmail().equals(emailActual)));
        }
        return puestos;
    }

    /** {@code partidosPorClub}: los partidos finalizados de cada club, para no pedirlos más de una vez. */
    private List<PuntosFichaje> puntos(EquipoFantasy equipo, Map<Long, List<Evento>> partidosPorClub) {
        List<PuntosFichaje> resultado = new ArrayList<>();
        for (FichajeFantasy fichaje : equipo.getFichajes()) {
            Jugador jugador = fichaje.getJugador();
            Equipo club = jugador.getEquipo();
            List<Evento> partidos = partidosPorClub.computeIfAbsent(club.getId(),
                    id -> eventos.partidosDe(club, EstadoEvento.FINALIZADO));
            List<PartidoPuntuado> cuentan = partidos.stream().filter(fichaje::cuenta)
                    .map(p -> new PartidoPuntuado(p, PuntosFantasy.puntos(jugador.getPosicion(), club, p)))
                    .toList();
            resultado.add(new PuntosFichaje(fichaje, cuentan));
        }
        return resultado;
    }
}
