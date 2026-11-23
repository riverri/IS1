package es.ucm.fdi.is1.apuestas.prediccion;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import es.ucm.fdi.is1.apuestas.Hora;
import es.ucm.fdi.is1.apuestas.apuesta.BajaService;
import es.ucm.fdi.is1.apuestas.notificaciones.NotificacionRepository;
import es.ucm.fdi.is1.apuestas.usuarios.Rol;
import es.ucm.fdi.is1.apuestas.usuarios.UsuarioRepository;
import es.ucm.fdi.is1.apuestas.usuarios.UsuarioService;

/** Sexta depuración: vista previa de compras y cuentas eliminadas en las predicciones. */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class PrediccionSextaDepuracionWebTest {
    static final String ANA = "ana@ucm.es", LUIS = "luis@ucm.es";
    @Autowired MockMvc mvc;
    @Autowired PrediccionService predicciones;
    @Autowired UsuarioService usuarioService;
    @Autowired UsuarioRepository usuarios;
    @Autowired BajaService bajas;
    @Autowired NotificacionRepository notis;

    @BeforeEach
    void preparar() {
        usuarioService.crear(ANA, "Ana", "secreta123", Rol.USUARIO);
        usuarioService.crear(LUIS, "Luis", "secreta123", Rol.USUARIO);
    }

    @Test
    void cotizacionConImporteEnormeNoDaError500() throws Exception {
        MercadoPrediccion m = predicciones.crear("¿P?", null, Hora.ahora().plusDays(5), new BigDecimal("100"), new BigDecimal("2"));
        mvc.perform(get("/predicciones/" + m.getId()).param("lado", "SI").param("importe", "1E400"))
                .andExpect(status().isOk());
    }

    @Test
    void cotizacionDeUnCentimoEnUnMercadoMuyInclinadoNoDaError500() throws Exception {
        MercadoPrediccion m = predicciones.crear("¿P?", null, Hora.ahora().plusDays(5), new BigDecimal("10"), new BigDecimal("2"));
        predicciones.comprar(LUIS, m.getId(), Lado.NO, new BigDecimal("500"), null);
        mvc.perform(get("/predicciones/" + m.getId()).param("lado", "SI").param("importe", "0.01"))
                .andExpect(status().isOk());
    }

    @Test
    void unaCotizacionQueNoDaParticipacionesSeRechaza() {
        MercadoPrediccion m = predicciones.crear("¿P?", null, Hora.ahora().plusDays(5), new BigDecimal("100"), new BigDecimal("2"));
        // Antes daba -0,018 participaciones
        assertThatThrownBy(() -> predicciones.cotizarCompra(m.getId(), Lado.SI, new BigDecimal("0.001")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void loPagadoAUnaCuentaEliminadaNoCuentaEnLasCuentasDeLaCasa() {
        MercadoPrediccion m = predicciones.crear("¿P?", null, Hora.ahora().plusDays(5), new BigDecimal("100"), new BigDecimal("2"));
        predicciones.comprar(ANA, m.getId(), Lado.SI, new BigDecimal("10"), null);
        bajas.eliminarCuenta(ANA, "secreta123");
        long antes = notis.count();
        predicciones.resolver(m.getId(), Lado.SI);
        PrediccionService.CuentasPrediccion c = predicciones.cuentas();
        assertThat(notis.count()).isEqualTo(antes);
        // Ana (eliminada) no ha cobrado nada: la casa se ha quedado los 10
        assertThat(c.pagos()).isEqualByComparingTo("0");
    }

    @Test
    void loDevueltoAUnaCuentaEliminadaAlAnularNoCuenta() {
        MercadoPrediccion m = predicciones.crear("¿P?", null, Hora.ahora().plusDays(5), new BigDecimal("100"), new BigDecimal("2"));
        predicciones.comprar(ANA, m.getId(), Lado.SI, new BigDecimal("10"), null);
        bajas.eliminarCuenta(ANA, "secreta123");
        long antes = notis.count();
        predicciones.anular(m.getId());
        PrediccionService.CuentasPrediccion c = predicciones.cuentas();
        assertThat(c.devoluciones()).isEqualByComparingTo("0");
    }

    @Test
    void unaCuentaEliminadaNoRecibeAvisoAlResolverse() {
        MercadoPrediccion m = predicciones.crear("¿P?", null, Hora.ahora().plusDays(5), new BigDecimal("100"), new BigDecimal("2"));
        predicciones.comprar(ANA, m.getId(), Lado.SI, new BigDecimal("10"), null);
        bajas.eliminarCuenta(ANA, "secreta123");
        long antes = notis.count();
        predicciones.resolver(m.getId(), Lado.SI);
        assertThat(notis.count()).isEqualTo(antes);
    }
}
