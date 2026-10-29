package com.example.proyecto.musica.controller;

import com.example.proyecto.musica.dto.AlbumRequest;
import com.example.proyecto.musica.dto.AlbumResponse;
import com.example.proyecto.musica.exception.RelacionEnUsoException;
import com.example.proyecto.musica.exception.RecursoNoEncontradoException;
import com.example.proyecto.musica.model.Album;
import com.example.proyecto.musica.model.Artista;
import com.example.proyecto.musica.repository.AlbumRepository;
import com.example.proyecto.musica.repository.ArtistaRepository;
import com.example.proyecto.musica.repository.CancionRepository;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/albumes")
@Transactional
public class AlbumController {
    private final AlbumRepository albumRepository;
    private final ArtistaRepository artistaRepository;
    private final CancionRepository cancionRepository;

    public AlbumController(AlbumRepository albumRepository, ArtistaRepository artistaRepository,
                           CancionRepository cancionRepository) {
        this.albumRepository = albumRepository;
        this.artistaRepository = artistaRepository;
        this.cancionRepository = cancionRepository;
    }

    @GetMapping
    public List<AlbumResponse> listar() {
        return albumRepository.findAll().stream().map(AlbumResponse::from).toList();
    }

    @GetMapping("/{id}")
    public AlbumResponse obtener(@PathVariable Long id) {
        return AlbumResponse.from(buscarAlbum(id));
    }

    @PostMapping
    public ResponseEntity<AlbumResponse> crear(@Valid @RequestBody AlbumRequest request) {
        Artista artista = buscarArtista(request.artistaId());
        Album guardado = albumRepository.save(new Album(request.titulo().trim(), request.fechaLanzamiento(),
                limpiar(request.genero()), artista));
        URI ubicacion = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(guardado.getId()).toUri();
        return ResponseEntity.created(ubicacion).body(AlbumResponse.from(guardado));
    }

    @PutMapping("/{id}")
    public AlbumResponse actualizar(@PathVariable Long id, @Valid @RequestBody AlbumRequest request) {
        Album album = buscarAlbum(id);
        album.setTitulo(request.titulo().trim());
        album.setFechaLanzamiento(request.fechaLanzamiento());
        album.setGenero(limpiar(request.genero()));
        album.setArtista(buscarArtista(request.artistaId()));
        return AlbumResponse.from(albumRepository.save(album));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        Album album = buscarAlbum(id);
        if (cancionRepository.existsByAlbum_Id(id)) {
            throw new RelacionEnUsoException("No se puede eliminar el álbum porque tiene canciones asociadas");
        }
        albumRepository.delete(album);
        return ResponseEntity.noContent().build();
    }

    private Album buscarAlbum(Long id) {
        return albumRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Álbum", id));
    }

    private Artista buscarArtista(Long id) {
        return artistaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Artista", id));
    }

    private String limpiar(String valor) {
        return valor == null ? null : valor.trim();
    }
}
