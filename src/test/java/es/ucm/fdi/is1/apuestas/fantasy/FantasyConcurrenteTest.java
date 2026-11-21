package es.ucm.fdi.is1.apuestas.fantasy;

import static org.assertj.core.api.Assertions.assertThat;

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

import es.ucm.fdi.is1.apuestas.equipos.EquipoRepository;
import es.ucm.fdi.is1.apuestas.equipos.Jugador;
import es.ucm.fdi.is1.apuestas.equipos.JugadorRepository;
import es.ucm.fdi.is1.apuestas.equipos.Posicion;
import es.ucm.fdi.is1.apuestas.usuarios.MovimientoSaldoRepository;
import es.ucm.fdi.is1.apuestas.usuarios.Rol;
import es.ucm.fdi.is1.apuestas.usuarios.UsuarioRepository;
import es.ucm.fdi.is1.apuestas.usuarios.UsuarioService;

/**
 * Fichar al mismo jugador varias veces a la vez (doble clic, dos pestañas) solo lo ficha una vez. Sin @Transactional:
 * cada envío va en su propia transacción, como en la aplicación real.
 */
@SpringBootTest
class FantasyConcurrenteTest {

    private static final String EMAIL = "fantasy-concurrente@ucm.es";

    @Autowired private FantasyService fantasy;
    @Autowired private EquipoFantasyRepository equiposFantasy;
    @Autowired private UsuarioService usuarioService;
    @Autowired private UsuarioRepository usuarios;
    @Autowired private MovimientoSaldoRepository movimientos;
    @Autowired private JugadorRepository jugadores;
    @Autowired private EquipoRepository equipos;

    private Jugador portero;

    @AfterEach
    void limpiar() {
        usuarios.findByEmail(EMAIL).flatMap(equiposFantasy::findByUsuario).ifPresent(equiposFantasy::delete);
        if (portero != null) {
            jugadores.deleteById(portero.getId());
        }
        usuarios.findByEmail(EMAIL).ifPresent(u -> movimientos.deleteAll(movimientos.findByUsuarioOrderByIdAsc(u)));
        usuarios.findByEmail(EMAIL).ifPresent(usuarios::delete);
    }

    @Test
    void elMismoJugadorALaVezSeFichaUnaSolaVez() throws Exception {
        usuarioService.crear(EMAIL, "Concurrente", "secreta123", Rol.USUARIO);
        portero = jugadores.save(new Jugador(equipos.findByNombre("Getafe CF").orElseThrow(), "Portero concurrente",
                Posicion.PORTERO));
        fantasy.crear(EMAIL, "Concurrentes FC");

        ExecutorService hilos = Executors.newFixedThreadPool(3);
        CountDownLatch salida = new CountDownLatch(1);
        List<Future<Boolean>> envios = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            envios.add(hilos.submit(() -> {
                salida.await();
                try {
                    fantasy.fichar(EMAIL, portero.getId());
                    return true;
                } catch (RuntimeException e) {
                    return false; // «Ya está en tu equipo»: correcto
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
        assertThat(fantasy.equipoDe(EMAIL).orElseThrow().getPlantilla()).hasSize(1);
    }
}
