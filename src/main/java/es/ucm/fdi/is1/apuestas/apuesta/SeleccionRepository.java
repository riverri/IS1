package es.ucm.fdi.is1.apuestas.apuesta;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import es.ucm.fdi.is1.apuestas.eventos.Evento;

public interface SeleccionRepository extends JpaRepository<Seleccion, Long> {

    List<Seleccion> findByEvento(Evento evento);
}
