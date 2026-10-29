package com.example.proyecto.musica.controller;

import com.example.proyecto.musica.dto.RolRequest;
import com.example.proyecto.musica.dto.RolResponse;
import com.example.proyecto.musica.exception.RecursoNoEncontradoException;
import com.example.proyecto.musica.exception.RelacionEnUsoException;
import com.example.proyecto.musica.model.Rol;
import com.example.proyecto.musica.repository.RolRepository;
import com.example.proyecto.musica.repository.UsuarioRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/roles")
public class RolController {
    private final RolRepository rolRepository;
    private final UsuarioRepository usuarioRepository;

    public RolController(RolRepository rolRepository, UsuarioRepository usuarioRepository) {
        this.rolRepository = rolRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping
    public List<RolResponse> listar(Authentication authentication) {
        exigirAdmin(authentication);
        return rolRepository.findAll().stream().map(RolResponse::from).toList();
    }

    @GetMapping("/{id}")
    public RolResponse obtener(@PathVariable Long id, Authentication authentication) {
        exigirAdmin(authentication);
        return RolResponse.from(buscarRol(id));
    }

    @PostMapping
    public ResponseEntity<RolResponse> crear(@Valid @RequestBody RolRequest request,
                                             Authentication authentication) {
        exigirAdmin(authentication);
        if (rolRepository.findByNombre(request.nombre()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El rol ya está registrado");
        }
        Rol guardado = rolRepository.save(new Rol(request.nombre()));
        URI ubicacion = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(guardado.getId()).toUri();
        return ResponseEntity.created(ubicacion).body(RolResponse.from(guardado));
    }

    @PutMapping("/{id}")
    public RolResponse actualizar(@PathVariable Long id, @Valid @RequestBody RolRequest request,
                                  Authentication authentication) {
        exigirAdmin(authentication);
        Rol rol = buscarRol(id);
        rolRepository.findByNombre(request.nombre())
                .filter(otro -> !otro.getId().equals(id))
                .ifPresent(otro -> {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "El rol ya está registrado");
                });
        rol.setNombre(request.nombre());
        return RolResponse.from(rolRepository.save(rol));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id, Authentication authentication) {
        exigirAdmin(authentication);
        Rol rol = buscarRol(id);
        if (usuarioRepository.existsByRol_Id(id)) {
            throw new RelacionEnUsoException("No se puede eliminar el rol porque tiene usuarios asignados");
        }
        rolRepository.delete(rol);
        return ResponseEntity.noContent().build();
    }

    private Rol buscarRol(Long id) {
        return rolRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Rol", id));
    }

    private void exigirAdmin(Authentication authentication) {
        boolean esAdmin = authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
        if (!esAdmin) {
            throw new AccessDeniedException("Se requiere el rol ADMIN");
        }
    }
}
