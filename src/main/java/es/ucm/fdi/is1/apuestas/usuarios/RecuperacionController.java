package es.ucm.fdi.is1.apuestas.usuarios;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import jakarta.validation.Valid;

/** Recuperar la contraseña (HU-17). */
@Controller
@RequestMapping("/recuperar")
public class RecuperacionController {

    private final RecuperacionService recuperacion;
    private final Sesiones sesiones;

    public RecuperacionController(RecuperacionService recuperacion, Sesiones sesiones) {
        this.recuperacion = recuperacion;
        this.sesiones = sesiones;
    }

    @GetMapping
    public String formulario() {
        return "recuperar";
    }

    /** Siempre la misma respuesta, exista o no la cuenta. */
    @PostMapping
    public String solicitar(@RequestParam(defaultValue = "") String email) {
        recuperacion.solicitar(email, ServletUriComponentsBuilder.fromCurrentContextPath().toUriString());
        return "redirect:/recuperar?enviado";
    }

    @GetMapping("/nueva")
    public String nueva(@RequestParam(defaultValue = "") String token, Model model) {
        NuevaPasswordForm form = new NuevaPasswordForm();
        form.setToken(token);
        model.addAttribute("nuevaPassword", form);
        model.addAttribute("enlaceValido", recuperacion.valido(token));
        return "recuperar-nueva";
    }

    @PostMapping("/nueva")
    public String restablecer(@Valid @ModelAttribute("nuevaPassword") NuevaPasswordForm form,
                              BindingResult errores, Model model) {
        if (form.getNueva() != null && !form.getNueva().equals(form.getConfirmacion())) {
            errores.rejectValue("confirmacion", "noCoincide", "Las contraseñas no coinciden");
        }
        if (!Contrasenas.cabe(form.getNueva())) {
            errores.rejectValue("nueva", "larga", Contrasenas.DEMASIADO_LARGA);
        }
        if (!errores.hasErrors()) {
            try {
                String email = recuperacion.restablecer(form.getToken(), form.getNueva());
                sesiones.cerrarOtras(email, null);
                return "redirect:/login?recuperada";
            } catch (EnlaceNoValidoException e) {
                model.addAttribute("enlaceValido", false);
                return "recuperar-nueva";
            }
        }
        model.addAttribute("enlaceValido", true);
        return "recuperar-nueva";
    }
}
