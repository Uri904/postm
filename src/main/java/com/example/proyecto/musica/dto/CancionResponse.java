package com.example.proyecto.musica.dto;

import com.example.proyecto.musica.model.Cancion;

public record CancionResponse(Long id, String titulo, Integer numeroPista, Integer duracionSegundos,
                              Long albumId, String albumTitulo) {
    public static CancionResponse from(Cancion cancion) {
        return new CancionResponse(cancion.getId(), cancion.getTitulo(), cancion.getNumeroPista(),
                cancion.getDuracionSegundos(), cancion.getAlbum().getId(), cancion.getAlbum().getTitulo());
    }
}
