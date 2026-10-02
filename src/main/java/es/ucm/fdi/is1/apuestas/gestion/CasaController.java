package es.ucm.fdi.is1.apuestas.gestion;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import es.ucm.fdi.is1.apuestas.apuesta.CuentasCasaService;

/** Panel de la casa para el creador de apuestas (HU-54). */
@Controller
public class CasaController {

    private final CuentasCasaService cuentas;

    public CasaController(CuentasCasaService cuentas) {
        this.cuentas = cuentas;
    }

    @GetMapping("/gestion/casa")
    public String casa(Model model) {
        model.addAttribute("cuentas", cuentas.cuentas());
        return "gestion/casa";
    }
}
