package es.ucm.fdi.is1.apuestas.usuarios;

import java.security.Principal;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;

@Controller
public class UsuariosController {

    private final UsuarioService usuarios;
    private final SaldoProperties saldo;

    public UsuariosController(UsuarioService usuarios, SaldoProperties saldo) {
        this.usuarios = usuarios;
        this.saldo = saldo;
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/registro")
    public String registro(Model model) {
        model.addAttribute("registro", new RegistroForm());
        model.addAttribute("bienvenida", saldo.bienvenida());
        return "registro";
    }

    @PostMapping("/registro")
    public String registrar(@Valid @ModelAttribute("registro") RegistroForm form, BindingResult errores, Model model) {
        if (form.getPassword() != null && !form.getPassword().equals(form.getConfirmacion())) {
            errores.rejectValue("confirmacion", "noCoincide", "Las contraseñas no coinciden");
        }
        if (!errores.hasErrors()) {
            try {
                usuarios.registrar(form);
                return "redirect:/login?registrado";
            } catch (EmailYaRegistradoException e) {
                errores.rejectValue("email", "duplicado", "Ya existe una cuenta con ese email");
            }
        }
        model.addAttribute("bienvenida", saldo.bienvenida());
        return "registro";
    }

    @GetMapping("/cuenta")
    public String cuenta(Principal principal, Model model) {
        Usuario usuario = usuarios.consultarConRecarga(principal.getName());
        NombreForm nombre = new NombreForm();
        nombre.setNombre(usuario.getNombre());
        model.addAttribute("datosNombre", nombre);
        model.addAttribute("datosPassword", new PasswordForm());
        return cuenta(usuario, model);
    }

    /** HU-47. */
    @PostMapping("/cuenta/nombre")
    public String cambiarNombre(@Valid @ModelAttribute("datosNombre") NombreForm form, BindingResult errores,
                                Principal principal, Model model, RedirectAttributes redireccion) {
        if (errores.hasErrors()) {
            model.addAttribute("datosPassword", new PasswordForm());
            return cuenta(usuarios.consultarConRecarga(principal.getName()), model);
        }
        usuarios.cambiarNombre(principal.getName(), form.getNombre());
        redireccion.addFlashAttribute("mensaje", "Nombre cambiado");
        return "redirect:/cuenta";
    }

    /** HU-47. */
    @PostMapping("/cuenta/password")
    public String cambiarPassword(@Valid @ModelAttribute("datosPassword") PasswordForm form, BindingResult errores,
                                  Principal principal, Model model, RedirectAttributes redireccion) {
        if (form.getNueva() != null && !form.getNueva().equals(form.getConfirmacion())) {
            errores.rejectValue("confirmacion", "noCoincide", "Las contraseñas no coinciden");
        }
        if (!errores.hasErrors()) {
            try {
                usuarios.cambiarPassword(principal.getName(), form.getActual(), form.getNueva());
                redireccion.addFlashAttribute("mensaje", "Contraseña cambiada. Úsala la próxima vez que entres.");
                return "redirect:/cuenta";
            } catch (PasswordIncorrectaException e) {
                errores.rejectValue("actual", "incorrecta", e.getMessage());
            }
        }
        Usuario usuario = usuarios.consultarConRecarga(principal.getName());
        NombreForm nombre = new NombreForm();
        nombre.setNombre(usuario.getNombre());
        model.addAttribute("datosNombre", nombre);
        return cuenta(usuario, model);
    }

    private String cuenta(Usuario usuario, Model model) {
        model.addAttribute("usuario", usuario);
        model.addAttribute("proximaRecarga", usuario.proximaRecarga(saldo.recargaPeriodo()));
        model.addAttribute("importeRecarga", saldo.recargaImporte());
        return "cuenta";
    }
}
