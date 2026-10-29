package com.example.proyecto.musica.dto;

import com.example.proyecto.musica.model.Usuario;

public record UsuarioResponse(Long id, String username, String email, String nombre, String rol) {
    public static UsuarioResponse from(Usuario usuario) {
        return new UsuarioResponse(usuario.getId(), usuario.getUsername(), usuario.getEmail(),
                usuario.getNombre(), usuario.getRol().getNombre().name());
    }
}
