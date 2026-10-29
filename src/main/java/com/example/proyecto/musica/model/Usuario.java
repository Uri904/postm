package com.example.proyecto.musica.model;

import jakarta.persistence.*;

@Entity
@Table(name = "usuarios")
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 40)
    private String username;

    @Column(nullable = false, unique = true, length = 254)
    private String email;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(name = "contrasena_hash", nullable = false, length = 100)
    private String contrasenaHash;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "rol_id", nullable = false)
    private Rol rol;

    protected Usuario() {
    }

    public Usuario(String username, String email, String nombre, String contrasenaHash, Rol rol) {
        this.username = username;
        this.email = email;
        this.nombre = nombre;
        this.contrasenaHash = contrasenaHash;
        this.rol = rol;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getNombre() {
        return nombre;
    }

    public String getContrasenaHash() {
        return contrasenaHash;
    }

    public Rol getRol() {
        return rol;
    }

    public void actualizar(String username, String email, String nombre, String contrasenaHash) {
        this.username = username;
        this.email = email;
        this.nombre = nombre;
        this.contrasenaHash = contrasenaHash;
    }
}
