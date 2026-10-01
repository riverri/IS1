package es.ucm.fdi.is1.apuestas.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import es.ucm.fdi.is1.apuestas.usuarios.RecargaAlEntrarHandler;

@Configuration
public class SeguridadConfig {

    @Bean
    public SecurityFilterChain filtroSeguridad(HttpSecurity http, RecargaAlEntrarHandler alEntrar) throws Exception {
        http
                .authorizeHttpRequests(peticiones -> peticiones
                        // Páginas públicas: cualquier visitante puede ver el catálogo (HU-08)
                        .requestMatchers("/", "/eventos", "/equipos", "/equipos/*", "/mercados", "/ranking", "/registro", "/login",
                                "/css/**", "/error", "/h2-console/**").permitAll()
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
                // La consola de H2 (solo desarrollo) usa frames y no envía token CSRF
                .csrf(csrf -> csrf.ignoringRequestMatchers("/h2-console/**"))
                .headers(cabeceras -> cabeceras.frameOptions(opciones -> opciones.sameOrigin()));
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
