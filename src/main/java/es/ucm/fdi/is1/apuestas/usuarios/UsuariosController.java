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

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

/**
 * Controlador de las cuentas: inicio de sesión, registro y Mi cuenta (saldo y recargas, juego responsable,
 * nombre y contraseña). HU-10, HU-11, HU-12, HU-13, HU-14 y HU-47. La baja (HU-18) está en
 * {@code BajaController}. La lógica está en {@link UsuarioService}.
 * Cuando un formulario tiene errores no se redirige: se vuelve a pintar la página para que se vean los mensajes.
 *
 * <p><b>Cómo funciona un controlador:</b>
 * <ul>
 *   <li>{@code @Controller}: Spring envía a esta clase las peticiones de las direcciones que declara.</li>
 *   <li>{@code @GetMapping("/x")}: el método se ejecuta al abrir la página /x (petición GET).
 *       {@code @PostMapping("/x")}: al enviar un formulario a /x (petición POST).</li>
 *   <li>El método devuelve un texto: el nombre de la plantilla HTML que se pinta ({@code "cuenta"} →
 *       {@code templates/cuenta.html}) o {@code "redirect:/otra"}, que manda al navegador a otra dirección.</li>
 *   <li>{@code Model}: la "mochila" de datos que se pasa a la plantilla; lo que se mete con
 *       {@code addAttribute("usuario", ...)} se usa en el HTML como {@code ${usuario}}.</li>
 *   <li>{@code Principal}: el usuario que ha iniciado sesión; {@code getName()} devuelve su email.</li>
 *   <li>{@code @Valid @ModelAttribute}: Spring rellena el formulario con los campos enviados y comprueba sus
 *       anotaciones ({@code @NotBlank}, {@code @Size}...); los errores quedan en el {@code BindingResult}.</li>
 *   <li>{@code RedirectAttributes.addFlashAttribute}: un mensaje que sobrevive a la redirección y se muestra
 *       una sola vez en la página siguiente.</li>
 * </ul>
 */
@Controller
public class UsuariosController {

    private final UsuarioService usuarios;
    private final SaldoProperties saldo;
    private final Clock reloj;
    private final Sesiones sesiones;

    public UsuariosController(UsuarioService usuarios, SaldoProperties saldo, Clock reloj, Sesiones sesiones) {
        this.usuarios = usuarios;
        this.saldo = saldo;
        this.reloj = reloj;
        this.sesiones = sesiones;
    }

    /**
     * GET /login: formulario de inicio de sesión (HU-12). El envío (POST /login) no llega aquí:
     * lo procesa Spring Security, configurado en {@code SeguridadConfig}.
     */
    @GetMapping("/login")
    public String login() {
        return "login";
    }

    /** GET /registro: formulario de registro, con el saldo de bienvenida que se recibirá (HU-11, HU-14). */
    @GetMapping("/registro")
    public String registro(Model model) {
        // Un formulario vacío para que la plantilla tenga dónde enlazar los campos (th:object="${registro}")
        model.addAttribute("registro", new RegistroForm());
        model.addAttribute("bienvenida", saldo.bienvenida());
        return "registro";
    }

    /**
     * POST /registro: crea la cuenta y lleva a /login?registrado (CA-11.1). Además de las reglas de
     * {@link RegistroForm}, comprueba que las contraseñas coinciden y caben en BCrypt.
     * Si el email ya está registrado, lo marca como error del campo (CA-11.2).
     */
    @PostMapping("/registro")
    public String registrar(@Valid @ModelAttribute("registro") RegistroForm form, BindingResult errores, Model model) {
        // rejectValue(campo, código, mensaje) añade un error a ese campo; la plantilla lo pinta bajo el campo
        if (form.getPassword() != null && !form.getPassword().equals(form.getConfirmacion())) {
            errores.rejectValue("confirmacion", "noCoincide", "Las contraseñas no coinciden");
        }
        if (!Contrasenas.cabe(form.getPassword())) {
            errores.rejectValue("password", "larga", Contrasenas.DEMASIADO_LARGA);
        }
        if (!errores.hasErrors()) {
            // try/catch: si registrar lanza EmailYaRegistradoException, se salta al catch en vez de romper la página
            try {
                usuarios.registrar(form);
                // Redirigir tras un POST evita que al recargar la página se envíe el formulario otra vez
                return "redirect:/login?registrado";
            } catch (EmailYaRegistradoException e) {
                errores.rejectValue("email", "duplicado", "Ya existe una cuenta con ese email");
            }
        }
        // Hay errores: se vuelve a pintar el registro con lo que escribió el usuario y los mensajes de error
        model.addAttribute("bienvenida", saldo.bienvenida());
        return "registro";
    }

