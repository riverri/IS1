package es.ucm.fdi.is1.apuestas.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import es.ucm.fdi.is1.apuestas.Hora;
import es.ucm.fdi.is1.apuestas.apuesta.Apuesta;
import es.ucm.fdi.is1.apuestas.apuesta.ApuestaService;
import es.ucm.fdi.is1.apuestas.apuesta.EstadoApuesta;
import es.ucm.fdi.is1.apuestas.apuesta.ResolucionService;
import es.ucm.fdi.is1.apuestas.apuesta.SeleccionPedida;
import es.ucm.fdi.is1.apuestas.cuotas.Especial;
import es.ucm.fdi.is1.apuestas.cuotas.Resultado;
import es.ucm.fdi.is1.apuestas.equipos.EquipoRepository;
import es.ucm.fdi.is1.apuestas.eventos.EstadoEvento;
import es.ucm.fdi.is1.apuestas.eventos.Evento;
import es.ucm.fdi.is1.apuestas.eventos.EventoRepository;

/** Fila 13 / HU-21: sincronización de eventos y resultados con la API (con la API simulada). */
@SpringBootTest(properties = {"apuestas.api.token=clave-de-prueba", "apuestas.api.retraso-inicial=PT1H"})
@Transactional
class SincronizacionServiceTest {

    @MockitoBean
    private FuenteDatosDeportivos fuente;

    @Autowired
    private SincronizacionService sincronizacion;

    @Autowired
    private EventoRepository eventos;

    @Autowired
    private EquipoRepository equipos;

    @Autowired
    private ApuestaService apuestas;

    @Autowired
    private ResolucionService resolucion;

    private static String utc(LocalDateTime madrid) {
        return madrid.atZone(ZoneId.of("Europe/Madrid")).toInstant().toString();
    }

    private static PartidoApi partido(long id, LocalDateTime fecha, String estado, String ganador,
                                      PartidoApi.EquipoApi local, PartidoApi.EquipoApi visitante) {
        return new PartidoApi(id, utc(fecha), estado, 10, local, visitante, new PartidoApi.MarcadorApi(ganador));
    }

    private static PartidoApi.EquipoApi equipo(int id, String nombre, String corto) {
        return new PartidoApi.EquipoApi(id, nombre, corto, "https://crests.football-data.org/" + id + ".png");
    }

    private void devuelve(PartidoApi... partidos) {
        when(fuente.partidos(eq("PD"), any(), any())).thenReturn(List.of(partidos));
        when(fuente.partidos(eq("CL"), any(), any())).thenReturn(List.of());
    }

    @Test
    void creaLosPartidosYEquiposQueFaltan() {
        LocalDateTime fecha = Hora.ahora().plusDays(3).withNano(0).withSecond(0);
        devuelve(partido(900001, fecha, "TIMED", null,
                equipo(745, "CD Leganés", "Leganés"), equipo(250, "Real Valladolid CF", "Valladolid")));

        ResumenSincronizacion resumen = sincronizacion.sincronizar();

        assertThat(resumen.getEventosNuevos()).isEqualTo(1);
        assertThat(resumen.getEquiposNuevos()).isEqualTo(2);
        Evento evento = eventos.findByIdExterno(900001L).orElseThrow();
        assertThat(evento.getLocal().getNombre()).isEqualTo("CD Leganés");
        assertThat(evento.getLocal().getEscudoUrl()).isEqualTo("https://crests.football-data.org/745.png");
        assertThat(evento.getFechaHora()).isEqualTo(fecha);
        assertThat(evento.getFase()).isEqualTo("Jornada 10");
        assertThat(evento.getCompeticion().getNombre()).isEqualTo("LaLiga");
    }

    @Test
    void enlazaConLosPartidosYEquiposQueYaExistian() {
        long antes = eventos.count();
        // Barça - Getafe de la jornada 8 ya está cargado a mano; en la API el Barça se llama igual
        // y el Getafe se reconoce por su identificador o por el nombre
        devuelve(partido(900002, LocalDateTime.of(2026, 10, 10, 18, 30), "TIMED", null,
                equipo(81, "FC Barcelona", "Barça"), equipo(82, "Getafe CF", "Getafe")));

        ResumenSincronizacion resumen = sincronizacion.sincronizar();

        assertThat(resumen.getEventosNuevos()).isZero();
        assertThat(resumen.getEquiposNuevos()).isZero();
        assertThat(eventos.count()).isEqualTo(antes);
        assertThat(eventos.findByIdExterno(900002L).orElseThrow().getLocal().getNombre()).isEqualTo("FC Barcelona");
    }

