package es.ucm.fdi.is1.apuestas.web;

import java.security.Principal;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import es.ucm.fdi.is1.apuestas.usuarios.Usuario;
import es.ucm.fdi.is1.apuestas.usuarios.UsuarioService;

/** Pone el usuario con sesión iniciada en todas las vistas (cabecera con nombre y saldo). */
@ControllerAdvice
public class UsuarioActualAdvice {

    private final UsuarioService usuarios;

    public UsuarioActualAdvice(UsuarioService usuarios) {
        this.usuarios = usuarios;
    }

    @ModelAttribute("usuarioActual")
    public Usuario usuarioActual(Principal principal) {
        if (principal == null) {
            return null;
        }
        return usuarios.buscar(principal.getName()).orElse(null);
    }
}
