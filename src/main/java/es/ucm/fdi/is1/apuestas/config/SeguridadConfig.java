package es.ucm.fdi.is1.apuestas.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter.ReferrerPolicy;
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

    /**
     * Content-Security-Policy: de dónde puede cargar cosas la página. Si alguien consiguiera colar HTML en una
     * página (por ejemplo en el nombre de un equipo), el navegador no cargaría scripts ni estilos de otra web ni
     * enviaría formularios fuera. Los scripts y estilos van dentro de las plantillas (los avisos de confirmación,
     * el modo oscuro…), por eso se permite 'unsafe-inline'. Las imágenes pueden venir de fuera: escudos de la API
     * de fútbol, logotipos de criptomonedas y anuncios.
     */
    static final String CSP = "default-src 'self'; script-src 'self' 'unsafe-inline'; style-src 'self' 'unsafe-inline'; "
            + "img-src 'self' data: https:; font-src 'self' data:; connect-src 'self'; frame-ancestors 'self'; "
            + "object-src 'none'; base-uri 'self'; form-action 'self'";

    /** Permissions-Policy: la web no usa cámara, micrófono, ubicación ni pagos, así que se desactivan. */
    static final String PERMISOS = "camera=(), microphone=(), geolocation=(), payment=(), usb=()";

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
                                "/css/**", "/js/**", "/error").permitAll()
                        // Ping para que Render no apague la web por inactividad (HU-85): responde sin sesión
                        .requestMatchers(HttpMethod.GET, "/ping").permitAll()
                        // Modo oscuro (HU-94): también para los visitantes
                        .requestMatchers(HttpMethod.GET, "/tema").permitAll()
                        // Clic en un anuncio interno (HU-60): los visitantes también los ven
                        .requestMatchers("/anuncios/*/ir").permitAll()
                        // Mercados de predicción (HU-67): cualquiera ve la lista y cada mercado; comprar exige sesión
                        .requestMatchers(HttpMethod.GET, "/predicciones", "/predicciones/*").permitAll()
                        // Quiniela (HU-79): cualquiera ve las quinielas; echar un boleto exige sesión
                        .requestMatchers(HttpMethod.GET, "/quinielas", "/quinielas/*").permitAll()
                        // La clasificación global solo expone datos agregados de las ligas públicas (HU-112)
                        .requestMatchers(HttpMethod.GET, "/ligas/clasificacion").permitAll()
                        // Criptomonedas (HU-69): la lista y cada cripto son públicas; la cartera, comprar y vender no.
                        // Va primero la cartera porque "/cripto/*" también la abarcaría
                        .requestMatchers("/cripto/cartera").authenticated()
                        .requestMatchers(HttpMethod.GET, "/cripto", "/cripto/*").permitAll()
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
                // Al cerrar sesión se vuelve a la portada, con el aviso «Has cerrado sesión»
                .logout(logout -> logout.logoutSuccessUrl("/?logout"))
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
                // Frames solo desde la propia web (los necesita la consola de H2); otras webs no pueden incrustarla.
                // Además: la política de contenido (CSP), no mandar la dirección completa a otras webs al seguir un
                // enlace (Referrer-Policy) y desactivar permisos del navegador que no se usan
                .headers(cabeceras -> cabeceras
                        .frameOptions(opciones -> opciones.sameOrigin())
                        .contentSecurityPolicy(csp -> csp.policyDirectives(CSP))
                        .referrerPolicy(referrer -> referrer.policy(ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN))
                        .permissionsPolicyHeader(permisos -> permisos.policy(PERMISOS)));
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
