package com.example.proyecto.musica.security;

import com.example.proyecto.musica.model.NombreRol;
import com.example.proyecto.musica.model.Rol;
import com.example.proyecto.musica.model.Usuario;
import com.example.proyecto.musica.repository.RolRepository;
import com.example.proyecto.musica.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;

import java.nio.charset.StandardCharsets;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfiguration {
    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        JwtGrantedAuthoritiesConverter authoritiesConverter = new JwtGrantedAuthoritiesConverter();
        authoritiesConverter.setAuthoritiesClaimName("roles");
        authoritiesConverter.setAuthorityPrefix("");
        JwtAuthenticationConverter authenticationConverter = new JwtAuthenticationConverter();
        authenticationConverter.setJwtGrantedAuthoritiesConverter(authoritiesConverter);

        return http
                .csrf(csrf -> csrf.disable())
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/api/auth/token").permitAll()
                        .requestMatchers("/api/roles/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/usuarios").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/usuarios/admin").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/usuarios").hasRole("ADMIN")
                        .requestMatchers("/api/usuarios/**").authenticated()
                        .anyRequest().permitAll())
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt ->
                        jwt.jwtAuthenticationConverter(authenticationConverter)))
                .build();
    }

    @Bean
    CommandLineRunner inicializarRolesYAdministrador(
            RolRepository rolRepository,
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder,
            @Value("${app.security.bootstrap-admin.username:}") String adminUsername,
            @Value("${app.security.bootstrap-admin.password:}") String adminPassword) {
        return args -> {
            Rol usuarioRole = rolRepository.findByNombre(NombreRol.USUARIO)
                    .orElseGet(() -> rolRepository.save(new Rol(NombreRol.USUARIO)));
            Rol adminRole = rolRepository.findByNombre(NombreRol.ADMIN)
                    .orElseGet(() -> rolRepository.save(new Rol(NombreRol.ADMIN)));

            if (adminUsername.isBlank() && adminPassword.isBlank()) {
                return;
            }
            if (adminUsername.isBlank() || adminPassword.length() < 12
                    || adminPassword.getBytes(StandardCharsets.UTF_8).length > 72) {
                throw new IllegalStateException(
                        "Configura BOOTSTRAP_ADMIN_USERNAME y BOOTSTRAP_ADMIN_PASSWORD (entre 12 y 72 bytes UTF-8)");
            }

            var existente = usuarioRepository.findByUsernameIgnoreCase(adminUsername);
            if (existente.isPresent()) {
                if (existente.get().getRol().getNombre() != NombreRol.ADMIN) {
                    throw new IllegalStateException("El usuario configurado para bootstrap ya existe y no es admin");
                }
                return;
            }
            usuarioRepository.save(new Usuario(adminUsername.trim(), adminUsername.trim() + "@example.invalid",
                    "Administrador", passwordEncoder.encode(adminPassword), adminRole));
        };
    }
}
