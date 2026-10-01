package es.ucm.fdi.is1.apuestas.gestion;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import es.ucm.fdi.is1.apuestas.equipos.Competicion;
import es.ucm.fdi.is1.apuestas.equipos.CompeticionRepository;
import es.ucm.fdi.is1.apuestas.equipos.Equipo;
import es.ucm.fdi.is1.apuestas.equipos.EquipoRepository;
import es.ucm.fdi.is1.apuestas.eventos.Evento;
import es.ucm.fdi.is1.apuestas.eventos.EventoRepository;

/** Alta de competiciones, equipos y eventos por parte del creador de apuestas (HU-01, HU-21). */
@Service
public class GestionService {

    private final CompeticionRepository competiciones;
    private final EquipoRepository equipos;
    private final EventoRepository eventos;

    public GestionService(CompeticionRepository competiciones, EquipoRepository equipos, EventoRepository eventos) {
        this.competiciones = competiciones;
        this.equipos = equipos;
        this.eventos = eventos;
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
