package com.example.proyecto.musica.dto;

import com.example.proyecto.musica.model.Album;

import java.time.LocalDate;

public record AlbumResponse(Long id, String titulo, LocalDate fechaLanzamiento, String genero,
                            Long artistaId, String artistaNombre) {
    public static AlbumResponse from(Album album) {
        return new AlbumResponse(album.getId(), album.getTitulo(), album.getFechaLanzamiento(),
                album.getGenero(), album.getArtista().getId(), album.getArtista().getNombre());
    }
}
