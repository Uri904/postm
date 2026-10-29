package com.example.proyecto.musica;

import com.example.proyecto.musica.repository.AlbumRepository;
import com.example.proyecto.musica.repository.ArtistaRepository;
import com.example.proyecto.musica.repository.CancionRepository;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:musica_test;DB_CLOSE_DELAY=-1",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.h2.console.enabled=false",
        "app.security.jwt.secret=clave-de-prueba-larga-con-mas-de-32-bytes"
})
@AutoConfigureMockMvc
class ProyectoMusicaApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ArtistaRepository artistaRepository;

    @Autowired
    private AlbumRepository albumRepository;

    @Autowired
    private CancionRepository cancionRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void limpiarBaseDatos() {
        cancionRepository.deleteAll();
        albumRepository.deleteAll();
        artistaRepository.deleteAll();
    }

    @Test
    void realizaCrudCompletoYRespetaLasRelaciones() throws Exception {
        MvcResult artistaCreado = mockMvc.perform(post("/api/artistas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre":"Los Ejemplos","pais":"México","fechaFormacion":"2001-01-01"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombre").value("Los Ejemplos"))
                .andReturn();
        long artistaId = idDe(artistaCreado);

        mockMvc.perform(get("/api/artistas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(artistaId));
        mockMvc.perform(get("/api/artistas/{id}", artistaId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(artistaId));

        mockMvc.perform(put("/api/artistas/{id}", artistaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre":"Los Ejemplos Actualizados","pais":"Chile","fechaFormacion":"2001-01-01"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Los Ejemplos Actualizados"));

        mockMvc.perform(post("/api/albumes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"titulo":"Álbum inválido","artistaId":9999}
                                """))
                .andExpect(status().isNotFound());

        MvcResult albumCreado = mockMvc.perform(post("/api/albumes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"titulo":"Primer álbum","fechaLanzamiento":"2024-01-01","genero":"Rock","artistaId":%d}
                                """.formatted(artistaId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.artistaId").value(artistaId))
                .andReturn();
        long albumId = idDe(albumCreado);
        mockMvc.perform(get("/api/albumes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(albumId));
        mockMvc.perform(get("/api/albumes/{id}", albumId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.artistaId").value(artistaId));
        mockMvc.perform(put("/api/albumes/{id}", albumId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"titulo":"Álbum inválido","artistaId":9999}
                                """))
                .andExpect(status().isNotFound());

        MvcResult cancionCreada = mockMvc.perform(post("/api/canciones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"titulo":"Tema inicial","numeroPista":1,"duracionSegundos":210,"albumId":%d}
                                """.formatted(albumId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.albumId").value(albumId))
                .andReturn();
        long cancionId = idDe(cancionCreada);
        mockMvc.perform(get("/api/canciones"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(cancionId));

        mockMvc.perform(get("/api/canciones/{id}", cancionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo").value("Tema inicial"));

        mockMvc.perform(put("/api/canciones/{id}", cancionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"titulo":"Tema editado","numeroPista":1,"duracionSegundos":215,"albumId":%d}
                                """.formatted(albumId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo").value("Tema editado"));

        mockMvc.perform(delete("/api/artistas/{id}", artistaId))
                .andExpect(status().isConflict());
        mockMvc.perform(delete("/api/albumes/{id}", albumId))
                .andExpect(status().isConflict());

        mockMvc.perform(delete("/api/canciones/{id}", cancionId))
                .andExpect(status().isNoContent());
        mockMvc.perform(delete("/api/albumes/{id}", albumId))
                .andExpect(status().isNoContent());
        mockMvc.perform(delete("/api/artistas/{id}", artistaId))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/artistas/{id}", artistaId))
                .andExpect(status().isNotFound());
    }

    @Test
    void rechazaDatosInvalidosYPistasDuplicadas() throws Exception {
        mockMvc.perform(post("/api/artistas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre":" ","pais":"México"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.nombre").exists());

        long artistaId = idDe(mockMvc.perform(post("/api/artistas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre":"Artista","pais":"México"}
                                """))
                .andExpect(status().isCreated())
                .andReturn());
        long albumId = idDe(mockMvc.perform(post("/api/albumes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"titulo":"Disco","artistaId":%d}
                                """.formatted(artistaId)))
                .andExpect(status().isCreated())
                .andReturn());
        String cancion = """
                {"titulo":"Canción","numeroPista":1,"duracionSegundos":180,"albumId":%d}
                """.formatted(albumId);
        mockMvc.perform(post("/api/canciones").contentType(MediaType.APPLICATION_JSON).content(cancion))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/api/canciones").contentType(MediaType.APPLICATION_JSON).content(cancion))
                .andExpect(status().isConflict());
    }

    private long idDe(MvcResult resultado) throws Exception {
        JsonNode json = objectMapper.readTree(resultado.getResponse().getContentAsString());
        return json.get("id").asLong();
    }
}
