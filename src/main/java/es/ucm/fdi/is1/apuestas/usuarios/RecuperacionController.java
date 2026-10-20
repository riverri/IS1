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

/**
 * Controlador de "He olvidado mi contraseña" (HU-17). Sus rutas son públicas ({@code SeguridadConfig}):
 * quien la ha olvidado no puede iniciar sesión. La lógica está en {@link RecuperacionService}.
 */
@Controller
@RequestMapping("/recuperar")
public class RecuperacionController {

    private final RecuperacionService recuperacion;
    private final Sesiones sesiones;

    public RecuperacionController(RecuperacionService recuperacion, Sesiones sesiones) {
        this.recuperacion = recuperacion;
        this.sesiones = sesiones;
    }

    /** GET /recuperar: formulario para pedir el enlace con el email. */
    @GetMapping
    public String formulario() {
        return "recuperar";
    }

    /**
     * POST /recuperar: pide el enlace y vuelve a /recuperar?enviado. Siempre la misma respuesta,
     * exista o no la cuenta.
     */
    @PostMapping
    public String solicitar(@RequestParam(defaultValue = "") String email) {
        // La base del enlace es la dirección por la que se ha entrado: vale en local y en la nube
        recuperacion.solicitar(email, ServletUriComponentsBuilder.fromCurrentContextPath().toUriString());
        return "redirect:/recuperar?enviado";
    }

    /**
     * GET /recuperar/nueva?token=...: la página del enlace del correo. Si el enlace ha caducado o ya
     * se ha usado, la vista lo avisa en lugar de mostrar el formulario (CA-17.2).
     */
    @GetMapping("/nueva")
    public String nueva(@RequestParam(defaultValue = "") String token, Model model) {
        NuevaPasswordForm form = new NuevaPasswordForm();
        form.setToken(token);
        model.addAttribute("nuevaPassword", form);
        model.addAttribute("enlaceValido", recuperacion.valido(token));
        return "recuperar-nueva";
    }

    /**
     * POST /recuperar/nueva: comprueba que las dos contraseñas coinciden y caben en BCrypt, pone la nueva,
     * cierra todas las sesiones abiertas de esa cuenta y lleva a /login?recuperada.
     */
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
                // null: quien recupera no tiene sesión iniciada, así que se cierran todas
                sesiones.cerrarOtras(email, null);
                return "redirect:/login?recuperada";
            } catch (EnlaceNoValidoException e) {
                // Caducó o se usó mientras se rellenaba el formulario
                model.addAttribute("enlaceValido", false);
                return "recuperar-nueva";
            }
        }
        // Errores en el formulario: se vuelve a mostrar; el enlace se comprueba otra vez al reenviarlo
        model.addAttribute("enlaceValido", true);
        return "recuperar-nueva";
    }
}
