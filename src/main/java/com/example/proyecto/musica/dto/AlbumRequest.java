package com.example.proyecto.musica.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record AlbumRequest(
        @NotBlank @Size(max = 150) String titulo,
        LocalDate fechaLanzamiento,
        @Size(max = 80) String genero,
        @NotNull @Positive Long artistaId
) {
}