    @Test
    void reconoceEquiposConNombresDistintos() {
        equipos.findByNombre("Atlético de Madrid").orElseThrow().setIdExterno(null);
        devuelve(partido(900003, Hora.ahora().plusDays(2), "TIMED", null,
                equipo(78, "Club Atlético de Madrid", "Atleti"), equipo(82, "Getafe CF", "Getafe")));

        ResumenSincronizacion resumen = sincronizacion.sincronizar();

        assertThat(resumen.getEquiposNuevos()).isZero();
        assertThat(equipos.findByIdExterno(78).orElseThrow().getNombre()).isEqualTo("Atlético de Madrid");
    }

    @Test
    void unPartidoTerminadoResuelveLasApuestas() {
        LocalDateTime fecha = Hora.ahora().plusHours(1).withNano(0).withSecond(0);
        PartidoApi.EquipoApi elche = equipo(285, "Elche CF", "Elche");
        PartidoApi.EquipoApi celta = equipo(558, "RC Celta de Vigo", "Celta");
        devuelve(partido(900004, fecha, "TIMED", null, elche, celta));
        sincronizacion.sincronizar();
        Evento evento = eventos.findByIdExterno(900004L).orElseThrow();
        Apuesta apuesta = apuestas.apostar("usuario@apuestas.es", evento.getId(), Resultado.VISITANTE,
                new BigDecimal("10"));

        devuelve(partido(900004, fecha, "FINISHED", "AWAY_TEAM", elche, celta));
        ResumenSincronizacion resumen = sincronizacion.sincronizar();

        assertThat(resumen.getResultados()).isEqualTo(1);
        assertThat(evento.getEstado()).isEqualTo(EstadoEvento.FINALIZADO);
        assertThat(evento.getResultado()).isEqualTo(Resultado.VISITANTE);
        assertThat(apuesta.getEstado()).isEqualTo(EstadoApuesta.GANADA);

        // Volver a sincronizar no vuelve a pagar
        assertThat(sincronizacion.sincronizar().getResultados()).isZero();
    }

    @Test
    void conElMarcadorSeResuelvenTambienLasApuestasDeGoles() {
        LocalDateTime fecha = Hora.ahora().plusHours(1).withNano(0).withSecond(0);
        PartidoApi.EquipoApi elche = equipo(285, "Elche CF", "Elche");
        PartidoApi.EquipoApi celta = equipo(558, "RC Celta de Vigo", "Celta");
        devuelve(partido(900006, fecha, "TIMED", null, elche, celta));
        sincronizacion.sincronizar();
        Evento evento = eventos.findByIdExterno(900006L).orElseThrow();
        Apuesta mas = apuestas.apostar("usuario@apuestas.es",
                List.of(new SeleccionPedida(evento.getId(), null, Especial.MAS_2_5, null)), new BigDecimal("10"));
        Apuesta ambos = apuestas.apostar("usuario@apuestas.es",
                List.of(new SeleccionPedida(evento.getId(), null, Especial.AMBOS_SI, null)), new BigDecimal("10"));

        devuelve(new PartidoApi(900006L, utc(fecha), "FINISHED", 10, elche, celta,
                new PartidoApi.MarcadorApi("AWAY_TEAM", "REGULAR", new PartidoApi.GolesApi(0, 3))));
        sincronizacion.sincronizar();

        assertThat(evento.getGolesLocal()).isZero();
        assertThat(evento.getGolesVisitante()).isEqualTo(3);
        assertThat(evento.getResultado()).isEqualTo(Resultado.VISITANTE);
        assertThat(mas.getEstado()).isEqualTo(EstadoApuesta.GANADA);
        assertThat(ambos.getEstado()).isEqualTo(EstadoApuesta.PERDIDA);
        assertThat(sincronizacion.sincronizar().getResultados()).isZero();
    }

