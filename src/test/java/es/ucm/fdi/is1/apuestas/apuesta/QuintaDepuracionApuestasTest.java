package es.ucm.fdi.is1.apuestas.apuesta;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import es.ucm.fdi.is1.apuestas.Hora;
import es.ucm.fdi.is1.apuestas.cuotas.Resultado;
import es.ucm.fdi.is1.apuestas.equipos.CompeticionRepository;
import es.ucm.fdi.is1.apuestas.equipos.EquipoRepository;
import es.ucm.fdi.is1.apuestas.eventos.Evento;
import es.ucm.fdi.is1.apuestas.eventos.EventoRepository;
import es.ucm.fdi.is1.apuestas.ligas.BloqueoLigaService;
import es.ucm.fdi.is1.apuestas.ligas.Liga;
import es.ucm.fdi.is1.apuestas.ligas.LigaService;
import es.ucm.fdi.is1.apuestas.usuarios.MovimientoSaldoRepository;
import es.ucm.fdi.is1.apuestas.usuarios.Rol;
import es.ucm.fdi.is1.apuestas.usuarios.TipoMovimiento;
import es.ucm.fdi.is1.apuestas.usuarios.UsuarioRepository;
import es.ucm.fdi.is1.apuestas.usuarios.UsuarioService;
import jakarta.persistence.EntityManager;

/** Quinta depuración: cobro anticipado con supercuota, límites y bloqueos al subir el importe. */
@SpringBootTest
@Transactional
class QuintaDepuracionApuestasTest {

    private static final String ANA = "ana@ucm.es";
    private static final String LUIS = "luis@ucm.es";

    @Autowired private UsuarioService usuarioService;
    @Autowired private UsuarioRepository usuarios;
    @Autowired private MovimientoSaldoRepository movimientos;
    @Autowired private CompeticionRepository competiciones;
    @Autowired private EquipoRepository equipos;
    @Autowired private EventoRepository eventos;
    @Autowired private ApuestaService apuestas;
    @Autowired private SupercuotaService supercuotas;
    @Autowired private LigaService ligas;
    @Autowired private BloqueoLigaService bloqueos;
    @Autowired private EntityManager em;

    private Evento futbol;
    private Evento otro;

    @BeforeEach
    void preparar() {
        usuarioService.crear(ANA, "Ana", "secreta123", Rol.USUARIO);
        usuarioService.crear(LUIS, "Luis", "secreta123", Rol.USUARIO);
        LocalDateTime manana = Hora.ahora().plusDays(1);
        futbol = eventos.save(new Evento(competiciones.findByNombre("LaLiga").orElseThrow(),
                equipos.findByNombre("Getafe CF").orElseThrow(), equipos.findByNombre("Sevilla FC").orElseThrow(),
                manana));
        otro = eventos.save(new Evento(competiciones.findByNombre("LaLiga").orElseThrow(),
                equipos.findByNombre("Real Madrid").orElseThrow(), equipos.findByNombre("Getafe CF").orElseThrow(),
                manana.plusDays(2)));
    }

    @Test
    void unaApuestaQuePasaALaSupercuotaAlCambiarElImporteNoSePuedeCobrarAntes() throws Exception {
        Apuesta simple = apuestas.apostar(ANA, futbol.getId(), Resultado.LOCAL, new BigDecimal("50"));
        Thread.sleep(5);
        BigDecimal mejorada = simple.getCuota().multiply(new BigDecimal("1.9")).setScale(2, RoundingMode.DOWN);
        supercuotas.crear(futbol.getId(), Resultado.LOCAL, mejorada, new BigDecimal("50"), 24);
        // Con el mismo importe no cuesta nada, y la apuesta pasa a la supercuota (HU-87)
        apuestas.modificarImporte(ANA, simple.getId(), new BigDecimal("50"));
        assertThat(simple.getCuota()).isEqualByComparingTo(mejorada);

        // Antes, el cobro anticipado pagaba enseguida casi el doble de lo apostado
        assertThat(apuestas.ofertasCashOut(List.of(simple), Hora.ahora())).doesNotContainKey(simple.getId());
    }

    @Test
    void subirElImporteDeUnaApuestaAntiguaCuentaEnElLimiteDeHoy() {
        Apuesta antigua = apuestas.apostar(ANA, otro.getId(), Resultado.LOCAL, new BigDecimal("10"));
        em.flush();
        em.createQuery("update Apuesta a set a.fecha = :fecha where a.id = :id")
                .setParameter("fecha", Hora.ahora().minusDays(2)).setParameter("id", antigua.getId()).executeUpdate();
        em.clear();
        usuarioService.fijarLimites(ANA, new BigDecimal("100"), null);
        apuestas.modificarImporte(ANA, antigua.getId(), new BigDecimal("110"));
        assertThat(movimientos.findTop100ByUsuarioOrderByIdDesc(usuarios.findByEmail(ANA).orElseThrow()).getFirst()
                .getTipo()).isEqualTo(TipoMovimiento.SUBIDA_APUESTA);

        // Hoy ya ha jugado 100 (CA-10.1)
        assertThatThrownBy(() -> apuestas.apostar(ANA, futbol.getId(), Resultado.LOCAL, new BigDecimal("100")))
                .isInstanceOf(JuegoResponsableException.class);
    }

    @Test
    void unaApuestaDeHoySubidaYBajadaCuentaLoQueQueda() {
        usuarioService.fijarLimites(ANA, new BigDecimal("100"), null);
        Apuesta hoy = apuestas.apostar(ANA, otro.getId(), Resultado.LOCAL, new BigDecimal("40"));
        apuestas.modificarImporte(ANA, hoy.getId(), new BigDecimal("20"));
        apuestas.modificarImporte(ANA, hoy.getId(), new BigDecimal("50"));
        // Cuenta 20 de la apuesta y 30 de la subida: le quedan 50, no 10 ni 30
        apuestas.apostar(ANA, futbol.getId(), Resultado.LOCAL, new BigDecimal("50"));
        assertThatThrownBy(() -> apuestas.apostar(ANA, futbol.getId(), Resultado.VISITANTE, new BigDecimal("1")))
                .isInstanceOf(JuegoResponsableException.class);
    }

    @Test
    void conUnBloqueoDeLigaNoSePuedeSubirLaApuestaQueYaHabia() {
        Liga liga = ligas.crear(ANA, "Amigos");
        ligas.unirse(LUIS, liga.getCodigo());
        Apuesta deLuis = apuestas.apostar(LUIS, futbol.getId(), Resultado.LOCAL, new BigDecimal("10"));
        bloqueos.crear(liga.getId(), ANA, usuarios.findByEmail(LUIS).orElseThrow().getId(), futbol.getId(), "LOCAL");

        assertThatThrownBy(() -> apuestas.modificarImporte(LUIS, deLuis.getId(), new BigDecimal("200")))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("bloqueada");
        // Bajarla sí (CA-105.3: la que ya tenía se queda)
        apuestas.modificarImporte(LUIS, deLuis.getId(), new BigDecimal("5"));
        assertThat(deLuis.getImporte()).isEqualByComparingTo("5");
    }
}
