package es.ucm.fdi.is1.apuestas.equipos;

import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Plantillas de los equipos: consulta pública y alta, nota y baja de jugadores por el creador. */
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
        return jugadores.findByEquipo(equipo).stream()
                .sorted(Comparator.comparing(Jugador::getDorsal, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(Jugador::getNombre))
                .collect(Collectors.groupingBy(Jugador::getPosicion, () -> new EnumMap<>(Posicion.class),
                        Collectors.toList()));
    }

    /** Alineación probable; solo en fútbol y si hay jugadores suficientes en cada posición. */
    @Transactional(readOnly = true)
    public Alineacion alineacion(Equipo equipo) {
        if (equipo.getDeporte() != Deporte.FUTBOL) {
            return null;
        }
        return Alineacion.de(jugadores.findByEquipo(equipo));
    }

    @Transactional
    public Jugador anadir(Long equipoId, String nombre, Posicion posicion, Integer dorsal, Double nota,
                          String nacionalidad) {
        Equipo equipo = equipos.findById(equipoId).orElseThrow(() -> new IllegalArgumentException("El equipo no existe"));
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("Escribe el nombre del jugador");
        }
        if (posicion == null) {
            throw new IllegalArgumentException("Elige la posición");
        }
        if (dorsal != null && (dorsal < 1 || dorsal > 99)) {
            throw new IllegalArgumentException("El dorsal debe estar entre 1 y 99");
        }
        Jugador jugador = new Jugador(equipo, nombre.trim(), posicion);
        jugador.setDorsal(dorsal);
        jugador.setNacionalidad(nacionalidad == null || nacionalidad.isBlank() ? null : nacionalidad.trim());
        if (nota != null) {
            jugador.cambiarNota(nota);
        }
        return jugadores.save(jugador);
    }

    @Transactional
    public Jugador cambiarNota(Long jugadorId, double nota) {
        Jugador jugador = jugador(jugadorId);
        jugador.cambiarNota(nota);
        return jugador;
    }

    /** Devuelve el jugador borrado, para volver a la página de su equipo. */
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
