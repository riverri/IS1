package es.ucm.fdi.is1.apuestas.apuesta;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import es.ucm.fdi.is1.apuestas.Hora;
import es.ucm.fdi.is1.apuestas.cuotas.Resultado;
import es.ucm.fdi.is1.apuestas.equipos.CompeticionRepository;
import es.ucm.fdi.is1.apuestas.equipos.EquipoRepository;
import es.ucm.fdi.is1.apuestas.eventos.Evento;
import es.ucm.fdi.is1.apuestas.eventos.EventoRepository;
import es.ucm.fdi.is1.apuestas.usuarios.MovimientoSaldoRepository;
import es.ucm.fdi.is1.apuestas.usuarios.Rol;
import es.ucm.fdi.is1.apuestas.usuarios.UsuarioRepository;
import es.ucm.fdi.is1.apuestas.usuarios.UsuarioService;

/**
 * F-02: dos apuestas a la vez del mismo usuario (doble clic, dos pestañas) no pueden dejar el saldo mal.
 * Sin @Transactional: cada apuesta va en su propia transacción, como en la aplicación real.
 */
@SpringBootTest
class ConcurrenciaSaldoTest {

    private static final String EMAIL = "concurrencia@ucm.es";

    @Autowired
    private ApuestaService apuestas;

    @Autowired
    private ApuestaRepository apuestaRepository;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private UsuarioRepository usuarios;

    @Autowired
    private MovimientoSaldoRepository movimientos;

    @Autowired
    private EventoRepository eventos;

    @Autowired
    private CompeticionRepository competiciones;

    @Autowired
    private EquipoRepository equipos;

    @Autowired
    private es.ucm.fdi.is1.apuestas.cuotas.CuotaHistoricaRepository historico;

    @Autowired
    private ResolucionService resolucion;

    @Autowired
    private org.springframework.transaction.PlatformTransactionManager transacciones;

    private Evento evento;
    private Evento otro;

    @AfterEach
    void limpiar() {
        apuestaRepository.findAll().stream().filter(a -> a.getUsuario().getEmail().equals(EMAIL))
                .forEach(apuestaRepository::delete);
        if (otro != null) {
            historico.deleteAll(historico.findByEventoOrderByFechaAscIdAsc(otro));
            eventos.deleteById(otro.getId());
        }
        if (evento != null) {
            // Primero su historial de cuotas (HU-91), que apunta al evento
            historico.deleteAll(historico.findByEventoOrderByFechaAscIdAsc(evento));
            eventos.deleteById(evento.getId());
        }
        // Primero sus movimientos de saldo (HU-55), que apuntan al usuario
        usuarios.findByEmail(EMAIL).ifPresent(u -> movimientos.deleteAll(movimientos.findByUsuarioOrderByIdAsc(u)));
        usuarios.findByEmail(EMAIL).ifPresent(usuarios::delete);
    }

    @Test
    void dosApuestasALaVezNoGastanMasDeLoQueHay() throws Exception {
        usuarioService.crear(EMAIL, "Concurrencia", "secreta123", Rol.USUARIO);
        evento = eventos.save(new Evento(competiciones.findByNombre("LaLiga").orElseThrow(),
                equipos.findByNombre("Getafe CF").orElseThrow(), equipos.findByNombre("Sevilla FC").orElseThrow(),
                Hora.ahora().plusDays(1)));
        BigDecimal saldoInicial = usuarios.findByEmail(EMAIL).orElseThrow().getSaldo();
        BigDecimal importe = saldoInicial.multiply(new BigDecimal("0.6")).setScale(0, java.math.RoundingMode.DOWN)
                .min(new BigDecimal("500"));

        ExecutorService hilos = Executors.newFixedThreadPool(2);
        CountDownLatch salida = new CountDownLatch(1);
        List<Future<Boolean>> resultados = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            resultados.add(hilos.submit(() -> {
                salida.await();
                try {
                    apuestas.apostar(EMAIL, evento.getId(), Resultado.LOCAL, importe);
                    return true;
                } catch (RuntimeException e) {
                    return false; // saldo insuficiente o conflicto de concurrencia: correcto, no se apuesta
                }
            }));
        }
        salida.countDown();
        int hechas = 0;
        for (Future<Boolean> r : resultados) {
            hechas += r.get() ? 1 : 0;
        }
        hilos.shutdown();

