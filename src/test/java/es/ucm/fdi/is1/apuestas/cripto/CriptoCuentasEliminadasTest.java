package es.ucm.fdi.is1.apuestas.cripto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import java.math.BigDecimal;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import es.ucm.fdi.is1.apuestas.apuesta.BajaService;
import es.ucm.fdi.is1.apuestas.usuarios.Rol;
import es.ucm.fdi.is1.apuestas.usuarios.UsuarioService;

/** Sexta depuración: las carteras de cuentas eliminadas no son deuda de la casa. */
@SpringBootTest
@Transactional
class CriptoCuentasEliminadasTest {
    @Autowired CriptoService cripto;
    @Autowired UsuarioService usuarioService;
    @Autowired BajaService bajas;
    @MockitoBean FuentePrecios fuente;

    @Test
    void laCarteraDeUnaCuentaEliminadaNoCuentaComoDeudaDeLaCasa() {
        usuarioService.crear("ana@ucm.es", "Ana", "secreta123", Rol.USUARIO);
        given(fuente.precios(any())).willReturn(Map.of("dogecoin",
                new FuentePrecios.Precio(new BigDecimal("0.20"), null)));
        cripto.actualizarPrecios();
        cripto.comprar("ana@ucm.es", "DOGE", new BigDecimal("100"), null);
        bajas.eliminarCuenta("ana@ucm.es", "secreta123");
        CriptoService.CuentasCripto c = cripto.cuentas();
        // Ana ya no puede vender: la casa se ha quedado los 100
        assertThat(c.getBeneficio()).isEqualByComparingTo("100");
    }
}
