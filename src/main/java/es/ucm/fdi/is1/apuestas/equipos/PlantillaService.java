package es.ucm.fdi.is1.apuestas.equipos;

import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Plantillas de los equipos: consulta pública y alta, nota y baja de jugadores por el creador.
 * Implementa la parte de plantilla y alineación de HU-49 y la gestión a mano de HU-50 (la descarga desde
 * la API está en el paquete {@code api}). Lo usan {@code FichaEquipoService} y {@code GestionController}.
 */
@Service
public class PlantillaService {

    private final JugadorRepository jugadores;
    private final EquipoRepository equipos;

    public PlantillaService(JugadorRepository jugadores, EquipoRepository equipos) {
        this.jugadores = jugadores;
        this.equipos = equipos;
    }

    /** Jugadores agrupados por posición (de portero a delantero) y ordenados por dorsal. */
    @Transactional(readOnly = true)
    public Map<Posicion, List<Jugador>> plantilla(Equipo equipo) {
        // Primero por dorsal (sin dorsal al final) y, si empatan, por nombre.
        // nullsLast(naturalOrder()): de menor a mayor, y los null (sin dorsal) al final.
        // groupingBy reparte los jugadores en grupos según su posición: {PORTERO=[...], DEFENSA=[...], ...}
        // El EnumMap ordena los grupos como el enum Posicion: portero, defensa, centrocampista, delantero
        return jugadores.findByEquipo(equipo).stream()
                .sorted(Comparator.comparing(Jugador::getDorsal, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(Jugador::getNombre))
                .collect(Collectors.groupingBy(Jugador::getPosicion, () -> new EnumMap<>(Posicion.class),
                        Collectors.toList()));
    }

    /**
     * Alineación probable; solo en fútbol y si hay jugadores suficientes en cada posición.
     * Cómo se eligen los titulares está en {@link Alineacion#de(List)}.
     *
     * @return null en otro deporte o si faltan jugadores
     */
    @Transactional(readOnly = true)
    public Alineacion alineacion(Equipo equipo) {
        if (equipo.getDeporte() != Deporte.FUTBOL) {
            return null;
        }
        return Alineacion.de(jugadores.findByEquipo(equipo));
    }

    /**
     * El creador añade un jugador a mano (HU-50). Sin {@code idExterno}, así que la sincronización con la
     * API no lo borra. Dorsal, nota y nacionalidad son opcionales; sin nota se queda con
     * {@link Jugador#NOTA_INICIAL}.
     *
     * @throws IllegalArgumentException si el equipo no existe o algún dato no es válido; el mensaje
     *                                  se muestra tal cual al creador
     */
    @Transactional
    public Jugador anadir(Long equipoId, String nombre, Posicion posicion, Integer dorsal, Double nota,
                          String nacionalidad) {
        Equipo equipo = equipos.findById(equipoId).orElseThrow(() -> new IllegalArgumentException("El equipo no existe"));
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("Escribe el nombre del jugador");
        }
        if (nombre.trim().length() > 100 || (nacionalidad != null && nacionalidad.trim().length() > 100)) {
            throw new IllegalArgumentException("El nombre y la nacionalidad pueden tener como mucho 100 caracteres");
        }
        if (posicion == null) {
            throw new IllegalArgumentException("Elige la posición");
        }
        if (dorsal != null && (dorsal < 1 || dorsal > 99)) {
            throw new IllegalArgumentException("El dorsal debe estar entre 1 y 99");
        }
        Jugador jugador = new Jugador(equipo, nombre.trim(), posicion);
        jugador.setDorsal(dorsal);
        // Una nacionalidad en blanco se guarda como null
        jugador.setNacionalidad(nacionalidad == null || nacionalidad.isBlank() ? null : nacionalidad.trim());
        if (nota != null) {
            jugador.cambiarNota(nota);
        }
        return jugadores.save(jugador);
    }

    /**
     * El creador cambia la nota de un jugador (HU-50); con ello puede cambiar la alineación probable.
     * No hace falta {@code save}: el jugador está gestionado por JPA y el cambio se guarda al terminar
     * la transacción.
     *
     * @throws IllegalArgumentException si el jugador no existe o la nota no está entre 0 y 10
     */
    @Transactional
    public Jugador cambiarNota(Long jugadorId, double nota) {
        Jugador jugador = jugador(jugadorId);
        jugador.cambiarNota(nota);
        return jugador;
    }

    /**
     * Quita un jugador de la plantilla (HU-50), venga de la API o se añadiera a mano.
     * Devuelve el jugador borrado, para volver a la página de su equipo.
     *
     * @throws IllegalArgumentException si el jugador no existe
     */
    @Transactional
    public Jugador borrar(Long jugadorId) {
        Jugador jugador = jugador(jugadorId);
        jugadores.delete(jugador);
        return jugador;
    }

    private Jugador jugador(Long id) {
        return jugadores.findById(id).orElseThrow(() -> new IllegalArgumentException("El jugador no existe"));
    }
}