    /** F-07: con prórroga o penaltis, las apuestas se deciden con el marcador de los 90 minutos. */
    @Test
    void conPenaltisCuentaElMarcadorDeLos90Minutos() {
        LocalDateTime fecha = Hora.ahora().plusHours(1).withNano(0).withSecond(0);
        PartidoApi.EquipoApi elche = equipo(285, "Elche CF", "Elche");
        PartidoApi.EquipoApi celta = equipo(558, "RC Celta de Vigo", "Celta");
        devuelve(partido(900007, fecha, "TIMED", null, elche, celta));
        sincronizacion.sincronizar();
        Evento evento = eventos.findByIdExterno(900007L).orElseThrow();
        Apuesta empate = apuestas.apostar("usuario@apuestas.es", evento.getId(), Resultado.EMPATE, new BigDecimal("10"));
        Apuesta mas = apuestas.apostar("usuario@apuestas.es",
                List.of(new SeleccionPedida(evento.getId(), null, Especial.MAS_2_5, null)), new BigDecimal("10"));

        // Gana el local en los penaltis, pero a los 90 minutos iban 1-1
        devuelve(new PartidoApi(900007L, utc(fecha), "FINISHED", 10, elche, celta,
                new PartidoApi.MarcadorApi("HOME_TEAM", "PENALTY_SHOOTOUT", new PartidoApi.GolesApi(6, 5),
                        new PartidoApi.GolesApi(1, 1))));
        sincronizacion.sincronizar();

        assertThat(evento.getResultado()).isEqualTo(Resultado.EMPATE);
        assertThat(evento.getGolesLocal()).isEqualTo(1);
        assertThat(empate.getEstado()).isEqualTo(EstadoApuesta.GANADA);
        assertThat(mas.getEstado()).isEqualTo(EstadoApuesta.PERDIDA);
    }

    @Test
    void conProrrogaYSinMarcadorDeLos90MinutosSeAvisaYNoSeResuelve() {
        LocalDateTime fecha = Hora.ahora().plusHours(1).withNano(0).withSecond(0);
        PartidoApi.EquipoApi elche = equipo(285, "Elche CF", "Elche");
        PartidoApi.EquipoApi celta = equipo(558, "RC Celta de Vigo", "Celta");
        devuelve(partido(900008, fecha, "TIMED", null, elche, celta));
        sincronizacion.sincronizar();
        Evento evento = eventos.findByIdExterno(900008L).orElseThrow();

        devuelve(new PartidoApi(900008L, utc(fecha), "FINISHED", 10, elche, celta,
                new PartidoApi.MarcadorApi("HOME_TEAM", "EXTRA_TIME", new PartidoApi.GolesApi(2, 1))));
        ResumenSincronizacion resumen = sincronizacion.sincronizar();

        assertThat(evento.getEstado()).isNotEqualTo(EstadoEvento.FINALIZADO);
        assertThat(resumen.getErrores()).anyMatch(e -> e.contains("introdúcelo a mano"));
    }

    /** N-02: un partido que anuló el creador no rompe la sincronización ni se le pone resultado. */
    @Test
    void unPartidoAnuladoNoBloqueaLosDemas() {
        LocalDateTime fecha = Hora.ahora().plusHours(1).withNano(0).withSecond(0);
        PartidoApi.EquipoApi elche = equipo(285, "Elche CF", "Elche");
        PartidoApi.EquipoApi celta = equipo(558, "RC Celta de Vigo", "Celta");
        PartidoApi.EquipoApi levante = equipo(88, "Levante UD", "Levante");
        PartidoApi.EquipoApi sevilla = equipo(559, "Sevilla FC", "Sevilla");
        devuelve(partido(900009, fecha, "TIMED", null, elche, celta),
                partido(900010, fecha, "TIMED", null, levante, sevilla));
        sincronizacion.sincronizar();
        Evento anulado = eventos.findByIdExterno(900009L).orElseThrow();
        Evento otro = eventos.findByIdExterno(900010L).orElseThrow();
        resolucion.anular(anulado.getId());

        devuelve(partido(900009, fecha, "FINISHED", "HOME_TEAM", elche, celta),
                partido(900010, fecha, "FINISHED", "AWAY_TEAM", levante, sevilla));
        ResumenSincronizacion resumen = sincronizacion.sincronizar();

        assertThat(anulado.getEstado()).isEqualTo(EstadoEvento.ANULADO);
        assertThat(otro.getResultado()).isEqualTo(Resultado.VISITANTE);
        assertThat(resumen.getErrores()).isEmpty();
    }

