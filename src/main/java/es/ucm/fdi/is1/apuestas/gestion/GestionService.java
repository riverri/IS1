package es.ucm.fdi.is1.apuestas.gestion;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import es.ucm.fdi.is1.apuestas.apuesta.SeleccionRepository;
import es.ucm.fdi.is1.apuestas.equipos.Competicion;
import es.ucm.fdi.is1.apuestas.equipos.CompeticionRepository;
import es.ucm.fdi.is1.apuestas.equipos.Equipo;
import es.ucm.fdi.is1.apuestas.equipos.EquipoRepository;
import es.ucm.fdi.is1.apuestas.eventos.EquipoNoEncontradoException;
import es.ucm.fdi.is1.apuestas.eventos.Evento;
import es.ucm.fdi.is1.apuestas.eventos.EventoNoDisponibleException;
import es.ucm.fdi.is1.apuestas.eventos.EventoRepository;

/**
 * Servicio del panel de gestión: alta de competiciones, equipos y eventos por parte del creador de apuestas
 * (HU-01, HU-21), y edición o borrado de eventos mal creados (HU-46).
 * Las reglas que incumple un formulario se avisan con {@link DatosInvalidosException}, que indica el campo,
 * para que {@link GestionController} muestre el error junto a él.
 */
@Service
public class GestionService {

    private final CompeticionRepository competiciones;
    private final EquipoRepository equipos;
    private final EventoRepository eventos;
    private final SeleccionRepository selecciones;
    private final Clock reloj;

    public GestionService(CompeticionRepository competiciones, EquipoRepository equipos, EventoRepository eventos,
                          SeleccionRepository selecciones, Clock reloj) {
        this.competiciones = competiciones;
        this.equipos = equipos;
        this.eventos = eventos;
        this.selecciones = selecciones;
        this.reloj = reloj;
    }

    /**
     * Da de alta una competición (HU-01).
     *
     * @throws DatosInvalidosException si ya hay otra con el mismo nombre (sin distinguir mayúsculas)
     */
    @Transactional
    public Competicion crearCompeticion(CompeticionForm form) {
        String nombre = form.getNombre().trim();
        // Se compara sin mayúsculas para no tener "LaLiga" y "laliga" como dos competiciones distintas
        if (competiciones.existsByNombreIgnoreCase(nombre)) {
            throw new DatosInvalidosException("nombre", "Ya hay una competición que se llama " + nombre);
        }
        return competiciones.save(new Competicion(nombre, form.getDeporte()));
    }

    /**
     * Da de alta un equipo o deportista con su calificación y las competiciones en las que participa (HU-01).
     *
     * @throws DatosInvalidosException si el nombre ya existe o alguna competición es de otro deporte
     */
    @Transactional
    public Equipo crearEquipo(EquipoForm form) {
        String nombre = form.getNombre().trim();
        if (equipos.existsByNombreIgnoreCase(nombre)) {
            // Los nombres identifican a los equipos (datos iniciales, API, mercados): no puede haber dos iguales
            throw new DatosInvalidosException("nombre", "Ya hay un equipo o deportista que se llama " + nombre
                    + ". Si es de otro deporte, añade algo que lo distinga (por ejemplo, \"" + nombre + " Baloncesto\").");
        }
        Equipo equipo = new Equipo(nombre, form.getDeporte(), form.getCalidad());
        for (Competicion competicion : competiciones.findAllById(form.getCompeticionIds())) {
            // Un equipo de fútbol no puede jugar una competición de baloncesto
            if (competicion.getDeporte() != form.getDeporte()) {
                throw new DatosInvalidosException("competicionIds",
                        "La competición " + competicion.getNombre() + " no es de " + form.getDeporte().getNombre());
            }
            equipo.participaEn(competicion);
        }
        return equipos.save(equipo);
    }

    /** @throws EquipoNoEncontradoException si no existe un equipo con ese id */
    @Transactional(readOnly = true)
    public Equipo equipo(Long id) {
        return equipos.findById(id).orElseThrow(() -> new EquipoNoEncontradoException(id));
    }

    /**
     * Cambia la calificación, la forma reciente (HU-02) y el escudo de un equipo. El nombre y el deporte
     * no se editan.
     *
     * @throws EquipoNoEncontradoException si no existe el equipo
     */
    @Transactional
    public void editarEquipo(Long equipoId, EdicionEquipoForm form) {
        Equipo equipo = equipo(equipoId);
        equipo.setCalidad(form.getCalidad());
        equipo.setForma(form.getForma());
        String escudo = form.getEscudoUrl();
        // Un escudo en blanco se guarda como null: el equipo se queda sin escudo
        equipo.setEscudoUrl(escudo == null || escudo.isBlank() ? null : escudo.trim());
    }

