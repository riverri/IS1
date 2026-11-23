package es.ucm.fdi.is1.apuestas.prediccion;

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
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import es.ucm.fdi.is1.apuestas.Hora;
import es.ucm.fdi.is1.apuestas.usuarios.MovimientoSaldoRepository;
import es.ucm.fdi.is1.apuestas.usuarios.Rol;
import es.ucm.fdi.is1.apuestas.usuarios.UsuarioRepository;
import es.ucm.fdi.is1.apuestas.usuarios.UsuarioService;

/**
 * Novena depuración: un doble clic en la primera compra de un mercado creaba dos veces la posición y la segunda
 * petición chocaba con la clave única (error 500). Sin transacción, como en la web.
 */
@SpringBootTest
class CompraPrediccionConcurrenteTest {

    private static final String ANA = "ana-compra-concurrente@ucm.es";

    @Autowired private PrediccionService predicciones;
    @Autowired private MercadoPrediccionRepository mercados;
    @Autowired private PosicionPrediccionRepository posiciones;
    @Autowired private OperacionPrediccionRepository operaciones;
    @Autowired private UsuarioService usuarioService;
    @Autowired private UsuarioRepository usuarios;
    @Autowired private MovimientoSaldoRepository movimientos;

    private final List<MercadoPrediccion> creados = new ArrayList<>();

    @AfterEach
    void limpiar() {
        for (MercadoPrediccion mercado : creados) {
            operaciones.deleteAll(operaciones.findByMercadoOrderByFechaAscIdAsc(mercado));
            posiciones.deleteAll(posiciones.findByMercado(mercado));
            mercados.deleteById(mercado.getId());
        }
        usuarios.findByEmail(ANA).ifPresent(u -> {
            movimientos.deleteAll(movimientos.findByUsuarioOrderByIdAsc(u));
            usuarios.delete(u);
        });
    }

    @Test
    void unDobleClicEnLaPrimeraCompraNoDaError() throws Exception {
        usuarioService.crear(ANA, "Ana", "secreta123", Rol.USUARIO);
        for (int i = 0; i < 5; i++) {
            MercadoPrediccion mercado = predicciones.crear("¿Doble clic " + i + "?", null, Hora.ahora().plusDays(5),
                    new BigDecimal("100"), new BigDecimal("2"));
            creados.add(mercado);
            BigDecimal antes = usuarios.findByEmail(ANA).orElseThrow().getSaldo();

            ExecutorService hilos = Executors.newFixedThreadPool(2);
            CountDownLatch salida = new CountDownLatch(1);
            List<Future<Object>> compras = new ArrayList<>();
            for (int j = 0; j < 2; j++) {
                compras.add(hilos.submit(() -> {
                    salida.await();
                    try {
                        return predicciones.comprar(ANA, mercado.getId(), Lado.SI, BigDecimal.TEN, null);
                    } catch (RuntimeException e) {
                        return e;
                    }
                }));
            }
            salida.countDown();
            int hechas = 0;
            for (Future<Object> compra : compras) {
                Object resultado = compra.get();
                // Antes, a veces, DataIntegrityViolationException (error 500). Ahora la segunda espera a la primera: o se
                // hace, o choca con la versión del mercado, que la web convierte en un aviso (ConcurrenciaAdvice)
                assertThat(resultado).isInstanceOfAny(PosicionPrediccion.class,
                        ObjectOptimisticLockingFailureException.class);
                hechas += resultado instanceof PosicionPrediccion ? 1 : 0;
            }
            hilos.shutdown();
            assertThat(usuarios.findByEmail(ANA).orElseThrow().getSaldo())
                    .isEqualByComparingTo(antes.subtract(BigDecimal.TEN.multiply(BigDecimal.valueOf(hechas))));
            assertThat(posiciones.findByMercado(mercado)).hasSize(1);
        }
    }
}
