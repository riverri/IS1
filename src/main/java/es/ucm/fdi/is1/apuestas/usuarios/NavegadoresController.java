package es.ucm.fdi.is1.apuestas.usuarios;

import java.security.Principal;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Account page for reviewing and revoking remembered browsers. */
@Controller
public class NavegadoresController {

    private final JdbcNavegadorTokenRepository tokens;

    public NavegadoresController(JdbcNavegadorTokenRepository tokens) {
        this.tokens = tokens;
    }

    @GetMapping("/cuenta/navegadores")
    public String listar(Principal principal, Model model) {
        model.addAttribute("navegadores", tokens.findByUsername(principal.getName()));
        return "navegadores";
    }

    @PostMapping("/cuenta/navegadores/{serie}/revocar")
    public String revocar(@PathVariable String serie, Principal principal, RedirectAttributes redirect) {
        tokens.removeToken(principal.getName(), serie);
        redirect.addFlashAttribute("mensaje", "Se ha revocado el navegador seleccionado");
        return "redirect:/cuenta/navegadores";
    }
}
