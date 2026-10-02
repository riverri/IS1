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
import es.ucm.fdi.is1.apuestas.eventos.Evento;
import es.ucm.fdi.is1.apuestas.eventos.EventoNoDisponibleException;
import es.ucm.fdi.is1.apuestas.eventos.EventoRepository;

/** Alta de competiciones, equipos y eventos por parte del creador de apuestas (HU-01, HU-21). */
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

    @Transactional
    public Competicion crearCompeticion(CompeticionForm form) {
        return competiciones.save(new Competicion(form.getNombre().trim(), form.getDeporte()));
    }

    @Transactional
    public Equipo crearEquipo(EquipoForm form) {
        Equipo equipo = new Equipo(form.getNombre().trim(), form.getDeporte(), form.getCalidad());
        for (Competicion competicion : competiciones.findAllById(form.getCompeticionIds())) {
            if (competicion.getDeporte() != form.getDeporte()) {
                throw new DatosInvalidosException("competicionIds",
                        "La competición " + competicion.getNombre() + " no es de " + form.getDeporte().getNombre());
            }
            equipo.participaEn(competicion);
        }
        return equipos.save(equipo);
    }

    @Transactional(readOnly = true)
    public Equipo equipo(Long id) {
        return equipos.findById(id).orElseThrow(() -> new DatosInvalidosException("id", "El equipo no existe"));
    }

    @Transactional
    public void editarEquipo(Long equipoId, EdicionEquipoForm form) {
        Equipo equipo = equipo(equipoId);
        equipo.setCalidad(form.getCalidad());
        equipo.setForma(form.getForma());
        String escudo = form.getEscudoUrl();
        equipo.setEscudoUrl(escudo == null || escudo.isBlank() ? null : escudo.trim());
    }

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
        if (form.getFase() != null && !form.getFase().isBlank()) {
            evento.setFase(form.getFase().trim());
        }
        return eventos.save(evento);
    }

    /**
     * Corrige un evento que aún no ha empezado (HU-46). Si ya tiene apuestas no se pueden cambiar
     * los equipos, porque cambiaría el sentido de esas apuestas; la fecha y la fase sí.
     */
    @Transactional
    public Evento editarEvento(Long eventoId, EdicionEventoForm form) {
        Evento evento = evento(eventoId);
        if (form.getLocalId().equals(form.getVisitanteId())) {
            throw new DatosInvalidosException("visitanteId", "El local y el visitante deben ser distintos");
        }
        Equipo local = equipoDeLaCompeticion(form.getLocalId(), evento.getCompeticion(), "localId");
        Equipo visitante = equipoDeLaCompeticion(form.getVisitanteId(), evento.getCompeticion(), "visitanteId");
        boolean cambianEquipos = !local.getId().equals(evento.getLocal().getId())
                || !visitante.getId().equals(evento.getVisitante().getId());
        if (cambianEquipos && selecciones.existsByEvento(evento)) {
            throw new DatosInvalidosException("localId",
                    "El evento ya tiene apuestas: no se pueden cambiar los equipos. Si está mal, anúlalo.");
        }
        String fase = form.getFase() == null || form.getFase().isBlank() ? null : form.getFase().trim();
        try {
            evento.modificar(local, visitante, form.getFechaHora(), fase, LocalDateTime.now(reloj));
        } catch (IllegalStateException e) {
            throw new DatosInvalidosException("fechaHora", e.getMessage());
        }
        return evento;
    }

    /** Borra un evento creado por error (HU-46). Si tiene apuestas solo se puede anular. */
    @Transactional
    public void borrarEvento(Long eventoId) {
        Evento evento = evento(eventoId);
        if (selecciones.existsByEvento(evento)) {
            throw new IllegalStateException("El evento tiene apuestas: no se puede borrar, solo anular "
                    + "(se devuelve el importe).");
        }
        eventos.delete(evento);
    }

    @Transactional(readOnly = true)
    public Evento evento(Long eventoId) {
        return eventos.findById(eventoId).orElseThrow(() -> new EventoNoDisponibleException(eventoId));
    }

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
