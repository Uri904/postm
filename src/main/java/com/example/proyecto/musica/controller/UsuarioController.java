package com.example.proyecto.musica.controller;

import com.example.proyecto.musica.dto.UsuarioRequest;
import com.example.proyecto.musica.dto.UsuarioResponse;
import com.example.proyecto.musica.exception.RecursoNoEncontradoException;
import com.example.proyecto.musica.model.NombreRol;
import com.example.proyecto.musica.model.Rol;
import com.example.proyecto.musica.model.Usuario;
import com.example.proyecto.musica.repository.RolRepository;
import com.example.proyecto.musica.repository.UsuarioRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
@Transactional
public class UsuarioController {
    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioController(UsuarioRepository usuarioRepository, RolRepository rolRepository,
                             PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping
    public ResponseEntity<UsuarioResponse> registrar(@Valid @RequestBody UsuarioRequest request) {
        return crearUsuario(request, NombreRol.USUARIO);
    }

    @PostMapping("/admin")
    public ResponseEntity<UsuarioResponse> crearAdministrador(@Valid @RequestBody UsuarioRequest request) {
        return crearUsuario(request, NombreRol.ADMIN);
    }

    @GetMapping("/me")
    public UsuarioResponse miPerfil(Authentication authentication) {
        return UsuarioResponse.from(buscarPorUsername(authentication.getName()));
    }

    @GetMapping
    public List<UsuarioResponse> listar(Authentication authentication) {
        exigirAdmin(authentication);
        return usuarioRepository.findAll().stream().map(UsuarioResponse::from).toList();
    }

    @GetMapping("/{id}")
    public UsuarioResponse obtener(@PathVariable Long id, Authentication authentication) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario", id));
        if (!esAdmin(authentication) && !usuario.getUsername().equalsIgnoreCase(authentication.getName())) {
            throw new AccessDeniedException("No tienes permiso para consultar este usuario");
        }
        return UsuarioResponse.from(usuario);
    }

    @PutMapping("/{id}")
    public UsuarioResponse actualizar(@PathVariable Long id, @Valid @RequestBody UsuarioRequest request,
                                      Authentication authentication) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario", id));
        if (!esAdmin(authentication) && !usuario.getUsername().equalsIgnoreCase(authentication.getName())) {
            throw new AccessDeniedException("No tienes permiso para modificar este usuario");
        }
        validarContrasena(request.contrasena());
        String username = request.username().trim();
        String email = request.email().trim().toLowerCase();
        if (usuarioRepository.existsByUsernameIgnoreCaseAndIdNot(username, id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El nombre de usuario ya está registrado");
        }
        if (usuarioRepository.existsByEmailIgnoreCaseAndIdNot(email, id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El correo electrónico ya está registrado");
        }
        usuario.actualizar(username, email, request.nombre().trim(), passwordEncoder.encode(request.contrasena()));
        return UsuarioResponse.from(usuarioRepository.save(usuario));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id, Authentication authentication) {
        exigirAdmin(authentication);
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario", id));
        usuarioRepository.delete(usuario);
        return ResponseEntity.noContent().build();
    }

    private ResponseEntity<UsuarioResponse> crearUsuario(UsuarioRequest request, NombreRol nombreRol) {
        validarContrasena(request.contrasena());
        String username = request.username().trim();
        String email = request.email().trim().toLowerCase();
        if (usuarioRepository.existsByUsernameIgnoreCase(username)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El nombre de usuario ya está registrado");
        }
        if (usuarioRepository.existsByEmailIgnoreCase(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El correo electrónico ya está registrado");
        }
        Rol rol = rolRepository.findByNombre(nombreRol)
                .orElseThrow(() -> new IllegalStateException("No se encontró el rol " + nombreRol));
        Usuario usuario = usuarioRepository.save(new Usuario(username, email, request.nombre().trim(),
                passwordEncoder.encode(request.contrasena()), rol));
        URI ubicacion = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(usuario.getId()).toUri();
        return ResponseEntity.created(ubicacion).body(UsuarioResponse.from(usuario));
    }

    private void validarContrasena(String contrasena) {
        if (contrasena.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La contraseña no puede exceder 72 bytes en UTF-8");
        }
    }

    private Usuario buscarPorUsername(String username) {
        return usuarioRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
    }

    private boolean esAdmin(Authentication authentication) {
        return authentication.getAuthorities().stream().anyMatch(authority ->
                authority.getAuthority().equals("ROLE_ADMIN"));
    }

    private void exigirAdmin(Authentication authentication) {
        if (!esAdmin(authentication)) {
            throw new AccessDeniedException("Se requiere el rol ADMIN");
        }
    }
}