    /** N-06: lo que suspende el creador no lo reactiva la API; lo que aplaza la API, sí. */
    @Test
    void laApiSoloReactivaLoQueEllaSuspendio() {
        LocalDateTime fecha = Hora.ahora().plusDays(2).withNano(0).withSecond(0);
        PartidoApi.EquipoApi elche = equipo(285, "Elche CF", "Elche");
        PartidoApi.EquipoApi celta = equipo(558, "RC Celta de Vigo", "Celta");
        PartidoApi.EquipoApi levante = equipo(88, "Levante UD", "Levante");
        PartidoApi.EquipoApi sevilla = equipo(559, "Sevilla FC", "Sevilla");
        devuelve(partido(900011, fecha, "TIMED", null, elche, celta),
                partido(900012, fecha, "POSTPONED", null, levante, sevilla));
        sincronizacion.sincronizar();
        Evento manual = eventos.findByIdExterno(900011L).orElseThrow();
        Evento aplazado = eventos.findByIdExterno(900012L).orElseThrow();
        resolucion.suspender(manual.getId());

        devuelve(partido(900011, fecha, "TIMED", null, elche, celta),
                partido(900012, fecha, "TIMED", null, levante, sevilla));
        sincronizacion.sincronizar();

        assertThat(manual.getEstado()).isEqualTo(EstadoEvento.SUSPENDIDO);
        assertThat(aplazado.getEstado()).isEqualTo(EstadoEvento.PROGRAMADO);
    }

    @Test
    void aplazadoSeSuspendeYCanceladoSeAnula() {
        LocalDateTime fecha = Hora.ahora().plusDays(4).withNano(0).withSecond(0);
        PartidoApi.EquipoApi sevilla = equipo(559, "Sevilla FC", "Sevilla");
        PartidoApi.EquipoApi betis = equipo(90, "Real Betis Balompié", "Betis");
        devuelve(partido(900005, fecha, "POSTPONED", null, sevilla, betis));
        sincronizacion.sincronizar();
        Evento evento = eventos.findByIdExterno(900005L).orElseThrow();
        assertThat(evento.getEstado()).isEqualTo(EstadoEvento.SUSPENDIDO);

        devuelve(partido(900005, fecha, "TIMED", null, sevilla, betis));
        sincronizacion.sincronizar();
        assertThat(evento.getEstado()).isEqualTo(EstadoEvento.PROGRAMADO);

        devuelve(partido(900005, fecha, "CANCELLED", null, sevilla, betis));
        sincronizacion.sincronizar();
        assertThat(evento.getEstado()).isEqualTo(EstadoEvento.ANULADO);
    }

    @Test
    void sincronizarDosVecesNoDuplica() {
        LocalDateTime fecha = Hora.ahora().plusDays(5).withNano(0).withSecond(0);
        devuelve(partido(900006, fecha, "SCHEDULED", null,
                equipo(745, "CD Leganés", "Leganés"), equipo(250, "Real Valladolid CF", "Valladolid")));

        sincronizacion.sincronizar();
        long eventosTrasPrimera = eventos.count();
        long equiposTrasPrimera = equipos.count();
        ResumenSincronizacion segunda = sincronizacion.sincronizar();

        assertThat(eventos.count()).isEqualTo(eventosTrasPrimera);
        assertThat(equipos.count()).isEqualTo(equiposTrasPrimera);
        assertThat(segunda.getEventosNuevos()).isZero();
    }

    @Test
    void cambiaLaFechaSiLaApiLaMueve() {
        LocalDateTime fecha = Hora.ahora().plusDays(6).withNano(0).withSecond(0);
        PartidoApi.EquipoApi a = equipo(745, "CD Leganés", "Leganés");
        PartidoApi.EquipoApi b = equipo(250, "Real Valladolid CF", "Valladolid");
        devuelve(partido(900007, fecha, "TIMED", null, a, b));
        sincronizacion.sincronizar();

        devuelve(partido(900007, fecha.plusHours(3), "TIMED", null, a, b));
        sincronizacion.sincronizar();

        assertThat(eventos.findByIdExterno(900007L).orElseThrow().getFechaHora()).isEqualTo(fecha.plusHours(3));
    }
}

@SpringBootTest
class SincronizacionSinClaveTest {

    @Autowired
    private SincronizacionService sincronizacion;

    @Test
    void sinClaveAvisaDeQueFaltaConfigurarla() {
        assertThatThrownBy(() -> sincronizacion.sincronizar())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("FOOTBALL_DATA_TOKEN");
    }
}
