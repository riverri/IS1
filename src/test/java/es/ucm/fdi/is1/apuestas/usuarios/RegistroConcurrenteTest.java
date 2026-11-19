package es.ucm.fdi.is1.apuestas.usuarios;

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

/**
 * Registrarse dos veces a la vez con el mismo email (doble clic en «Crear cuenta»): se crea una sola cuenta y los
 * demás intentos dicen que el email ya está registrado, en lugar de dar un error 500.
 * Sin @Transactional: cada registro va en su propia transacción, como en la aplicación real.
 */
@SpringBootTest
class RegistroConcurrenteTest {

    private static final String EMAIL = "doble-clic@ucm.es";

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private UsuarioRepository usuarios;

    @Autowired
    private MovimientoSaldoRepository movimientos;

    @AfterEach
    void limpiar() {
        usuarios.findByEmail(EMAIL).ifPresent(u -> movimientos.deleteAll(movimientos.findByUsuarioOrderByIdAsc(u)));
        usuarios.findByEmail(EMAIL).ifPresent(usuarios::delete);
    }

    @Test
    void dosRegistrosALaVezCreanUnaSolaCuenta() throws Exception {
        ExecutorService hilos = Executors.newFixedThreadPool(4);
        CountDownLatch salida = new CountDownLatch(1);
        List<Future<String>> resultados = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            resultados.add(hilos.submit(() -> {
                salida.await();
                try {
                    usuarioService.crear(EMAIL, "Doble clic", "secreta123", Rol.USUARIO);
                    return "creada";
                } catch (EmailYaRegistradoException e) {
                    return "ya registrado";
                } catch (RuntimeException e) {
                    return e.getClass().getSimpleName();
                }
            }));
        }
        salida.countDown();
        List<String> obtenidos = new ArrayList<>();
        for (Future<String> r : resultados) {
            obtenidos.add(r.get());
        }
        hilos.shutdown();

        assertThat(obtenidos).containsOnly("creada", "ya registrado");
        assertThat(obtenidos).filteredOn("creada"::equals).hasSize(1);
        assertThat(usuarios.findByEmail(EMAIL)).isPresent();
    }
}
