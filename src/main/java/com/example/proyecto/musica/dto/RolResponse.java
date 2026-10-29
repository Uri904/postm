package com.example.proyecto.musica.dto;

import com.example.proyecto.musica.model.Rol;

public record RolResponse(Long id, String nombre) {
    public static RolResponse from(Rol rol) {
        return new RolResponse(rol.getId(), rol.getNombre().name());
    }
}
