package es.ucm.fdi.is1.apuestas.prediccion;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import org.junit.jupiter.api.Test;

/** HU-64 y HU-65: el creador de mercado automático LMSR (sin base de datos). */
class LmsrTest {

    private static final double B = 100;

    @Test
    void empiezaAlCincuentaPorCientoYLosPreciosSumanUno() {
        // CA-63.1
        assertThat(Lmsr.precioSi(0, 0, B)).isCloseTo(0.5, within(1e-12));
        double si = Lmsr.precioSi(80, 20, B);
        double no = Lmsr.precioSi(20, 80, B);
        assertThat(si + no).isCloseTo(1, within(1e-12));
        assertThat(si).isGreaterThan(0.5);
    }

    @Test
    void comprarCuestaLoQueSubeLaFuncionDeCoste() {
        // La fórmula cerrada da justo las participaciones que cuestan ese dinero: C(q + Δ) − C(q) = dinero
        double delta = Lmsr.participacionesPorDinero(true, 30, 10, 5, B);
        assertThat(Lmsr.coste(10 + delta, 5, B) - Lmsr.coste(10, 5, B)).isCloseTo(30, within(1e-9));
        assertThat(Lmsr.precioSi(10 + delta, 5, B)).isGreaterThan(Lmsr.precioSi(10, 5, B));
    }

    @Test
    void venderLoQueSeAcabaDeComprarDevuelveLoMismo() {
        // CA-65.1: sin comisión, comprar y vender enseguida deja el dinero igual (no hay ganancia gratis)
        double delta = Lmsr.participacionesPorDinero(false, 50, 0, 0, B);
        double devuelto = Lmsr.dineroPorVenta(false, delta, 0, delta, B);
        assertThat(devuelto).isCloseTo(50, within(1e-9));
    }

    @Test
    void cadaParticipacionCuestaMenosDeUnaMonedita() {
        // Comprar con un precio por debajo de 1 siempre da más participaciones que moneditas se pagan
        assertThat(Lmsr.participacionesPorDinero(true, 10, 0, 0, B)).isGreaterThan(10);
    }

    @Test
    void laCasaNoPierdeMasDeBPorLogaritmoDeDos() {
        // Aunque alguien compre muchísimo de un lado y acierte, la casa no pierde más de b · ln 2
        double delta = Lmsr.participacionesPorDinero(true, 5000, 0, 0, B);
        double cobrado = Lmsr.coste(delta, 0, B) - Lmsr.coste(0, 0, B);
        double perdidaCasa = delta - cobrado;
        assertThat(perdidaCasa).isLessThanOrEqualTo(Lmsr.perdidaMaxima(B) + 1e-9);
        assertThat(Lmsr.perdidaMaxima(B)).isCloseTo(69.3147, within(1e-4));
    }

    @Test
    void noSeDesbordaConMuchasParticipaciones() {
        assertThat(Lmsr.coste(1e6, 0, B)).isFinite();
        assertThat(Lmsr.precioSi(1e6, 0, B)).isCloseTo(1, within(1e-9));
    }
}
