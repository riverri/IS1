package es.ucm.fdi.is1.apuestas.ligas;

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

import es.ucm.fdi.is1.apuestas.usuarios.MovimientoSaldoRepository;
import es.ucm.fdi.is1.apuestas.usuarios.Rol;
import es.ucm.fdi.is1.apuestas.usuarios.UsuarioRepository;
import es.ucm.fdi.is1.apuestas.usuarios.UsuarioService;

/** CA-51.2: dos uniones simultáneas no pueden sobrepasar el máximo de ligas por usuario. */
@SpringBootTest
class LigasConcurrenteTest {

    private static final String ANA = "ana-ligas-concurrente@ucm.es";
    private static final String LUIS = "luis-ligas-concurrente@ucm.es";
    private static final String EVA = "eva-ligas-concurrente@ucm.es";

    @Autowired private LigaService ligas;
    @Autowired private LigaRepository ligaRepository;
    @Autowired private UsuarioService usuariosService;
    @Autowired private UsuarioRepository usuarios;
    @Autowired private MovimientoSaldoRepository movimientos;

    private final List<Long> idsLigas = new ArrayList<>();

    @AfterEach
    void limpiar() {
        ligaRepository.findAll().stream().filter(l -> idsLigas.contains(l.getId())).forEach(ligaRepository::delete);
        for (String email : List.of(ANA, LUIS, EVA)) {
            usuarios.findByEmail(email).ifPresent(u -> movimientos.deleteAll(movimientos.findByUsuarioOrderByIdAsc(u)));
            usuarios.findByEmail(email).ifPresent(usuarios::delete);
        }
        idsLigas.clear();
    }

    @Test
    void dosUnionesALaVezRespetanElMaximoDeLigas() throws Exception {
        usuariosService.crear(ANA, "Ana", "secreta123", Rol.USUARIO);
        usuariosService.crear(LUIS, "Luis", "secreta123", Rol.USUARIO);
        usuariosService.crear(EVA, "Eva", "secreta123", Rol.USUARIO);
        for (int i = 0; i < LigaService.MAX_LIGAS - 1; i++) {
            idsLigas.add(ligas.crear(ANA, "Liga de Ana " + i).getId());
        }
        Liga deLuis = ligas.crear(LUIS, "Liga de Luis");
        Liga deEva = ligas.crear(EVA, "Liga de Eva");
        idsLigas.add(deLuis.getId());
        idsLigas.add(deEva.getId());

        ExecutorService hilos = Executors.newFixedThreadPool(2);
        CountDownLatch salida = new CountDownLatch(1);
        List<Future<Boolean>> envios = List.of(
                hilos.submit(() -> unirse(salida, deLuis.getCodigo())),
                hilos.submit(() -> unirse(salida, deEva.getCodigo())));
        salida.countDown();
        int unidos = 0;
        for (Future<Boolean> envio : envios) {
            unidos += envio.get() ? 1 : 0;
        }
        hilos.shutdownNow();

        assertThat(unidos).isEqualTo(1);
        assertThat(ligas.mias(ANA)).hasSize(LigaService.MAX_LIGAS);
    }

    private boolean unirse(CountDownLatch salida, String codigo) throws InterruptedException {
        salida.await();
        try {
            ligas.unirse(ANA, codigo);
            return true;
        } catch (IllegalArgumentException limiteAlcanzado) {
            return false;
        }
    }
}
