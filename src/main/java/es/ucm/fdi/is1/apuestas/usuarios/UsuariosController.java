package es.ucm.fdi.is1.apuestas.usuarios;

import java.security.Principal;
import java.util.List;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
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
    private final RegistroCorreoService registroCorreo;
    private final CambioCorreoService cambioCorreo;
    private final String basePublica;
    private final boolean registroAuxiliarHabilitado;

    public UsuariosController(UsuarioService usuarios, SaldoProperties saldo, Clock reloj, Sesiones sesiones,
                              RegistroCorreoService registroCorreo,
                              CambioCorreoService cambioCorreo,
                              @Value("${apuestas.url-publica}") String basePublica,
                              @Value("${apuestas.registro.auxiliar-habilitado:false}") boolean registroAuxiliarHabilitado) {
        this.usuarios = usuarios;
        this.saldo = saldo;
        this.reloj = reloj;
        this.sesiones = sesiones;
        this.registroCorreo = registroCorreo;
        this.cambioCorreo = cambioCorreo;
        this.basePublica = sinBarraFinal(basePublica);
        this.registroAuxiliarHabilitado = registroAuxiliarHabilitado;
    }

    /**
     * GET /login: formulario de inicio de sesión (HU-12). El envío (POST /login) no llega aquí:
     * lo procesa Spring Security, configurado en {@code SeguridadConfig}.
     */
    @GetMapping("/login")
    public String login() {
        return "login";
    }

    private static final String REGISTRO_EMAIL = UsuariosController.class.getName() + ".email";
    private static final String REGISTRO_CADUCA = UsuariosController.class.getName() + ".caduca";
    private static final Duration VALIDEZ_SESION_REGISTRO = Duration.ofMinutes(15);

    /** GET /registro: requests an email confirmation before collecting credentials. */
    @GetMapping("/registro")
    public String registro(Model model) {
        model.addAttribute("solicitud", new SolicitudRegistroForm());
        model.addAttribute("bienvenida", saldo.bienvenida());
        return "registro";
    }

    /** Sends a generic response so account existence cannot be inferred from this endpoint. */
    @PostMapping("/registro")
    public String registrar(@Valid @ModelAttribute("solicitud") SolicitudRegistroForm form,
                            BindingResult errores, Model model) {
        if (errores.hasErrors()) {
            model.addAttribute("bienvenida", saldo.bienvenida());
            return "registro";
        }
        registroCorreo.solicitar(form.getEmail(), basePublica);
        return "redirect:/login?verificacionPendiente";
    }

    /** Página auxiliar para entornos de demostración donde todavía no se dispone de SMTP. */
    @GetMapping("/registro-auxiliar")
    public String registroAuxiliar(Model model) {
        if (!registroAuxiliarHabilitado) {
            return "redirect:/registro";
        }
        model.addAttribute("registroAuxiliar", new RegistroAuxiliarForm());
        model.addAttribute("bienvenida", saldo.bienvenida());
        return "registro-auxiliar";
    }

    /** Crea directamente una cuenta verificada; la ruta solo existe cuando se habilita explícitamente. */
    @PostMapping("/registro-auxiliar")
    public String registrarAuxiliar(@Valid @ModelAttribute("registroAuxiliar") RegistroAuxiliarForm form,
                                    BindingResult errores, Model model) {
        if (!registroAuxiliarHabilitado) {
            return "redirect:/registro";
        }
        if (form.getPassword() != null && !form.getPassword().equals(form.getConfirmacion())) {
            errores.rejectValue("confirmacion", "noCoincide", "Las contraseñas no coinciden");
        }
        if (!Contrasenas.cabe(form.getPassword())) {
            errores.rejectValue("password", "larga", Contrasenas.DEMASIADO_LARGA);
        }
        if (errores.hasErrors()) {
            model.addAttribute("bienvenida", saldo.bienvenida());
            return "registro-auxiliar";
        }
        try {
            usuarios.registrarVerificado(form.getEmail(), form.getNombre(), form.getPassword());
            return "redirect:/login?registrado";
        } catch (EmailYaRegistradoException e) {
            errores.rejectValue("email", "duplicado", "Ese correo ya está asociado a una cuenta");
            model.addAttribute("bienvenida", saldo.bienvenida());
            return "registro-auxiliar";
        }
    }

    @GetMapping("/registro/completar")
    public String completarRegistro(HttpServletRequest peticion, Model model) {
        HttpSession sesion = peticion.getSession(false);
        if (!registroConfirmado(sesion)) {
            return "redirect:/registro";
        }
        model.addAttribute("registro", new CompletarRegistroForm());
        model.addAttribute("emailConfirmado", sesion.getAttribute(REGISTRO_EMAIL));
        model.addAttribute("bienvenida", saldo.bienvenida());
        return "registro-completar";
    }

    @PostMapping("/registro/completar")
    public String guardarRegistro(@Valid @ModelAttribute("registro") CompletarRegistroForm form,
                                  BindingResult errores, HttpServletRequest peticion, Model model) {
        HttpSession sesion = peticion.getSession(false);
        if (!registroConfirmado(sesion)) {
            return "redirect:/registro";
        }
        if (form.getPassword() != null && !form.getPassword().equals(form.getConfirmacion())) {
            errores.rejectValue("confirmacion", "noCoincide", "Las contraseñas no coinciden");
        }
        if (!Contrasenas.cabe(form.getPassword())) {
            errores.rejectValue("password", "larga", Contrasenas.DEMASIADO_LARGA);
        }
        if (errores.hasErrors()) {
            model.addAttribute("emailConfirmado", sesion.getAttribute(REGISTRO_EMAIL));
            model.addAttribute("bienvenida", saldo.bienvenida());
            return "registro-completar";
        }
        try {
            usuarios.registrarVerificado((String) sesion.getAttribute(REGISTRO_EMAIL),
                    form.getNombre(), form.getPassword());
            sesion.removeAttribute(REGISTRO_EMAIL);
            sesion.removeAttribute(REGISTRO_CADUCA);
            return "redirect:/login?registrado";
        } catch (EmailYaRegistradoException e) {
            sesion.removeAttribute(REGISTRO_EMAIL);
            sesion.removeAttribute(REGISTRO_CADUCA);
            return "redirect:/login?verificacionPendiente";
        }
    }

    private boolean registroConfirmado(HttpSession sesion) {
        if (sesion == null || sesion.getAttribute(REGISTRO_EMAIL) == null
                || !(sesion.getAttribute(REGISTRO_CADUCA) instanceof Long caduca)
                || reloj.millis() >= caduca) {
            if (sesion != null) {
                sesion.removeAttribute(REGISTRO_EMAIL);
                sesion.removeAttribute(REGISTRO_CADUCA);
            }
            return false;
        }
        return true;
    }

    void habilitarCompletarRegistro(HttpServletRequest peticion, String email) {
        peticion.getSession(true);
        peticion.changeSessionId();
        HttpSession sesion = peticion.getSession(true);
        sesion.setAttribute(REGISTRO_EMAIL, email);
        sesion.setAttribute(REGISTRO_CADUCA, reloj.millis() + VALIDEZ_SESION_REGISTRO.toMillis());
    }

    /** GET /cuenta: Mi cuenta. Antes aplica la recarga gratuita si toca, así el saldo está al día (HU-13, HU-14). */
    @GetMapping("/cuenta")
    public String cuenta(Principal principal, Model model) {
        return cuenta(usuarios.consultarConRecarga(principal.getName()), model);
    }

    /** GET /cuenta/movimientos: cada entrada y salida del saldo, con su motivo y el saldo que dejó (HU-55). */
    @GetMapping("/cuenta/movimientos")
    public String movimientos(Principal principal, Model model) {
        List<MovimientoSaldo> movimientos = usuarios.movimientos(principal.getName());
        model.addAttribute("movimientos", movimientos);
        // Como en Mi cuenta: si acaba de aplicarse la recarga, la cabecera enseña el saldo ya recargado
        if (!movimientos.isEmpty()) {
            model.addAttribute("usuarioActual", movimientos.getFirst().getUsuario());
        }
        return "movimientos";
    }

    /**
     * POST /cuenta/limites: límites diario y semanal que se pone el usuario (HU-10). Vuelve a Mi cuenta.
     * Si el diario supera al semanal, lo muestra como error del campo.
     */
    @PostMapping("/cuenta/limites")
    public String limites(@Valid @ModelAttribute("limitesPersonales") LimitesPersonalesForm form,
                          BindingResult errores, Principal principal, Model model, RedirectAttributes redireccion) {
        if (!errores.hasErrors()) {
            try {
                usuarios.fijarLimites(principal.getName(), form.getDiario(), form.getSemanal());
                redireccion.addFlashAttribute("mensaje", "Límites guardados");
                // "#juego-responsable" hace que el navegador baje hasta esa sección de la página
                return "redirect:/cuenta";
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
        return "redirect:/cuenta";
    }

    /** POST /cuenta/correos: activa o desactiva el correo al ganar una apuesta (HU-77, CA-77.2). */
    @PostMapping("/cuenta/correos")
    public String correos(@RequestParam boolean activados, Principal principal, RedirectAttributes redireccion) {
        usuarios.activarCorreos(principal.getName(), activados);
        redireccion.addFlashAttribute("mensaje", activados
                ? "Te avisaremos por correo cuando ganes una apuesta."
                : "Ya no recibirás correos al ganar; los avisos de la web siguen igual.");
        return "redirect:/cuenta";
    }

    /** POST /cuenta/resumen: activa o desactiva el resumen semanal por correo (HU-92, CA-92.2). */
    @PostMapping("/cuenta/resumen")
    public String resumen(@RequestParam boolean activado, Principal principal, RedirectAttributes redireccion) {
        usuarios.activarResumen(principal.getName(), activado);
        redireccion.addFlashAttribute("mensaje", activado
                ? "Te mandaremos cada lunes el resumen de tu semana."
                : "Ya no recibirás el resumen semanal.");
        return "redirect:/cuenta";
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

    /** Requests an email change after checking the current password and sends confirmation to the new address. */
    @PostMapping("/cuenta/correo")
    public String cambiarCorreo(@Valid @ModelAttribute("cambioCorreo") CambioCorreoForm form, BindingResult errores,
                                Principal principal, Model model, RedirectAttributes redireccion) {
        if (errores.hasErrors()) {
            return cuenta(usuarios.consultarConRecarga(principal.getName()), model);
        }
        try {
            cambioCorreo.solicitar(principal.getName(), form.getPassword(), form.getNuevoEmail(), basePublica);
            redireccion.addFlashAttribute("mensaje", "Confirma el cambio desde el enlace enviado al nuevo correo.");
        } catch (PasswordIncorrectaException e) {
            errores.rejectValue("password", "incorrecta", e.getMessage());
            return cuenta(usuarios.consultarConRecarga(principal.getName()), model);
        } catch (EmailYaRegistradoException e) {
            errores.rejectValue("nuevoEmail", "duplicado", "Ese correo ya está asociado a una cuenta");
            return cuenta(usuarios.consultarConRecarga(principal.getName()), model);
        } catch (DemasiadosCambiosCorreoException e) {
            errores.rejectValue("nuevoEmail", "demasiados", e.getMessage());
            return cuenta(usuarios.consultarConRecarga(principal.getName()), model);
        }
        return "redirect:/cuenta";
    }

    /** Página Mi cuenta; los formularios que ya están en el modelo (con sus errores) se respetan. */
    private String cuenta(Usuario usuario, Model model) {
        // La cabecera (usuarioActual) se cargó antes de aplicar la recarga: se pone el usuario al día para que su saldo
        // sea el mismo que el de la tarjeta de Mi cuenta
        model.addAttribute("usuarioActual", usuario);
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
        if (!model.containsAttribute("cambioCorreo")) {
            model.addAttribute("cambioCorreo", new CambioCorreoForm());
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

    private static String sinBarraFinal(String url) {
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }
}
