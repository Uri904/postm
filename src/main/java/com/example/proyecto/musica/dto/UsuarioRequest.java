package com.example.proyecto.musica.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UsuarioRequest(
        @NotBlank @Size(min = 3, max = 40) String username,
        @NotBlank @Email @Size(max = 254) String email,
        @NotBlank @Size(max = 100) String nombre,
        @NotBlank @Size(min = 12, max = 72) String contrasena
) {
}
