package com.example.proyecto.musica.dto;

import jakarta.validation.constraints.NotBlank;

public record TokenRequest(
        @NotBlank String username,
        @NotBlank String contrasena
) {
}
