package es.ucm.fdi.is1.apuestas.eventos;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import es.ucm.fdi.is1.apuestas.equipos.Competicion;
import es.ucm.fdi.is1.apuestas.equipos.Deporte;
import es.ucm.fdi.is1.apuestas.equipos.Equipo;

/** HU-19: un evento que ya ha empezado no está disponible para apostar. */
class EventoTest {

    private static final LocalDateTime AHORA = LocalDateTime.of(2026, 10, 1, 18, 0);

    private Evento eventoA(LocalDateTime fecha) {
        Competicion liga = new Competicion("LaLiga", Deporte.FUTBOL);
        return new Evento(liga, new Equipo("A", Deporte.FUTBOL, 7.0), new Equipo("B", Deporte.FUTBOL, 6.0), fecha);
    }

    @Test
    void admiteApuestasSiNoHaEmpezado() {
        assertThat(eventoA(AHORA.plusHours(1)).admiteApuestas(AHORA)).isTrue();
    }

    @Test
    void noAdmiteApuestasSiYaHaEmpezado() {
        assertThat(eventoA(AHORA.minusMinutes(1)).admiteApuestas(AHORA)).isFalse();
        assertThat(eventoA(AHORA).admiteApuestas(AHORA)).isFalse();
    }
}
