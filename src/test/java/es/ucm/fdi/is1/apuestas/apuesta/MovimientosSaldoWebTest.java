package es.ucm.fdi.is1.apuestas.apuesta;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
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
import es.ucm.fdi.is1.apuestas.usuarios.MovimientoSaldo;
import es.ucm.fdi.is1.apuestas.usuarios.MovimientoSaldoRepository;
import es.ucm.fdi.is1.apuestas.usuarios.Rol;
import es.ucm.fdi.is1.apuestas.usuarios.SaldoInsuficienteException;
import es.ucm.fdi.is1.apuestas.usuarios.TipoMovimiento;
import es.ucm.fdi.is1.apuestas.usuarios.Usuario;
import es.ucm.fdi.is1.apuestas.usuarios.UsuarioRepository;
import es.ucm.fdi.is1.apuestas.usuarios.UsuarioService;

/** HU-55: libro de movimientos del saldo, con apuestas, pagos, cancelaciones, correcciones y baja reales. */
@SpringBootTest
@Transactional
class MovimientosSaldoWebTest {

    private static final String ANA = "ana@ucm.es";

    @Autowired
    private ApuestaService apuestas;

    @Autowired
    private ResolucionService resolucion;

    @Autowired
    private BajaService bajas;

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

    private Evento partido;

    @BeforeEach
    void preparar() {
        usuarioService.crear(ANA, "Ana", "secreta123", Rol.USUARIO);
        partido = eventos.save(new Evento(competiciones.findByNombre("LaLiga").orElseThrow(),
                equipos.findByNombre("Getafe CF").orElseThrow(),
                equipos.findByNombre("Sevilla FC").orElseThrow(), Hora.ahora().plusDays(1)));
    }

    private Usuario ana() {
        return usuarios.findByEmail(ANA).orElseThrow();
    }

    private List<MovimientoSaldo> deAna() {
        return movimientos.findByUsuarioOrderByIdAsc(ana());
    }

    /** CA-55.2: el último saldo resultante y la suma de todos los importes coinciden con el saldo. */
    private void cuadra() {
        List<MovimientoSaldo> lista = deAna();
        BigDecimal saldo = ana().getSaldo();
        assertThat(lista.getLast().getSaldoResultante()).isEqualByComparingTo(saldo);
        assertThat(lista.stream().map(MovimientoSaldo::getImporte).reduce(BigDecimal.ZERO, BigDecimal::add))
                .isEqualByComparingTo(saldo);
    }

    @Test
    void apostarYCobrarDejanSusMovimientos() {
        Apuesta apuesta = apuestas.apostar(ANA, partido.getId(), Resultado.LOCAL, new BigDecimal("10"));
        resolucion.introducirResultado(partido.getId(), Resultado.LOCAL);

        // CA-55.1: bienvenida, cargo de la apuesta y premio, con su concepto
        List<MovimientoSaldo> lista = deAna();
        assertThat(lista).extracting(MovimientoSaldo::getTipo)
                .containsExactly(TipoMovimiento.BIENVENIDA, TipoMovimiento.APUESTA, TipoMovimiento.PREMIO);
        assertThat(lista.get(1).getImporte()).isEqualByComparingTo("-10");
        assertThat(lista.get(1).getConcepto()).isEqualTo("Nueva apuesta a Getafe CF (Getafe CF – Sevilla FC)");
        assertThat(lista.get(2).getImporte()).isEqualByComparingTo(apuesta.getGananciaPotencial());
        cuadra();
    }

    @Test
    void cancelarYCorregirUnResultadoTambienSeApuntan() {
        Apuesta cancelada = apuestas.apostar(ANA, partido.getId(), Resultado.EMPATE, new BigDecimal("5"));
        apuestas.cancelar(ANA, cancelada.getId());
        Apuesta apuesta = apuestas.apostar(ANA, partido.getId(), Resultado.LOCAL, new BigDecimal("10"));
        resolucion.introducirResultado(partido.getId(), Resultado.LOCAL);
        // El creador se equivocó: ganó el visitante, así que se retira el premio
        resolucion.introducirResultado(partido.getId(), Resultado.VISITANTE);

        List<MovimientoSaldo> lista = deAna();
        assertThat(lista).extracting(MovimientoSaldo::getTipo).containsExactly(TipoMovimiento.BIENVENIDA,
                TipoMovimiento.APUESTA, TipoMovimiento.DEVOLUCION, TipoMovimiento.APUESTA, TipoMovimiento.PREMIO,
                TipoMovimiento.CORRECCION);
        assertThat(lista.get(2).getImporte()).isEqualByComparingTo("5");
        assertThat(lista.getLast().getImporte()).isEqualByComparingTo(apuesta.getGananciaPotencial().negate());
        cuadra();
    }

    @Test
    void unaApuestaQueFallaNoDejaMovimiento() {
        // Ana se queda con 5 monedas y quiere apostar 10
        ana().cargar(ana().getSaldo().subtract(new BigDecimal("5")), TipoMovimiento.APUESTA, "Gasto de prueba",
                Hora.ahora());
        int antes = deAna().size();

        // CA-55.3
        assertThatThrownBy(() -> apuestas.apostar(ANA, partido.getId(), Resultado.LOCAL, new BigDecimal("10")))
                .isInstanceOf(SaldoInsuficienteException.class);

        assertThat(deAna()).hasSize(antes);
        assertThat(ana().getSaldo()).isEqualByComparingTo("5");
    }

    @Test
    void laBajaDejaElSaldoACeroEnElLibro() {
        apuestas.apostar(ANA, partido.getId(), Resultado.LOCAL, new BigDecimal("10"));

        bajas.eliminarCuenta(ANA, "secreta123");

        Usuario eliminada = usuarios.findAll().stream().filter(Usuario::isEliminado).findFirst().orElseThrow();
        List<MovimientoSaldo> lista = movimientos.findByUsuarioOrderByIdAsc(eliminada);
        // La apuesta se canceló (se devolvió) y después el saldo pasó a cero
        assertThat(lista).extracting(MovimientoSaldo::getTipo).endsWith(TipoMovimiento.DEVOLUCION, TipoMovimiento.BAJA);
        assertThat(lista.getLast().getSaldoResultante()).isEqualByComparingTo("0");
    }
}
