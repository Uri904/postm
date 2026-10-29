package com.example.proyecto.musica.repository;

import com.example.proyecto.musica.model.Artista;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ArtistaRepository extends JpaRepository<Artista, Long> {
}
