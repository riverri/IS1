package es.ucm.fdi.is1.apuestas.seguimiento;

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
import org.springframework.boot.test.context.SpringBootTest;

import es.ucm.fdi.is1.apuestas.Hora;
import es.ucm.fdi.is1.apuestas.cuotas.CalculadoraCuotas;
import es.ucm.fdi.is1.apuestas.cuotas.CuotaHistoricaRepository;
import es.ucm.fdi.is1.apuestas.cuotas.Resultado;
import es.ucm.fdi.is1.apuestas.equipos.CompeticionRepository;
import es.ucm.fdi.is1.apuestas.equipos.EquipoRepository;
import es.ucm.fdi.is1.apuestas.eventos.Evento;
import es.ucm.fdi.is1.apuestas.eventos.EventoRepository;
import es.ucm.fdi.is1.apuestas.usuarios.MovimientoSaldoRepository;
import es.ucm.fdi.is1.apuestas.usuarios.Rol;
import es.ucm.fdi.is1.apuestas.usuarios.UsuarioRepository;
import es.ucm.fdi.is1.apuestas.usuarios.UsuarioService;

/** CA-90.1: el límite de alertas por usuario y partido se mantiene con peticiones simultáneas. */
@SpringBootTest
class AlertasConcurrenteTest {

    private static final String EMAIL = "alertas-concurrente@ucm.es";

    @Autowired private AlertasService alertas;
    @Autowired private AlertaCuotaRepository alertasRepo;
    @Autowired private UsuarioService usuariosService;
    @Autowired private UsuarioRepository usuarios;
    @Autowired private MovimientoSaldoRepository movimientos;
    @Autowired private EventoRepository eventos;
    @Autowired private CuotaHistoricaRepository historico;
    @Autowired private CompeticionRepository competiciones;
    @Autowired private EquipoRepository equipos;
    @Autowired private CalculadoraCuotas calculadora;

    private Evento evento;

    @AfterEach
    void limpiar() {
        if (evento != null) {
            alertasRepo.deleteAll(alertasRepo.findByUsuarioAndEventoOrderByIdAsc(
                    usuarios.findByEmail(EMAIL).orElseThrow(), evento));
            historico.deleteAll(historico.findByEventoOrderByFechaAscIdAsc(evento));
            eventos.deleteById(evento.getId());
        }
        usuarios.findByEmail(EMAIL).ifPresent(u -> movimientos.deleteAll(movimientos.findByUsuarioOrderByIdAsc(u)));
        usuarios.findByEmail(EMAIL).ifPresent(usuarios::delete);
    }

    @Test
    void dosAltasSimultaneasNoSuperanElMaximoDeTresAlertas() throws Exception {
        usuariosService.crear(EMAIL, "Alertas", "secreta123", Rol.USUARIO);
        evento = eventos.save(new Evento(competiciones.findByNombre("LaLiga").orElseThrow(),
                equipos.findByNombre("Getafe CF").orElseThrow(), equipos.findByNombre("Sevilla FC").orElseThrow(),
                Hora.ahora().plusDays(1)));
        BigDecimal actual = calculadora.calcular(evento).local();
        alertas.crear(EMAIL, evento.getId(), Resultado.LOCAL, actual.add(new BigDecimal("0.50")));
        alertas.crear(EMAIL, evento.getId(), Resultado.LOCAL, actual.add(new BigDecimal("0.60")));

        ExecutorService hilos = Executors.newFixedThreadPool(2);
        CountDownLatch salida = new CountDownLatch(1);
        List<Future<Boolean>> envios = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            BigDecimal cuota = actual.add(new BigDecimal(i == 0 ? "0.70" : "0.80"));
            envios.add(hilos.submit(() -> {
                salida.await();
                try {
                    alertas.crear(EMAIL, evento.getId(), Resultado.LOCAL, cuota);
                    return true;
                } catch (IllegalArgumentException limiteAlcanzado) {
                    return false;
                }
            }));
        }
        salida.countDown();
        int creadas = 0;
        for (Future<Boolean> envio : envios) {
            creadas += envio.get() ? 1 : 0;
        }
        hilos.shutdownNow();

        assertThat(creadas).isEqualTo(1);
        assertThat(alertasRepo.findByUsuarioAndEventoOrderByIdAsc(
                usuarios.findByEmail(EMAIL).orElseThrow(), evento)).hasSize(AlertasService.MAXIMO_POR_EVENTO);
    }
}
