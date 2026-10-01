package es.ucm.fdi.is1.apuestas.apuesta;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import es.ucm.fdi.is1.apuestas.eventos.Evento;
import es.ucm.fdi.is1.apuestas.mercados.Mercado;

public interface SeleccionRepository extends JpaRepository<Seleccion, Long> {

    List<Seleccion> findByEvento(Evento evento);

    List<Seleccion> findByCandidatoMercado(Mercado mercado);

    /** Pares (pronóstico, importe total) de las apuestas activas a un evento, para el ajuste por volumen. */
    @Query("""
            select s.pronostico, sum(s.apuesta.importe) from Seleccion s
            where s.evento = :evento and s.apuesta.estado = es.ucm.fdi.is1.apuestas.apuesta.EstadoApuesta.ACTIVA
            group by s.pronostico""")
    List<Object[]> importesActivosPorPronostico(Evento evento);
}
