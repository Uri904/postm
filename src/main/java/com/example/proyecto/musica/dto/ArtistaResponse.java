package com.example.proyecto.musica.dto;

import com.example.proyecto.musica.model.Artista;

import java.time.LocalDate;

public record ArtistaResponse(Long id, String nombre, String pais, LocalDate fechaFormacion) {
    public static ArtistaResponse from(Artista artista) {
        return new ArtistaResponse(artista.getId(), artista.getNombre(), artista.getPais(),
                artista.getFechaFormacion());
    }
}
