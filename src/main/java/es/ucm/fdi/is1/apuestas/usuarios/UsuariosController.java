package es.ucm.fdi.is1.apuestas.usuarios;

import java.security.Principal;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

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

    /** Ranking público: muestra el top 20 y, si no estás entre ellos, también tu posición (HU-36). */
    @GetMapping("/ranking")
    public String ranking(Principal principal, Model model) {
        List<PuestoRanking> todos = usuarios.ranking(principal == null ? null : principal.getName());
        List<PuestoRanking> primeros = todos.stream().limit(20).toList();
        PuestoRanking mio = todos.stream().filter(PuestoRanking::soyYo).findFirst()
                .filter(p -> p.posicion() > 20).orElse(null);
        model.addAttribute("primeros", primeros);
        model.addAttribute("mio", mio);
        model.addAttribute("total", todos.size());
        return "ranking";
    }

    @GetMapping("/cuenta")
    public String cuenta(Principal principal, Model model) {
        Usuario usuario = usuarios.consultarConRecarga(principal.getName());
        model.addAttribute("usuario", usuario);
        model.addAttribute("proximaRecarga", usuario.proximaRecarga(saldo.recargaPeriodo()));
        model.addAttribute("importeRecarga", saldo.recargaImporte());
        return "cuenta";
    }
}
