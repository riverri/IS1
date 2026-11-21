package es.ucm.fdi.is1.apuestas.apuesta;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import es.ucm.fdi.is1.apuestas.eventos.Evento;
import es.ucm.fdi.is1.apuestas.mercados.Mercado;

/**
 * Repositorio de {@link Seleccion}. Se busca por selección y no por apuesta porque al resolver un evento
 * o un mercado hay que encontrar todas las apuestas que lo incluyen, sean simples o combinadas.
 */
public interface SeleccionRepository extends JpaRepository<Seleccion, Long> {

    /** Todas las selecciones de un evento, de apuestas en cualquier estado. */
    List<Seleccion> findByEvento(Evento evento);

    /** Si alguien ha apostado alguna vez al evento: entonces no se puede borrar ni cambiar de equipos (HU-46). */
    boolean existsByEvento(Evento evento);

    /**
     * Selecciones a cualquier candidato del mercado (navega {@code candidato.mercado}): en el nombre del
     * método, "CandidatoMercado" significa "el atributo mercado del atributo candidato".
     */
    List<Seleccion> findByCandidatoMercado(Mercado mercado);

    /**
     * Por cada selección 1X2 de una apuesta activa al evento: [pronóstico, id del usuario, importe de la apuesta,
     * número de selecciones de la apuesta], para el ajuste por volumen.
     */
    @Query("""
            select s.pronostico, s.apuesta.usuario.id, s.apuesta.importe, size(s.apuesta.selecciones)
            from Seleccion s
            where s.evento = :evento and s.pronostico is not null
              and s.apuesta.estado = es.ucm.fdi.is1.apuestas.apuesta.EstadoApuesta.ACTIVA""")
    List<Object[]> seleccionesActivasPorPronostico(Evento evento);

    /**
     * Igual que arriba, sin las apuestas de un usuario: al valorar su cash-out no cuenta nada de lo que él
     * mismo tiene apostado (HU-75), para que no pueda mover la cuota con otra apuesta suya y cancelarla después.
     */
    @Query("""
            select s.pronostico, s.apuesta.usuario.id, s.apuesta.importe, size(s.apuesta.selecciones)
            from Seleccion s
            where s.evento = :evento and s.pronostico is not null
              and s.apuesta.estado = es.ucm.fdi.is1.apuestas.apuesta.EstadoApuesta.ACTIVA
              and (:usuarioExcluido is null or s.apuesta.usuario.id <> :usuarioExcluido)""")
    List<Object[]> seleccionesActivasPorPronosticoExcepto(Evento evento, Long usuarioExcluido);

    /**
     * Si el usuario tiene o ha tenido (aunque la cancelara) otra apuesta en alguno de estos eventos. Con ella pudo
     * mover la cuota a la que aceptó la combinada, así que su cash-out no puede pagar más de lo apostado (HU-75).
     */
    @Query("""
            select count(s) > 0 from Seleccion s
            where s.evento.id in :eventos and s.apuesta.usuario.id = :usuario and s.apuesta.id <> :apuesta""")
    boolean hayOtrasApuestasDelUsuario(Collection<Long> eventos, Long usuario, Long apuesta);

    /**
     * Lo mismo para muchas apuestas a la vez: pares [id de la apuesta, id del evento] de todas las apuestas del
     * usuario, en cualquier estado, en esos eventos. Con ellos se responde a la pregunta anterior en memoria.
     */
    @Query("""
            select s.apuesta.id, s.evento.id from Seleccion s
            where s.evento.id in :eventos and s.apuesta.usuario.id = :usuario""")
    List<Object[]> apuestasDelUsuarioEnEventos(Collection<Long> eventos, Long usuario);

    /** Volumen 1X2 de todos los eventos visibles, para calcular las cuotas de un catálogo en lote. */
    @Query("""
            select s.evento.id, s.pronostico, s.apuesta.usuario.id, s.apuesta.importe, size(s.apuesta.selecciones)
            from Seleccion s
            where s.evento in :eventos and s.pronostico is not null
              and s.apuesta.estado = es.ucm.fdi.is1.apuestas.apuesta.EstadoApuesta.ACTIVA""")
    List<Object[]> seleccionesActivasPorPronosticoDeEventos(List<Evento> eventos);

    /** Selecciones a eventos (no a mercados) de apuestas activas, con su apuesta y su evento: riesgo (HU-76). */
    @Query("""
            select s from Seleccion s join fetch s.apuesta a join fetch s.evento
            where a.estado = es.ucm.fdi.is1.apuestas.apuesta.EstadoApuesta.ACTIVA""")
    List<Seleccion> activasDeEventos();
}
