package es.ucm.fdi.is1.apuestas.usuarios;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.servlet.http.HttpServletRequest;

/** Public pages for requesting and confirming an email verification link. */
@Controller
public class VerificacionCorreoController {

    private final VerificacionCorreoService verificacion;
    private final RegistroCorreoService registroCorreo;
    private final UsuariosController usuarios;
    private final String basePublica;

    public VerificacionCorreoController(VerificacionCorreoService verificacion, RegistroCorreoService registroCorreo,
                                        UsuariosController usuarios,
                                        @Value("${apuestas.url-publica}") String basePublica) {
        this.verificacion = verificacion;
        this.registroCorreo = registroCorreo;
        this.usuarios = usuarios;
        this.basePublica = basePublica.endsWith("/")
                ? basePublica.substring(0, basePublica.length() - 1) : basePublica;
    }

    @GetMapping("/verificar-correo")
    public String solicitar() {
        return "verificar-correo";
    }

    @PostMapping("/verificar-correo")
    public String reenviar(@RequestParam String email, RedirectAttributes redireccion) {
        registroCorreo.solicitar(email, basePublica);
        verificacion.solicitar(email, basePublica);
        redireccion.addFlashAttribute("mensaje",
                "Si hay una cuenta pendiente con esa dirección, enviaremos un enlace de verificación.");
        return "redirect:/verificar-correo";
    }

    @GetMapping("/verificar-correo/confirmar")
    public String confirmar(@RequestParam(required = false) String token, Model model) {
        model.addAttribute("token", token);
        model.addAttribute("valido", verificacion.valido(token));
        return "confirmar-correo";
    }

    @PostMapping("/verificar-correo/confirmar")
    public String consumir(@RequestParam String token, RedirectAttributes redireccion) {
        try {
            verificacion.confirmar(token);
            return "redirect:/login?verificado";
        } catch (EnlaceVerificacionNoValidoException e) {
            redireccion.addFlashAttribute("error", e.getMessage());
            return "redirect:/verificar-correo/confirmar";
        }
    }

    @GetMapping("/registro/confirmar")
    public String confirmarRegistro(@RequestParam(required = false) String token, Model model) {
        model.addAttribute("token", token);
        var email = registroCorreo.emailConfirmadoPor(token);
        model.addAttribute("emailConfirmado", email.orElse(""));
        model.addAttribute("valido", email.isPresent());
        return "confirmar-registro";
    }

    @PostMapping("/registro/confirmar")
    public String consumirRegistro(@RequestParam String token, HttpServletRequest peticion,
                                   RedirectAttributes redireccion) {
        try {
            String email = registroCorreo.confirmar(token);
            usuarios.habilitarCompletarRegistro(peticion, email);
            return "redirect:/registro/completar";
        } catch (EnlaceVerificacionNoValidoException e) {
            redireccion.addFlashAttribute("error", e.getMessage());
            return "redirect:/registro/confirmar";
        }
    }
}
