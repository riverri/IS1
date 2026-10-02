package es.ucm.fdi.is1.apuestas.usuarios;

import java.security.Principal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;

@Controller
public class UsuariosController {

    private final UsuarioService usuarios;
    private final SaldoProperties saldo;
    private final Clock reloj;

    public UsuariosController(UsuarioService usuarios, SaldoProperties saldo, Clock reloj) {
        this.usuarios = usuarios;
        this.saldo = saldo;
        this.reloj = reloj;
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
        return cuenta(usuarios.consultarConRecarga(principal.getName()), model);
    }

    /** Límites diario y semanal que se pone el usuario (HU-10). */
    @PostMapping("/cuenta/limites")
    public String limites(@Valid @ModelAttribute("limitesPersonales") LimitesPersonalesForm form,
                          BindingResult errores, Principal principal, Model model, RedirectAttributes redireccion) {
        if (!errores.hasErrors()) {
            try {
                usuarios.fijarLimites(principal.getName(), form.getDiario(), form.getSemanal());
                redireccion.addFlashAttribute("mensaje", "Límites guardados");
                return "redirect:/cuenta#juego-responsable";
            } catch (IllegalArgumentException e) {
                errores.rejectValue("diario", "mayorQueSemanal", e.getMessage());
            }
        }
        return cuenta(usuarios.consultarConRecarga(principal.getName()), model);
    }

    /** Pausa temporal de las apuestas (HU-10). */
    @PostMapping("/cuenta/pausa")
    public String pausa(@RequestParam(defaultValue = "0") int dias, Principal principal,
                        RedirectAttributes redireccion) {
        try {
            LocalDateTime hasta = usuarios.pausar(principal.getName(), dias);
            redireccion.addFlashAttribute("mensaje", "Apuestas en pausa hasta el "
                    + hasta.format(DateTimeFormatter.ofPattern("dd/MM/yyyy 'a las' HH:mm")) + ".");
        } catch (IllegalArgumentException e) {
            redireccion.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/cuenta#juego-responsable";
    }

    /** HU-47. */
    @PostMapping("/cuenta/nombre")
    public String cambiarNombre(@Valid @ModelAttribute("datosNombre") NombreForm form, BindingResult errores,
                                Principal principal, Model model, RedirectAttributes redireccion) {
        if (errores.hasErrors()) {
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
        return cuenta(usuarios.consultarConRecarga(principal.getName()), model);
    }

    /** Página Mi cuenta; los formularios que ya están en el modelo (con sus errores) se respetan. */
    private String cuenta(Usuario usuario, Model model) {
        if (!model.containsAttribute("datosNombre")) {
            NombreForm nombre = new NombreForm();
            nombre.setNombre(usuario.getNombre());
            model.addAttribute("datosNombre", nombre);
        }
        if (!model.containsAttribute("datosPassword")) {
            model.addAttribute("datosPassword", new PasswordForm());
        }
        if (!model.containsAttribute("limitesPersonales")) {
            LimitesPersonalesForm limites = new LimitesPersonalesForm();
            limites.setDiario(usuario.getLimiteDiario());
            limites.setSemanal(usuario.getLimiteSemanal());
            model.addAttribute("limitesPersonales", limites);
        }
        model.addAttribute("enPausa", usuario.enPausa(LocalDateTime.now(reloj)));
        model.addAttribute("usuario", usuario);
        model.addAttribute("proximaRecarga", usuario.proximaRecarga(saldo.recargaPeriodo()));
        model.addAttribute("importeRecarga", saldo.recargaImporte());
        return "cuenta";
    }
}
