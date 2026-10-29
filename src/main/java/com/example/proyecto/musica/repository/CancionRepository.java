package com.example.proyecto.musica.repository;

import com.example.proyecto.musica.model.Cancion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CancionRepository extends JpaRepository<Cancion, Long> {
    boolean existsByAlbum_Id(Long albumId);
}
