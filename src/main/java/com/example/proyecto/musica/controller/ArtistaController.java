package com.example.proyecto.musica.controller;

import com.example.proyecto.musica.dto.ArtistaRequest;
import com.example.proyecto.musica.dto.ArtistaResponse;
import com.example.proyecto.musica.exception.RelacionEnUsoException;
import com.example.proyecto.musica.exception.RecursoNoEncontradoException;
import com.example.proyecto.musica.model.Artista;
import com.example.proyecto.musica.repository.AlbumRepository;
import com.example.proyecto.musica.repository.ArtistaRepository;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/artistas")
@Transactional
public class ArtistaController {
    private final ArtistaRepository artistaRepository;
    private final AlbumRepository albumRepository;

    public ArtistaController(ArtistaRepository artistaRepository, AlbumRepository albumRepository) {
        this.artistaRepository = artistaRepository;
        this.albumRepository = albumRepository;
    }

    @GetMapping
    public List<ArtistaResponse> listar() {
        return artistaRepository.findAll().stream().map(ArtistaResponse::from).toList();
    }

    @GetMapping("/{id}")
    public ArtistaResponse obtener(@PathVariable Long id) {
        return ArtistaResponse.from(buscarArtista(id));
    }

    @PostMapping
    public ResponseEntity<ArtistaResponse> crear(@Valid @RequestBody ArtistaRequest request) {
        Artista guardado = artistaRepository.save(
                new Artista(request.nombre().trim(), request.pais().trim(), request.fechaFormacion()));
        URI ubicacion = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(guardado.getId()).toUri();
        return ResponseEntity.created(ubicacion).body(ArtistaResponse.from(guardado));
    }

    @PutMapping("/{id}")
    public ArtistaResponse actualizar(@PathVariable Long id, @Valid @RequestBody ArtistaRequest request) {
        Artista artista = buscarArtista(id);
        artista.setNombre(request.nombre().trim());
        artista.setPais(request.pais().trim());
        artista.setFechaFormacion(request.fechaFormacion());
        return ArtistaResponse.from(artistaRepository.save(artista));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        Artista artista = buscarArtista(id);
        if (albumRepository.existsByArtista_Id(id)) {
            throw new RelacionEnUsoException("No se puede eliminar el artista porque tiene álbumes asociados");
        }
        artistaRepository.delete(artista);
        return ResponseEntity.noContent().build();
    }

    private Artista buscarArtista(Long id) {
        return artistaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Artista", id));
    }
}
