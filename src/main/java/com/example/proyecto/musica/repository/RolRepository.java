package com.example.proyecto.musica.repository;

import com.example.proyecto.musica.model.NombreRol;
import com.example.proyecto.musica.model.Rol;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RolRepository extends JpaRepository<Rol, Long> {
    Optional<Rol> findByNombre(NombreRol nombre);
}
