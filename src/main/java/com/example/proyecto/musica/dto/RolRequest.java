package com.example.proyecto.musica.dto;

import com.example.proyecto.musica.model.NombreRol;
import jakarta.validation.constraints.NotNull;

public record RolRequest(@NotNull NombreRol nombre) {
}
