package es.ucm.fdi.is1.apuestas.apuesta;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import es.ucm.fdi.is1.apuestas.Hora;
import es.ucm.fdi.is1.apuestas.cuotas.CuotaHistoricaRepository;
import es.ucm.fdi.is1.apuestas.cuotas.Resultado;
import es.ucm.fdi.is1.apuestas.equipos.CompeticionRepository;
import es.ucm.fdi.is1.apuestas.equipos.EquipoRepository;
import es.ucm.fdi.is1.apuestas.eventos.Evento;
import es.ucm.fdi.is1.apuestas.eventos.EventoRepository;
import es.ucm.fdi.is1.apuestas.ligas.BloqueoLigaService;
import es.ucm.fdi.is1.apuestas.usuarios.MovimientoSaldoRepository;
import es.ucm.fdi.is1.apuestas.usuarios.Rol;
import es.ucm.fdi.is1.apuestas.usuarios.UsuarioRepository;
import es.ucm.fdi.is1.apuestas.usuarios.UsuarioService;

/**
 * Séptima depuración: una combinada bloqueaba sus partidos en el orden del boleto y el cobro anticipado (u otra
 * combinada) en otro orden; a la vez, cada una esperaba a la otra. Sin transacción, como en la aplicación.
 */
@SpringBootTest
class InterbloqueoCombinadaTest {

    private static final String ANA = "ana-interbloqueo@ucm.es";
    private static final String LUIS = "luis-interbloqueo@ucm.es";

    @Autowired private ApuestaService apuestas;
    @Autowired private ApuestaRepository apuestaRepository;
    @Autowired private UsuarioService usuarioService;
    @Autowired private UsuarioRepository usuarios;
    @Autowired private MovimientoSaldoRepository movimientos;
    @Autowired private EventoRepository eventos;
    @Autowired private CuotaHistoricaRepository historico;
    @Autowired private CompeticionRepository competiciones;
    @Autowired private EquipoRepository equipos;
    @MockitoSpyBean private BloqueoLigaService bloqueos;

    private final List<Evento> creados = new ArrayList<>();

    @AfterEach
    void limpiar() {
        for (String email : List.of(ANA, LUIS)) {
            usuarios.findByEmail(email).ifPresent(u -> {
                apuestaRepository.deleteAll(apuestaRepository.findByUsuarioAndEstadoInOrderByFechaDesc(u,
                        List.of(EstadoApuesta.values())));
                movimientos.deleteAll(movimientos.findByUsuarioOrderByIdAsc(u));
                usuarios.delete(u);
            });
        }
        for (Evento evento : creados) {
            historico.deleteAll(historico.findByEventoOrderByFechaAscIdAsc(evento));
            eventos.deleteById(evento.getId());
        }
    }

    private Evento partido(String local, String visitante, int dias) {
        Evento evento = eventos.save(new Evento(competiciones.findByNombre("LaLiga").orElseThrow(),
                equipos.findByNombre(local).orElseThrow(), equipos.findByNombre(visitante).orElseThrow(),
                Hora.ahora().plusDays(dias)));
        creados.add(evento);
        return evento;
    }

    @Test
    void dosCombinadasConLosPartidosEnOrdenDistintoNoSeEsperanParaSiempre() throws Exception {
        usuarioService.crear(ANA, "Ana", "secreta123", Rol.USUARIO);
        usuarioService.crear(LUIS, "Luis", "secreta123", Rol.USUARIO);
        Evento e1 = partido("Getafe CF", "Sevilla FC", 1);
        Evento e2 = partido("Elche CF", "Levante UD", 2);

        // Ana se para justo después de bloquear su primer partido (E2), hasta que Luis empiece
        CountDownLatch anaTieneSuPrimerPartido = new CountDownLatch(1);
        CountDownLatch seguir = new CountDownLatch(1);
        doAnswer(invocacion -> {
            if (Thread.currentThread().getName().equals("ana") && anaTieneSuPrimerPartido.getCount() == 1) {
                anaTieneSuPrimerPartido.countDown();
                seguir.await(5, TimeUnit.SECONDS);
            }
            return invocacion.callRealMethod();
        }).when(bloqueos).comprobar(any(), any(), any(), any());

        ExecutorService hiloAna = Executors.newSingleThreadExecutor(r -> new Thread(r, "ana"));
        ExecutorService hiloLuis = Executors.newSingleThreadExecutor(r -> new Thread(r, "luis"));
        Future<Object> deAna = hiloAna.submit(() -> intentar(() -> apuestas.apostar(ANA,
                List.of(new SeleccionPedida(e2.getId(), Resultado.VISITANTE, null),
                        new SeleccionPedida(e1.getId(), Resultado.VISITANTE, null)), BigDecimal.TEN)));
        assertThat(anaTieneSuPrimerPartido.await(10, TimeUnit.SECONDS)).isTrue();
        Future<Object> deLuis = hiloLuis.submit(() -> intentar(() -> apuestas.apostar(LUIS,
                List.of(new SeleccionPedida(e1.getId(), Resultado.EMPATE, null),
                        new SeleccionPedida(e2.getId(), Resultado.EMPATE, null)), BigDecimal.TEN)));
        Thread.sleep(700);
        seguir.countDown();

        assertThat(deAna.get(30, TimeUnit.SECONDS)).as("la combinada de Ana").isInstanceOf(Apuesta.class);
        assertThat(deLuis.get(30, TimeUnit.SECONDS)).as("la combinada de Luis").isInstanceOf(Apuesta.class);
        hiloAna.shutdown();
        hiloLuis.shutdown();
    }

    private static Object intentar(Supplier<Object> operacion) {
        try {
            return operacion.get();
        } catch (RuntimeException e) {
            return e;
        }
    }
}
