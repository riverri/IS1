package es.ucm.fdi.is1.apuestas.ligas;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import es.ucm.fdi.is1.apuestas.Hora;
import es.ucm.fdi.is1.apuestas.cuotas.CuotaHistoricaRepository;
import es.ucm.fdi.is1.apuestas.equipos.CompeticionRepository;
import es.ucm.fdi.is1.apuestas.equipos.EquipoRepository;
import es.ucm.fdi.is1.apuestas.eventos.Evento;
import es.ucm.fdi.is1.apuestas.eventos.EventoRepository;
import es.ucm.fdi.is1.apuestas.usuarios.MovimientoSaldoRepository;
import es.ucm.fdi.is1.apuestas.usuarios.Rol;
import es.ucm.fdi.is1.apuestas.usuarios.UsuarioRepository;
import es.ucm.fdi.is1.apuestas.usuarios.UsuarioService;

/**
 * #136: el mismo bloqueo enviado varias veces a la vez (doble clic, dos pestañas) se cobra una sola vez.
 * Sin @Transactional: cada envío va en su propia transacción, como en la aplicación real.
 */
@SpringBootTest
class BloqueoConcurrenteTest {

    private static final String ANA = "ana-bloqueo-concurrente@ucm.es";
    private static final String LUIS = "luis-bloqueo-concurrente@ucm.es";

    @Autowired private BloqueoLigaService bloqueos;
    @Autowired private BloqueoLigaRepository bloqueoRepository;
    @Autowired private LigaService ligas;
    @Autowired private LigaRepository ligaRepository;
    @Autowired private UsuarioService usuarioService;
    @Autowired private UsuarioRepository usuarios;
    @Autowired private MovimientoSaldoRepository movimientos;
    @Autowired private EventoRepository eventos;
    @Autowired private CompeticionRepository competiciones;
    @Autowired private EquipoRepository equipos;
    @Autowired private CuotaHistoricaRepository historico;

    private Evento evento;
    private Liga liga;

    @AfterEach
    void limpiar() {
        if (evento != null) {
            bloqueoRepository.deleteAll(bloqueoRepository.findByEvento(evento));
            historico.deleteAll(historico.findByEventoOrderByFechaAscIdAsc(evento));
            eventos.deleteById(evento.getId());
        }
        if (liga != null) {
            ligaRepository.deleteById(liga.getId());
        }
        for (String email : List.of(ANA, LUIS)) {
            usuarios.findByEmail(email).ifPresent(u -> movimientos.deleteAll(movimientos.findByUsuarioOrderByIdAsc(u)));
            usuarios.findByEmail(email).ifPresent(usuarios::delete);
        }
    }

    @Test
    void elMismoBloqueoALaVezSeCobraUnaSolaVez() throws Exception {
        usuarioService.crear(ANA, "Ana", "secreta123", Rol.USUARIO);
        usuarioService.crear(LUIS, "Luis", "secreta123", Rol.USUARIO);
        liga = ligas.crear(ANA, "Concurrencia");
        ligas.unirse(LUIS, liga.getCodigo());
        evento = eventos.save(new Evento(competiciones.findByNombre("LaLiga").orElseThrow(),
                equipos.findByNombre("Getafe CF").orElseThrow(), equipos.findByNombre("Sevilla FC").orElseThrow(),
                Hora.ahora().plusDays(1)));
        BigDecimal saldoInicial = usuarios.findByEmail(ANA).orElseThrow().getSaldo();
        Long luis = usuarios.findByEmail(LUIS).orElseThrow().getId();

        ExecutorService hilos = Executors.newFixedThreadPool(3);
        CountDownLatch salida = new CountDownLatch(1);
        List<Future<Boolean>> envios = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            envios.add(hilos.submit(() -> {
                salida.await();
                try {
                    bloqueos.crear(liga.getId(), ANA, luis, evento.getId(), "LOCAL");
                    return true;
                } catch (IllegalArgumentException e) {
                    // «Ese bloqueo ya está activo»: con el evento bloqueado en exclusiva, los demás esperan y lo ven.
                    // Antes chocaban con la clave única (DataIntegrityViolationException) y la página daba error 500
                    return false;
                }
            }));
        }
        salida.countDown();
        int hechos = 0;
        for (Future<Boolean> envio : envios) {
            hechos += envio.get() ? 1 : 0;
        }
        hilos.shutdown();

        assertThat(hechos).isEqualTo(1);
        assertThat(bloqueoRepository.findByEvento(evento)).hasSize(1);
        assertThat(usuarios.findByEmail(ANA).orElseThrow().getSaldo())
                .isEqualByComparingTo(saldoInicial.subtract(BloqueoLigaService.COSTE));
    }
}
