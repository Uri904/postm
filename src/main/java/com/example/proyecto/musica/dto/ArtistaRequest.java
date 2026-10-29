package com.example.proyecto.musica.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record ArtistaRequest(
        @NotBlank @Size(max = 120) String nombre,
        @NotBlank @Size(max = 80) String pais,
        LocalDate fechaFormacion
) {
}
