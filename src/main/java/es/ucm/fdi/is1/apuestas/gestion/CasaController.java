package es.ucm.fdi.is1.apuestas.gestion;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import es.ucm.fdi.is1.apuestas.apuesta.CuentasCasaService;
import es.ucm.fdi.is1.apuestas.casino.CasinoService;
import es.ucm.fdi.is1.apuestas.cripto.CriptoService;
import es.ucm.fdi.is1.apuestas.prediccion.PrediccionService;

/**
 * Controlador del panel de la casa para el creador de apuestas (HU-54). Los cálculos (total apostado,
 * pagado, beneficio y margen real frente al teórico) los hace {@link CuentasCasaService}.
 */
@Controller
public class CasaController {

    private final CuentasCasaService cuentas;
    private final CasinoService casino;
    private final PrediccionService predicciones;
    private final CriptoService cripto;

    public CasaController(CuentasCasaService cuentas, CasinoService casino, PrediccionService predicciones,
                          CriptoService cripto) {
        this.cuentas = cuentas;
        this.casino = casino;
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
        // El casino va aparte: no son apuestas deportivas, pero también es dinero que gana o pierde la casa
        model.addAttribute("casino", casino.resumenCasa());
        model.addAttribute("configuracionCasino", casino.configuracion());
        model.addAttribute("predicciones", predicciones.cuentas());
        model.addAttribute("cripto", cripto.cuentas());
        return "gestion/casa";
    }
}
