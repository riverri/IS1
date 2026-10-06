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

@Configuration
public class SeguridadConfig {

    @Bean
    public SecurityFilterChain filtroSeguridad(HttpSecurity http, RecargaAlEntrarHandler alEntrar,
                                               SessionRegistry registroSesiones) throws Exception {
        http
                .authorizeHttpRequests(peticiones -> peticiones
                        // Páginas públicas: cualquier visitante puede ver el catálogo (HU-08)
                        .requestMatchers("/", "/eventos", "/equipos", "/equipos/*", "/mercados", "/ranking", "/jugadores/*", "/registro", "/login", "/recuperar", "/recuperar/nueva",
                                "/css/**", "/error").permitAll()
                        // Consola de la base de datos (solo existe en local): únicamente para el creador
                        .requestMatchers("/h2-console/**").hasRole("CREADOR")
                        // Panel del creador de apuestas
                        .requestMatchers("/gestion/**").hasRole("CREADOR")
                        // El resto (apostar, cuenta…) exige haber iniciado sesión
                        .anyRequest().authenticated())
                .formLogin(login -> login
                        .loginPage("/login")
                        .usernameParameter("email")
                        .successHandler(alEntrar)
                        .permitAll())
                .logout(logout -> logout.logoutSuccessUrl("/"))
                // Registro de sesiones: al eliminar la cuenta o cambiar la contraseña se cierran las de otros navegadores
                .sessionManagement(sesiones -> sesiones
                        .maximumSessions(-1)
                        .sessionRegistry(registroSesiones)
                        .expiredUrl("/login?expirada"))
                // La consola de H2 (solo desarrollo) usa frames y no envía token CSRF
                .csrf(csrf -> csrf.ignoringRequestMatchers("/h2-console/**"))
                .headers(cabeceras -> cabeceras.frameOptions(opciones -> opciones.sameOrigin()));
        return http.build();
    }

    @Bean
    public SessionRegistry registroSesiones() {
        return new SessionRegistryImpl();
    }

    /** Avisa al registro de sesiones cuando una caduca o se cierra. */
    @Bean
    public HttpSessionEventPublisher publicadorSesiones() {
        return new HttpSessionEventPublisher();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
