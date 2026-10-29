package com.example.proyecto.musica.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "canciones", uniqueConstraints = {
        @UniqueConstraint(name = "uk_cancion_album_pista", columnNames = {"album_id", "numero_pista"})
})
public class Cancion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String titulo;

    @Column(name = "numero_pista", nullable = false)
    private Integer numeroPista;

    @Column(name = "duracion_segundos", nullable = false)
    private Integer duracionSegundos;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "album_id", nullable = false)
    private Album album;

    protected Cancion() {
    }

    public Cancion(String titulo, Integer numeroPista, Integer duracionSegundos, Album album) {
        this.titulo = titulo;
        this.numeroPista = numeroPista;
        this.duracionSegundos = duracionSegundos;
        this.album = album;
    }

    public Long getId() {
        return id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public Integer getNumeroPista() {
        return numeroPista;
    }

    public void setNumeroPista(Integer numeroPista) {
        this.numeroPista = numeroPista;
    }

    public Integer getDuracionSegundos() {
        return duracionSegundos;
    }

    public void setDuracionSegundos(Integer duracionSegundos) {
        this.duracionSegundos = duracionSegundos;
    }

    public Album getAlbum() {
        return album;
    }

    public void setAlbum(Album album) {
        this.album = album;
    }
}