    /**
     * Crea un evento a mano (HU-21). Se guarda programado y aparece enseguida en el catálogo.
     *
     * @throws DatosInvalidosException si local y visitante coinciden, la competición no existe o algún equipo
     *                                 no participa en ella
     */
    @Transactional
    public Evento crearEvento(EventoForm form) {
        if (form.getLocalId().equals(form.getVisitanteId())) {
            throw new DatosInvalidosException("visitanteId", "El local y el visitante deben ser distintos");
        }
        Competicion competicion = competiciones.findById(form.getCompeticionId())
                .orElseThrow(() -> new DatosInvalidosException("competicionId", "La competición no existe"));
        Equipo local = equipoDeLaCompeticion(form.getLocalId(), competicion, "localId");
        Equipo visitante = equipoDeLaCompeticion(form.getVisitanteId(), competicion, "visitanteId");
        Evento evento = new Evento(competicion, local, visitante, form.getFechaHora());
        // La fase es opcional (jornada, semifinal...): si viene vacía el evento se queda sin ella
        if (form.getFase() != null && !form.getFase().isBlank()) {
            evento.setFase(form.getFase().trim());
        }
        return eventos.save(evento);
    }

    /**
     * Corrige un evento que aún no ha empezado (HU-46). Si ya tiene apuestas no se pueden cambiar
     * los equipos, porque cambiaría el sentido de esas apuestas; la fecha y la fase sí.
     * La competición no se cambia: los equipos nuevos tienen que participar en la del evento.
     *
     * @throws DatosInvalidosException si los datos no son válidos, el evento ya ha empezado (o no está
     *                                 programado ni suspendido) o se cambian los equipos de un evento con apuestas
     */
    @Transactional
    public Evento editarEvento(Long eventoId, EdicionEventoForm form) {
        Evento evento = evento(eventoId);
        if (form.getLocalId().equals(form.getVisitanteId())) {
            throw new DatosInvalidosException("visitanteId", "El local y el visitante deben ser distintos");
        }
        Equipo local = equipoDeLaCompeticion(form.getLocalId(), evento.getCompeticion(), "localId");
        Equipo visitante = equipoDeLaCompeticion(form.getVisitanteId(), evento.getCompeticion(), "visitanteId");
        // Se comparan los ids porque Equipo no redefine equals
        boolean cambianEquipos = !local.getId().equals(evento.getLocal().getId())
                || !visitante.getId().equals(evento.getVisitante().getId());
        if (cambianEquipos && selecciones.existsByEvento(evento)) {
            throw new DatosInvalidosException("localId",
                    "El evento ya tiene apuestas: no se pueden cambiar los equipos. Si está mal, anúlalo.");
        }
        String fase = form.getFase() == null || form.getFase().isBlank() ? null : form.getFase().trim();
        // El propio evento comprueba que aún no ha empezado; su error se convierte en un error del formulario
        try {
            evento.modificar(local, visitante, form.getFechaHora(), fase, LocalDateTime.now(reloj));
        } catch (IllegalStateException e) {
            throw new DatosInvalidosException("fechaHora", e.getMessage());
        }
        return evento;
    }

    /**
     * Borra un evento creado por error (HU-46). Si tiene apuestas solo se puede anular.
     *
     * @throws IllegalStateException si alguna apuesta (simple o combinada) tiene una selección en el evento
     */
    @Transactional
    public void borrarEvento(Long eventoId) {
        Evento evento = evento(eventoId);
        // Borrarlo dejaría apuestas apuntando a un evento que no existe; anularlo devuelve el importe
        if (selecciones.existsByEvento(evento)) {
            throw new IllegalStateException("El evento tiene apuestas: no se puede borrar, solo anular "
                    + "(se devuelve el importe).");
        }
        eventos.delete(evento);
    }

    /** @throws EventoNoDisponibleException si no existe un evento con ese id */
    @Transactional(readOnly = true)
    public Evento evento(Long eventoId) {
        return eventos.findById(eventoId).orElseThrow(() -> new EventoNoDisponibleException(eventoId));
    }

    /**
     * Busca un equipo y comprueba que juega la competición. {@code campo} es el campo del formulario
     * al que se asocia el error.
     */
    private Equipo equipoDeLaCompeticion(Long equipoId, Competicion competicion, String campo) {
        Equipo equipo = equipos.findById(equipoId)
                .orElseThrow(() -> new DatosInvalidosException(campo, "El equipo no existe"));
        if (!equipo.participaEn(competicion.getId())) {
            throw new DatosInvalidosException(campo,
                    equipo.getNombre() + " no participa en " + competicion.getNombre());
        }
        return equipo;
    }

    @Transactional(readOnly = true)
    public List<Competicion> competiciones() {
        return competiciones.findAllByOrderByDeporteAscNombreAsc();
    }

    @Transactional(readOnly = true)
    public List<Equipo> equipos() {
        return equipos.findAllByOrderByDeporteAscNombreAsc();
    }

    @Transactional(readOnly = true)
    public List<Evento> eventos() {
        return eventos.findAllByOrderByFechaHoraAsc();
    }
}
