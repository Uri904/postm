package com.example.proyecto.musica.controller;

import com.example.proyecto.musica.dto.CancionRequest;
import com.example.proyecto.musica.dto.CancionResponse;
import com.example.proyecto.musica.exception.RecursoNoEncontradoException;
import com.example.proyecto.musica.model.Album;
import com.example.proyecto.musica.model.Cancion;
import com.example.proyecto.musica.repository.AlbumRepository;
import com.example.proyecto.musica.repository.CancionRepository;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/canciones")
@Transactional
public class CancionController {
    private final CancionRepository cancionRepository;
    private final AlbumRepository albumRepository;

    public CancionController(CancionRepository cancionRepository, AlbumRepository albumRepository) {
        this.cancionRepository = cancionRepository;
        this.albumRepository = albumRepository;
    }

    @GetMapping
    public List<CancionResponse> listar() {
        return cancionRepository.findAll().stream().map(CancionResponse::from).toList();
    }

    @GetMapping("/{id}")
    public CancionResponse obtener(@PathVariable Long id) {
        return CancionResponse.from(buscarCancion(id));
    }

    @PostMapping
    public ResponseEntity<CancionResponse> crear(@Valid @RequestBody CancionRequest request) {
        Album album = buscarAlbum(request.albumId());
        Cancion guardada = cancionRepository.save(new Cancion(request.titulo().trim(), request.numeroPista(),
                request.duracionSegundos(), album));
        URI ubicacion = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(guardada.getId()).toUri();
        return ResponseEntity.created(ubicacion).body(CancionResponse.from(guardada));
    }

    @PutMapping("/{id}")
    public CancionResponse actualizar(@PathVariable Long id, @Valid @RequestBody CancionRequest request) {
        Cancion cancion = buscarCancion(id);
        cancion.setTitulo(request.titulo().trim());
        cancion.setNumeroPista(request.numeroPista());
        cancion.setDuracionSegundos(request.duracionSegundos());
        cancion.setAlbum(buscarAlbum(request.albumId()));
        return CancionResponse.from(cancionRepository.save(cancion));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        cancionRepository.delete(buscarCancion(id));
        return ResponseEntity.noContent().build();
    }

    private Cancion buscarCancion(Long id) {
        return cancionRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Canción", id));
    }

    private Album buscarAlbum(Long id) {
        return albumRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Álbum", id));
    }
}
