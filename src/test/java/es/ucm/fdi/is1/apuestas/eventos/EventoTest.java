package es.ucm.fdi.is1.apuestas.eventos;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import es.ucm.fdi.is1.apuestas.cuotas.Resultado;
import es.ucm.fdi.is1.apuestas.equipos.Competicion;
import es.ucm.fdi.is1.apuestas.equipos.Deporte;
import es.ucm.fdi.is1.apuestas.equipos.Equipo;

/**
 * Estados de un evento sin arrancar Spring: cuándo admite apuestas (HU-19), resultado y marcador (HU-04, HU-52),
 * suspender, reactivar y anular (HU-05) y editar (HU-46).
 */
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

    private Evento baloncesto(LocalDateTime fecha) {
        Competicion acb = new Competicion("Liga ACB", Deporte.BALONCESTO);
        return new Evento(acb, new Equipo("C", Deporte.BALONCESTO, 7.0), new Equipo("D", Deporte.BALONCESTO, 6.0), fecha);
    }

    @Test
    void unSuspendidoOFinalizadoNoAdmiteApuestas() {
        Evento suspendido = eventoA(AHORA.plusDays(1));
        suspendido.suspender();
        assertThat(suspendido.admiteApuestas(AHORA)).isFalse();
        Evento finalizado = eventoA(AHORA.plusDays(1));
        finalizado.finalizar(Resultado.LOCAL);
        assertThat(finalizado.admiteApuestas(AHORA)).isFalse();
    }

    @Test
    void haEmpezadoJustoALaHora() {
        assertThat(eventoA(AHORA).haEmpezado(AHORA)).isTrue();
        assertThat(eventoA(AHORA.minusSeconds(1)).haEmpezado(AHORA)).isTrue();
        assertThat(eventoA(AHORA.plusSeconds(1)).haEmpezado(AHORA)).isFalse();
    }

    @Test
    void elMarcadorDecideElGanadorYSeGuarda() {
        Evento evento = eventoA(AHORA);
        evento.finalizarConMarcador(2, 1);
        assertThat(evento.getResultado()).isEqualTo(Resultado.LOCAL);
        assertThat(evento.getGolesLocal()).isEqualTo(2);
        assertThat(evento.getGolesVisitante()).isEqualTo(1);
        assertThat(evento.isConMarcador()).isTrue();
        assertThat(evento.getEstado()).isEqualTo(EstadoEvento.FINALIZADO);

        evento.finalizarConMarcador(0, 3);
        assertThat(evento.getResultado()).isEqualTo(Resultado.VISITANTE);
        evento.finalizarConMarcador(1, 1);
        assertThat(evento.getResultado()).isEqualTo(Resultado.EMPATE);
        evento.finalizarConMarcador(0, 0);
        assertThat(evento.getResultado()).isEqualTo(Resultado.EMPATE);
    }

    @Test
    void ponerSoloElGanadorBorraElMarcadorAnterior() {
        Evento evento = eventoA(AHORA);
        evento.finalizarConMarcador(2, 1);
        evento.finalizar(Resultado.VISITANTE);
        assertThat(evento.getResultado()).isEqualTo(Resultado.VISITANTE);
        assertThat(evento.getGolesLocal()).isNull();
        assertThat(evento.getGolesVisitante()).isNull();
        assertThat(evento.isConMarcador()).isFalse();
    }

    @Test
    void elMarcadorNoAdmiteGolesNegativosNiDeportesSinGoles() {
        assertThatThrownBy(() -> eventoA(AHORA).finalizarConMarcador(-1, 0)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> eventoA(AHORA).finalizarConMarcador(0, -1)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> baloncesto(AHORA).finalizarConMarcador(80, 70))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("solo se indica el ganador");
        Evento cero = eventoA(AHORA);
        cero.finalizarConMarcador(0, 0);
        assertThat(cero.getGolesLocal()).isZero();
    }

    @Test
    void enBaloncestoNoHayEmpateYUnAnuladoNoTieneResultado() {
        assertThatThrownBy(() -> baloncesto(AHORA).finalizar(Resultado.EMPATE))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("no hay empate");
        Evento anulado = eventoA(AHORA);
        anulado.anular();
        assertThatThrownBy(() -> anulado.finalizar(Resultado.LOCAL)).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void anularBorraElResultadoYNoSePuedeAnularDosVeces() {
        Evento evento = eventoA(AHORA);
        evento.finalizarConMarcador(3, 0);
        evento.anular();
        assertThat(evento.getEstado()).isEqualTo(EstadoEvento.ANULADO);
        assertThat(evento.getResultado()).isNull();
        assertThat(evento.getGolesLocal()).isNull();
        assertThat(evento.getGolesVisitante()).isNull();
        assertThatThrownBy(evento::anular).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void suspenderYReactivar() {
        Evento evento = eventoA(AHORA.plusDays(1));
        evento.suspenderPorApi();
        assertThat(evento.getEstado()).isEqualTo(EstadoEvento.SUSPENDIDO);
        assertThat(evento.isSuspendidoPorApi()).isTrue();
        assertThatThrownBy(evento::suspender).isInstanceOf(IllegalStateException.class);

        evento.reactivar(AHORA);
        assertThat(evento.getEstado()).isEqualTo(EstadoEvento.PROGRAMADO);
        assertThat(evento.isSuspendidoPorApi()).isFalse();
        assertThatThrownBy(() -> evento.reactivar(AHORA)).isInstanceOf(IllegalStateException.class);

        // Suspendido por el creador: la marca de la API queda quitada
        evento.suspender();
        assertThat(evento.isSuspendidoPorApi()).isFalse();
    }

    @Test
    void noSeReactivaSiYaHaPasadoSuHora() {
        Evento evento = eventoA(AHORA.plusHours(1));
        evento.suspender();
        assertThatThrownBy(() -> evento.reactivar(AHORA.plusHours(1)))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("nueva fecha");
        assertThat(evento.getEstado()).isEqualTo(EstadoEvento.SUSPENDIDO);
    }

    @Test
    void seEditaProgramadoSinEmpezarOSuspendidoAunquePaseSuHora() {
        assertThat(eventoA(AHORA.plusMinutes(1)).editable(AHORA)).isTrue();
        assertThat(eventoA(AHORA).editable(AHORA)).isFalse();
        Evento suspendido = eventoA(AHORA.minusDays(1));
        suspendido.suspender();
        assertThat(suspendido.editable(AHORA)).isTrue();
        Evento finalizado = eventoA(AHORA.plusDays(1));
        finalizado.finalizar(Resultado.LOCAL);
        assertThat(finalizado.editable(AHORA)).isFalse();
        assertThatThrownBy(() -> finalizado.modificar(finalizado.getLocal(), finalizado.getVisitante(), AHORA.plusDays(2),
                null, AHORA)).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void modificarCambiaLosDatosYMarcaLaFechaManualSoloEnLosDeLaApi() {
        Evento manual = eventoA(AHORA.plusDays(1));
        Equipo otro = new Equipo("E", Deporte.FUTBOL, 5.0);
        manual.modificar(otro, manual.getVisitante(), AHORA.plusDays(3), "Jornada 9", AHORA);
        assertThat(manual.getLocal()).isSameAs(otro);
        assertThat(manual.getFechaHora()).isEqualTo(AHORA.plusDays(3));
        assertThat(manual.getFase()).isEqualTo("Jornada 9");
        assertThat(manual.isFechaManual()).isFalse();

        Evento deLaApi = eventoA(AHORA.plusDays(1));
        deLaApi.setIdExterno(123L);
        deLaApi.modificar(deLaApi.getLocal(), deLaApi.getVisitante(), AHORA.plusDays(1), null, AHORA);
        assertThat(deLaApi.isFechaManual()).as("misma fecha").isFalse();
        deLaApi.modificar(deLaApi.getLocal(), deLaApi.getVisitante(), AHORA.plusDays(2), null, AHORA);
        assertThat(deLaApi.isFechaManual()).isTrue();
    }

    @Test
    void laFechaDeLaApiSoloCambiaSiNoSeHaJugado() {
        Evento programado = eventoA(AHORA.plusDays(1));
        programado.cambiarFecha(AHORA.plusDays(4));
        assertThat(programado.getFechaHora()).isEqualTo(AHORA.plusDays(4));
        Evento suspendido = eventoA(AHORA.plusDays(1));
        suspendido.suspender();
        suspendido.cambiarFecha(AHORA.plusDays(5));
        assertThat(suspendido.getFechaHora()).isEqualTo(AHORA.plusDays(5));
        Evento finalizado = eventoA(AHORA.plusDays(1));
        finalizado.finalizar(Resultado.LOCAL);
        finalizado.cambiarFecha(AHORA.plusDays(6));
        assertThat(finalizado.getFechaHora()).isEqualTo(AHORA.plusDays(1));
        Evento anulado = eventoA(AHORA.plusDays(1));
        anulado.anular();
        anulado.cambiarFecha(AHORA.plusDays(6));
        assertThat(anulado.getFechaHora()).isEqualTo(AHORA.plusDays(1));
    }

    @Test
    void elResultadoManualSeMarca() {
        Evento evento = eventoA(AHORA);
        assertThat(evento.isResultadoManual()).isFalse();
        evento.marcarResultadoManual();
        assertThat(evento.isResultadoManual()).isTrue();
    }
}
