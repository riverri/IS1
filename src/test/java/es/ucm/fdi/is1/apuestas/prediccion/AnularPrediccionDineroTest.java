package es.ucm.fdi.is1.apuestas.prediccion;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import es.ucm.fdi.is1.apuestas.Hora;
import es.ucm.fdi.is1.apuestas.apuesta.BajaService;
import es.ucm.fdi.is1.apuestas.usuarios.Rol;
import es.ucm.fdi.is1.apuestas.usuarios.UsuarioRepository;
import es.ucm.fdi.is1.apuestas.usuarios.UsuarioService;

/** Novena depuración: dinero al anular un mercado de predicción. */
@SpringBootTest
@Transactional
class AnularPrediccionDineroTest {

    @Autowired PrediccionService predicciones;
    @Autowired PosicionPrediccionRepository posiciones;
    @Autowired UsuarioService usuarioService;
    @Autowired UsuarioRepository usuarios;
    @Autowired BajaService bajas;

    private BigDecimal saldo(String email) {
        return usuarios.findByEmail(email).orElseThrow().getSaldo();
    }

    /**
     * Comprar el «No», comprar mucho «Sí», vender el «No» (con pérdida) y vender el «Sí» (con ganancia): el mercado
     * vuelve a como estaba y Ana ha perdido solo la comisión. Al anular se le devuelve la pérdida del «No» (posición a
     * posición) y se quedaba la ganancia del «Sí»: salía ganando de un mercado anulado.
     */
    @Test
    void anularNoDevuelveMasDeLoQueSePerdioEnElMercado() {
        usuarioService.crear("ana-r9@ucm.es", "Ana", "secreta123", Rol.USUARIO);
        MercadoPrediccion m = predicciones.crear("¿R9?", null, Hora.ahora().plusDays(5), new BigDecimal("100"),
                new BigDecimal("1"));
        BigDecimal antes = saldo("ana-r9@ucm.es");
        PosicionPrediccion no = predicciones.comprar("ana-r9@ucm.es", m.getId(), Lado.NO, new BigDecimal("100"), null);
        PosicionPrediccion si = predicciones.comprar("ana-r9@ucm.es", m.getId(), Lado.SI, new BigDecimal("400"), null);
        predicciones.vender("ana-r9@ucm.es", m.getId(), Lado.NO, no.getParticipaciones(), null);
        predicciones.vender("ana-r9@ucm.es", m.getId(), Lado.SI, si.getParticipaciones(), null);
        BigDecimal trasOperar = saldo("ana-r9@ucm.es");
        predicciones.anular(m.getId());
        BigDecimal despues = saldo("ana-r9@ucm.es");
        // Anular debería dejarla como mucho como empezó (le devuelve lo perdido), nunca con más
        assertThat(despues).isLessThanOrEqualTo(antes);
    }

    /** Lo devuelto al anular a quien luego elimina su cuenta deja de contar en el panel: la casa «gana» al darse de baja. */
    @Test
    void eliminarLaCuentaNoCambiaElBeneficioDePredicciones() {
        usuarioService.crear("luis-r9@ucm.es", "Luis", "secreta123", Rol.USUARIO);
        MercadoPrediccion m = predicciones.crear("¿R9b?", null, Hora.ahora().plusDays(5), new BigDecimal("100"),
                new BigDecimal("2"));
        predicciones.comprar("luis-r9@ucm.es", m.getId(), Lado.SI, new BigDecimal("50"), null);
        predicciones.anular(m.getId());
        BigDecimal antes = predicciones.cuentas().getBeneficio();
        bajas.eliminarCuenta("luis-r9@ucm.es", "secreta123");
        BigDecimal despues = predicciones.cuentas().getBeneficio();
        assertThat(despues).isEqualByComparingTo(antes);
    }
}
