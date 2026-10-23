package es.ucm.fdi.is1.apuestas.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.session.HttpSessionEventPublisher;

import es.ucm.fdi.is1.apuestas.usuarios.RecargaAlEntrarHandler;

/**
 * Configuración de Spring Security: quién puede entrar en cada página, inicio y cierre de sesión,
 * sesiones abiertas y cifrado de contraseñas (HU-08, HU-11, HU-12, HU-18, HU-47).
 * Hay tres niveles: visitante sin sesión (catálogo público), usuario con sesión (apostar, cuenta…)
 * y creador de apuestas (rol CREADOR: panel de Gestión).
 *
 * <p>Spring Security funciona como un "portero": cada petición pasa por una cadena de filtros antes de llegar a
 * los controladores. Si la página exige sesión y el visitante no la tiene, le manda a /login; si exige el rol
 * CREADOR y no lo tiene, le devuelve un error 403 (prohibido). Los métodos {@code @Bean} de esta clase fabrican
 * los objetos de seguridad que Spring usa (ver {@link RelojConfig} para qué es un {@code @Bean}).
 */
@Configuration
public class SeguridadConfig {

    /** Reglas de acceso. Se comprueban en orden y se aplica la primera que encaja con la dirección. */
    @Bean
    public SecurityFilterChain filtroSeguridad(HttpSecurity http, RecargaAlEntrarHandler alEntrar,
                                               SessionRegistry registroSesiones) throws Exception {
        // Estilo "fluido": cada método configura una parte y devuelve el mismo objeto, por eso se encadenan con puntos.
        // Cada parte recibe una lambda (x -> x.algo()) con la que se rellenan sus opciones
        http
                .authorizeHttpRequests(peticiones -> peticiones
                        // Páginas públicas: cualquier visitante puede ver el catálogo (HU-08)
                        // ("/equipos/*" y "/jugadores/*" abarcan un solo nivel: la ficha, no sus subpáginas)
                        .requestMatchers("/", "/eventos", "/equipos", "/equipos/*", "/mercados", "/ranking", "/jugadores/*", "/registro", "/login", "/recuperar", "/recuperar/nueva",
                                "/css/**", "/error").permitAll()
                        // hasRole("CREADOR") comprueba que el usuario tenga el rol CREADOR (ver usuarios.Rol)
                        // Consola de la base de datos (solo existe en local): únicamente para el creador
                        .requestMatchers("/h2-console/**").hasRole("CREADOR")
                        // Panel del creador de apuestas
                        .requestMatchers("/gestion/**").hasRole("CREADOR")
                        // El resto (apostar, cuenta…) exige haber iniciado sesión
                        .anyRequest().authenticated())
                // Formulario propio en /login; el campo del usuario se llama email. Al entrar, RecargaAlEntrarHandler
                // aplica la recarga gratuita pendiente (HU-14) y vuelve a la página que se pedía
                .formLogin(login -> login
                        .loginPage("/login")
                        .usernameParameter("email")
                        .successHandler(alEntrar)
                        .permitAll())
                // Al cerrar sesión se vuelve a la portada
                .logout(logout -> logout.logoutSuccessUrl("/"))
                // Registro de sesiones: al eliminar la cuenta o cambiar la contraseña se cierran las de otros navegadores
                // (-1 = sin límite de sesiones por usuario; una sesión cerrada así lleva a /login?expirada)
                .sessionManagement(sesiones -> sesiones
                        .maximumSessions(-1)
                        .sessionRegistry(registroSesiones)
                        .expiredUrl("/login?expirada"))
                // CSRF activado: cada formulario POST lleva un token oculto (Thymeleaf lo añade solo), así otra web
                // no puede enviar formularios en nombre del usuario.
                // La consola de H2 (solo desarrollo) usa frames y no envía token CSRF
                .csrf(csrf -> csrf.ignoringRequestMatchers("/h2-console/**"))
                // Frames solo desde la propia web (los necesita la consola de H2); otras webs no pueden incrustarla
                .headers(cabeceras -> cabeceras.frameOptions(opciones -> opciones.sameOrigin()));
        // Construye la cadena de filtros con todas las reglas anteriores
        return http.build();
    }

    /** Sesiones abiertas de cada usuario; la usa {@code usuarios.Sesiones} para cerrar las de otros navegadores. */
    @Bean
    public SessionRegistry registroSesiones() {
        return new SessionRegistryImpl();
    }

    /** Avisa al registro de sesiones cuando una caduca o se cierra. */
    @Bean
    public HttpSessionEventPublisher publicadorSesiones() {
        return new HttpSessionEventPublisher();
    }

    /**
     * Las contraseñas se guardan con BCrypt: un hash con sal, nunca en claro (HU-11, HU-12).
     * Un hash es una transformación que no se puede deshacer: para comprobar una contraseña se vuelve a calcular
     * y se compara. La "sal" es un valor aleatorio que hace que dos contraseñas iguales den hashes distintos.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
