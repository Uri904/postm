package com.example.proyecto.musica.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;

@Entity
@Table(name = "artistas")
public class Artista {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nombre;

    @Column(nullable = false, length = 80)
    private String pais;

    @Column(name = "fecha_formacion")
    private LocalDate fechaFormacion;

    protected Artista() {
    }

    public Artista(String nombre, String pais, LocalDate fechaFormacion) {
        this.nombre = nombre;
        this.pais = pais;
        this.fechaFormacion = fechaFormacion;
    }

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getPais() {
        return pais;
    }

    public void setPais(String pais) {
        this.pais = pais;
    }

    public LocalDate getFechaFormacion() {
        return fechaFormacion;
    }

    public void setFechaFormacion(LocalDate fechaFormacion) {
        this.fechaFormacion = fechaFormacion;
    }
}
