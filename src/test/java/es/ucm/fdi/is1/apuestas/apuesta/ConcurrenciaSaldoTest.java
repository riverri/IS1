package es.ucm.fdi.is1.apuestas.apuesta;

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
import es.ucm.fdi.is1.apuestas.cuotas.Resultado;
import es.ucm.fdi.is1.apuestas.equipos.CompeticionRepository;
import es.ucm.fdi.is1.apuestas.equipos.EquipoRepository;
import es.ucm.fdi.is1.apuestas.eventos.Evento;
import es.ucm.fdi.is1.apuestas.eventos.EventoRepository;
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
    private EventoRepository eventos;

    @Autowired
    private CompeticionRepository competiciones;

    @Autowired
    private EquipoRepository equipos;

    private Evento evento;

    @AfterEach
    void limpiar() {
        apuestaRepository.findAll().stream().filter(a -> a.getUsuario().getEmail().equals(EMAIL))
                .forEach(apuestaRepository::delete);
        if (evento != null) {
            eventos.deleteById(evento.getId());
        }
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
}
