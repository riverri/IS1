package es.ucm.fdi.is1.apuestas.gestion;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import es.ucm.fdi.is1.apuestas.apuesta.CuentasCasaService;

/**
 * Controlador del panel de la casa para el creador de apuestas (HU-54). Los cálculos (total apostado,
 * pagado, beneficio y margen real frente al teórico) los hace {@link CuentasCasaService}.
 */
@Controller
public class CasaController {

    private final CuentasCasaService cuentas;

    public CasaController(CuentasCasaService cuentas) {
        this.cuentas = cuentas;
    }

    /**
     * GET /gestion/casa (solo CREADOR): cuentas de la casa, en total y desglosadas por tipo de apuesta
     * y por deporte (CA-54.1, CA-54.2).
     */
    @GetMapping("/gestion/casa")
    public String casa(Model model) {
        model.addAttribute("cuentas", cuentas.cuentas());
        return "gestion/casa";
    }
}
