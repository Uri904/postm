package com.example.proyecto.musica.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CancionRequest(
        @NotBlank @Size(max = 150) String titulo,
        @NotNull @Positive Integer numeroPista,
        @NotNull @Positive Integer duracionSegundos,
        @NotNull @Positive Long albumId
) {
}