        BigDecimal saldoFinal = usuarios.findByEmail(EMAIL).orElseThrow().getSaldo();
        long guardadas = apuestaRepository.findAll().stream().filter(a -> a.getUsuario().getEmail().equals(EMAIL)).count();
        assertThat(saldoFinal.signum()).isGreaterThanOrEqualTo(0);
        assertThat(guardadas).isEqualTo(hechas);
        assertThat(saldoFinal).isEqualByComparingTo(saldoInicial.subtract(importe.multiply(BigDecimal.valueOf(hechas))));
    }

    /**
     * Cancelar y cambiar el importe a la vez: si el cambio (que con el mismo importe no toca el saldo) se guardara
     * después de la cancelación, la apuesta volvería a estar activa con el dinero ya devuelto. Con el bloqueo
     * optimista de la apuesta, guardar una copia que se leyó antes de cancelarla falla.
     */
    @Test
    void unaApuestaLeidaAntesDeCancelarlaNoSeGuardaEncima() {
        usuarioService.crear(EMAIL, "Concurrencia", "secreta123", Rol.USUARIO);
        evento = eventos.save(new Evento(competiciones.findByNombre("LaLiga").orElseThrow(),
                equipos.findByNombre("Getafe CF").orElseThrow(), equipos.findByNombre("Sevilla FC").orElseThrow(),
                Hora.ahora().plusDays(1)));
        Apuesta apuesta = apuestas.apostar(EMAIL, evento.getId(), Resultado.LOCAL, BigDecimal.TEN);
        Apuesta leidaAntes = apuestaRepository.findById(apuesta.getId()).orElseThrow();

        apuestas.cancelar(EMAIL, apuesta.getId());

        assertThatThrownBy(() -> apuestaRepository.save(leidaAntes))
                .isInstanceOf(ObjectOptimisticLockingFailureException.class);
        assertThat(apuestaRepository.findById(apuesta.getId()).orElseThrow().getEstado())
                .isEqualTo(EstadoApuesta.CANCELADA);
    }

    /**
     * Apostar mientras el creador anula el partido: la apuesta espera a que termine la anulación y entonces ve que el
     * partido ya no admite apuestas. Antes se guardaba, nadie la devolvía y se quedaba activa con el dinero retenido.
     */
    @Test
    void unaApuestaQueLlegaMientrasSeAnulaElPartidoNoSeGuarda() throws Exception {
        usuarioService.crear(EMAIL, "Concurrencia", "secreta123", Rol.USUARIO);
        evento = eventos.save(new Evento(competiciones.findByNombre("LaLiga").orElseThrow(),
                equipos.findByNombre("Getafe CF").orElseThrow(), equipos.findByNombre("Sevilla FC").orElseThrow(),
                Hora.ahora().plusDays(1)));
        BigDecimal saldoInicial = usuarios.findByEmail(EMAIL).orElseThrow().getSaldo();

        ExecutorService hilo = Executors.newSingleThreadExecutor();
        CountDownLatch anulado = new CountDownLatch(1);
        // La anulación se hace pero tarda en confirmarse (medio segundo con el evento bloqueado)
        Future<?> anulacion = hilo.submit(() -> new org.springframework.transaction.support.TransactionTemplate(transacciones)
                .executeWithoutResult(estado -> {
                    resolucion.anular(evento.getId());
                    anulado.countDown();
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }));
        anulado.await();

        org.assertj.core.api.Assertions.assertThatThrownBy(
                        () -> apuestas.apostar(EMAIL, evento.getId(), Resultado.LOCAL, BigDecimal.TEN))
                .isInstanceOf(es.ucm.fdi.is1.apuestas.eventos.EventoNoDisponibleException.class);
        anulacion.get();
        hilo.shutdown();

        assertThat(apuestaRepository.findAll()).noneMatch(a -> a.getUsuario().getEmail().equals(EMAIL));
        assertThat(usuarios.findByEmail(EMAIL).orElseThrow().getSaldo()).isEqualByComparingTo(saldoInicial);
    }

    /**
     * Cambiar el importe de una combinada reescribe la cuota de sus selecciones. Si a la vez se anula uno de sus
     * partidos, guardar la combinada leída antes ya no vuelve a dejar pendiente la selección anulada.
     */
    @Test
    void unaCombinadaLeidaAntesDeAnularUnoDeSusPartidosNoSeGuardaEncima() {
        usuarioService.crear(EMAIL, "Concurrencia", "secreta123", Rol.USUARIO);
        evento = eventos.save(new Evento(competiciones.findByNombre("LaLiga").orElseThrow(),
                equipos.findByNombre("Getafe CF").orElseThrow(), equipos.findByNombre("Sevilla FC").orElseThrow(),
                Hora.ahora().plusDays(1)));
        otro = eventos.save(new Evento(competiciones.findByNombre("LaLiga").orElseThrow(),
                equipos.findByNombre("Elche CF").orElseThrow(), equipos.findByNombre("Levante UD").orElseThrow(),
                Hora.ahora().plusDays(2)));
        Apuesta combinada = apuestas.apostar(EMAIL, List.of(new SeleccionPedida(evento.getId(), Resultado.LOCAL, null),
                new SeleccionPedida(otro.getId(), Resultado.LOCAL, null)), BigDecimal.TEN);
        Apuesta leidaAntes = apuestaRepository.conSelecciones(combinada.getId()).orElseThrow();

        resolucion.anular(otro.getId());
        leidaAntes.getSelecciones().forEach(sel -> sel.actualizarCuota(new BigDecimal("1.50")));

        assertThatThrownBy(() -> apuestaRepository.save(leidaAntes))
                .isInstanceOf(ObjectOptimisticLockingFailureException.class);
    }
}
