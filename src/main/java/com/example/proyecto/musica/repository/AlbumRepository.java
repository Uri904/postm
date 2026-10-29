package com.example.proyecto.musica.repository;

import com.example.proyecto.musica.model.Album;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlbumRepository extends JpaRepository<Album, Long> {
    boolean existsByArtista_Id(Long artistaId);
}
