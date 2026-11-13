package es.ucm.fdi.is1.apuestas.gestion;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import es.ucm.fdi.is1.apuestas.apuesta.CuentasCasaService;
import es.ucm.fdi.is1.apuestas.cripto.CriptoService;
import es.ucm.fdi.is1.apuestas.prediccion.PrediccionService;

/**
 * Controlador del panel de la casa para el creador de apuestas (HU-53). Los cálculos (total apostado,
 * pagado, beneficio y margen real frente al teórico) los hace {@link CuentasCasaService}.
 */
@Controller
public class CasaController {

    private final CuentasCasaService cuentas;
    private final PrediccionService predicciones;
    private final CriptoService cripto;

    public CasaController(CuentasCasaService cuentas, PrediccionService predicciones, CriptoService cripto) {
        this.cuentas = cuentas;
        this.predicciones = predicciones;
        this.cripto = cripto;
    }

    /**
     * GET /gestion/casa (solo CREADOR): cuentas de la casa, en total y desglosadas por tipo de apuesta
     * y por deporte (CA-54.1, CA-54.2).
     */
    @GetMapping("/gestion/casa")
    public String casa(Model model) {
        model.addAttribute("cuentas", cuentas.cuentas());
        model.addAttribute("predicciones", predicciones.cuentas());
        model.addAttribute("cripto", cripto.cuentas());
        return "gestion/casa";
    }
}