    /** GET /cuenta: Mi cuenta. Antes aplica la recarga gratuita si toca, así el saldo está al día (HU-13, HU-14). */
    @GetMapping("/cuenta")
    public String cuenta(Principal principal, Model model) {
        return cuenta(usuarios.consultarConRecarga(principal.getName()), model);
    }

    /** GET /cuenta/movimientos: cada entrada y salida del saldo, con su motivo y el saldo que dejó (HU-55). */
    @GetMapping("/cuenta/movimientos")
    public String movimientos(Principal principal, Model model) {
        model.addAttribute("movimientos", usuarios.movimientos(principal.getName()));
        return "movimientos";
    }

    /**
     * POST /cuenta/limites: límites diario y semanal que se pone el usuario (HU-10). Vuelve a la sección
     * de juego responsable de Mi cuenta. Si el diario supera al semanal, lo muestra como error del campo.
     */
    @PostMapping("/cuenta/limites")
    public String limites(@Valid @ModelAttribute("limitesPersonales") LimitesPersonalesForm form,
                          BindingResult errores, Principal principal, Model model, RedirectAttributes redireccion) {
        if (!errores.hasErrors()) {
            try {
                usuarios.fijarLimites(principal.getName(), form.getDiario(), form.getSemanal());
                redireccion.addFlashAttribute("mensaje", "Límites guardados");
                // "#juego-responsable" hace que el navegador baje hasta esa sección de la página
                return "redirect:/cuenta#juego-responsable";
            } catch (IllegalArgumentException e) {
                errores.rejectValue("diario", "mayorQueSemanal", e.getMessage());
            }
        }
        return cuenta(usuarios.consultarConRecarga(principal.getName()), model);
    }

    /** POST /cuenta/pausa: pausa temporal de las apuestas (HU-10); los días deben ser 1, 7 o 30. */
    @PostMapping("/cuenta/pausa")
    // @RequestParam lee el campo "dias" del formulario y lo convierte a int
    public String pausa(@RequestParam(defaultValue = "0") int dias, Principal principal,
                        RedirectAttributes redireccion) {
        // Sin elegir duración llega 0, y el servicio lo rechaza con un mensaje
        try {
            LocalDateTime hasta = usuarios.pausar(principal.getName(), dias);
            redireccion.addFlashAttribute("mensaje", "Apuestas en pausa hasta el "
                    + hasta.format(DateTimeFormatter.ofPattern("dd/MM/yyyy 'a las' HH:mm")) + ".");
        } catch (IllegalArgumentException e) {
            redireccion.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/cuenta#juego-responsable";
    }

    /** POST /cuenta/nombre: cambia el nombre visible (HU-47). */
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

    /**
     * POST /cuenta/password: cambia la contraseña si la actual es correcta (HU-47) y cierra las sesiones
     * de la cuenta en otros navegadores; la de este sigue abierta.
     */
    @PostMapping("/cuenta/password")
    public String cambiarPassword(@Valid @ModelAttribute("datosPassword") PasswordForm form, BindingResult errores,
                                  Principal principal, Model model, RedirectAttributes redireccion,
                                  HttpServletRequest peticion) {
        if (form.getNueva() != null && !form.getNueva().equals(form.getConfirmacion())) {
            errores.rejectValue("confirmacion", "noCoincide", "Las contraseñas no coinciden");
        }
        if (!Contrasenas.cabe(form.getNueva())) {
            errores.rejectValue("nueva", "larga", Contrasenas.DEMASIADO_LARGA);
        }
        if (!errores.hasErrors()) {
            try {
                usuarios.cambiarPassword(principal.getName(), form.getActual(), form.getNueva());
                // getSession(false): la sesión actual, sin crear una nueva
                sesiones.cerrarOtras(principal.getName(), peticion.getSession(false));
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
        // Si el formulario ya está en el modelo es porque se envió con errores: se deja tal cual para mostrarlos.
        // Si no, se crea uno nuevo relleno con los datos actuales del usuario
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
        // Cuándo llega la próxima recarga gratuita y de cuánto es (HU-14)
        model.addAttribute("proximaRecarga", usuario.proximaRecarga(saldo.recargaPeriodo()));
        model.addAttribute("importeRecarga", saldo.recargaImporte());
        return "cuenta";
    }
}
