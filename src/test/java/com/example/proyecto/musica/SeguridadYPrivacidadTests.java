package com.example.proyecto.musica;

import com.example.proyecto.musica.repository.RolRepository;
import com.example.proyecto.musica.repository.UsuarioRepository;
import com.example.proyecto.musica.model.NombreRol;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:seguridad_test;DB_CLOSE_DELAY=-1",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.h2.console.enabled=false",
        "app.security.bootstrap-admin.username=admin-test",
        "app.security.bootstrap-admin.password=admin-test-password-123",
        "app.privacy.contact-email=privacidad@example.com",
        "app.security.jwt.secret=clave-de-prueba-larga-con-mas-de-32-bytes"
})
@AutoConfigureMockMvc
class SeguridadYPrivacidadTests {
    private static final String ADMIN_USERNAME = "admin-test";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RolRepository rolRepository;

    @BeforeEach
    void limpiarUsuariosDePrueba() {
        usuarioRepository.findAll().stream()
                .filter(usuario -> !usuario.getUsername().equals(ADMIN_USERNAME))
                .forEach(usuarioRepository::delete);
    }

    @Test
    void usuarioSoloConsultaSuPerfilYAdminPuedeConsultarLosDemas() throws Exception {
        long anaId = registrarUsuario("ana", "ana@example.com");
        long luisId = registrarUsuario("luis", "luis@example.com");
        String tokenAna = bearerToken("ana", "Usuario-fuerte-123");
        String tokenAdmin = bearerToken(ADMIN_USERNAME, "admin-test-password-123");

        mockMvc.perform(get("/api/usuarios/me").header("Authorization", "Bearer " + tokenAna))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("ana"))
                .andExpect(jsonPath("$.rol").value("USUARIO"))
                .andExpect(jsonPath("$.contrasena").doesNotExist());

        mockMvc.perform(get("/api/usuarios/{id}", anaId).header("Authorization", "Bearer " + tokenAna))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(anaId));
        mockMvc.perform(get("/api/usuarios/{id}", luisId).header("Authorization", "Bearer " + tokenAna))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/usuarios").header("Authorization", "Bearer " + tokenAna))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/usuarios/{id}", luisId).header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("luis"));
        mockMvc.perform(get("/api/usuarios").header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.username == 'ana')]").exists());
    }

    @Test
    void registroPublicoNoPermiteAsignarseRolAdmin() throws Exception {
        mockMvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"nuevo","email":"nuevo@example.com","nombre":"Nuevo Usuario",
                                 "contrasena":"Usuario-fuerte-123","rol":"ADMIN"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.rol").value("USUARIO"))
                .andExpect(jsonPath("$.contrasena").doesNotExist());

        mockMvc.perform(post("/api/usuarios/admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"otro-admin","email":"otro-admin@example.com","nombre":"Otro Admin",
                                 "contrasena":"Usuario-fuerte-456"}
                                """))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/usuarios/admin")
                        .header("Authorization", "Bearer " + bearerToken(
                                ADMIN_USERNAME, "admin-test-password-123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"otro-admin","email":"otro-admin@example.com","nombre":"Otro Admin",
                                 "contrasena":"Usuario-fuerte-456"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.rol").value("ADMIN"));

        org.junit.jupiter.api.Assertions.assertTrue(rolRepository.count() >= 2);
    }

    @Test
    void rechazaContrasenaQueExcedeElLimiteDeBcrypt() throws Exception {
        String contrasena = "ñ".repeat(40);
        mockMvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"contrasena-larga","email":"larga@example.com","nombre":"Usuario",
                                 "contrasena":"%s"}
                                """.formatted(contrasena)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void requiereAutenticacionYExponeInformacionDePrivacidad() throws Exception {
        mockMvc.perform(get("/api/usuarios/me")).andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/privacidad/politicas-seguridad"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.medidas").isArray())
                .andExpect(jsonPath("$.datosPersonales.length()").value(4))
                .andExpect(jsonPath("$.finalidades.length()").value(4))
                .andExpect(jsonPath("$.conservacion").isNotEmpty())
                .andExpect(jsonPath("$.contactoPrivacidad").value("privacidad@example.com"));
        mockMvc.perform(get("/api/privacidad/derechos-arco"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.derechos[0].nombre").value("Acceso"))
                .andExpect(jsonPath("$.derechos[1].nombre").value("Rectificación"))
                .andExpect(jsonPath("$.derechos[2].nombre").value("Cancelación"))
                .andExpect(jsonPath("$.derechos[3].nombre").value("Oposición"))
                .andExpect(jsonPath("$.comoEjercer").isNotEmpty())
                .andExpect(jsonPath("$.informacionSolicitud").isNotEmpty())
                .andExpect(jsonPath("$.plazoRespuesta").isNotEmpty())
                .andExpect(jsonPath("$.contactoPrivacidad").value("privacidad@example.com"));
    }

    @Test
    void emiteTokenBearerConCredencialesValidasYRechazaCredencialesIncorrectas() throws Exception {
        registrarUsuario("token-user", "token-user@example.com");

        var tokenResponse = mockMvc.perform(post("/api/auth/token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"token-user","contrasena":"Usuario-fuerte-123"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").value(3600))
                .andReturn();

        String token = new tools.jackson.databind.ObjectMapper()
                .readTree(tokenResponse.getResponse().getContentAsString()).get("accessToken").asString();
        mockMvc.perform(get("/api/usuarios/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("token-user"));
        mockMvc.perform(post("/api/auth/token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"token-user","contrasena":"incorrecta"}
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void usuarioPuedeActualizarSuCuentaYAdminPuedeEliminarla() throws Exception {
        long usuarioId = registrarUsuario("editar", "editar@example.com");
        String tokenUsuario = bearerToken("editar", "Usuario-fuerte-123");
        String tokenAdmin = bearerToken(ADMIN_USERNAME, "admin-test-password-123");

        mockMvc.perform(put("/api/usuarios/{id}", usuarioId)
                        .header("Authorization", "Bearer " + tokenUsuario)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"editado","email":"editado@example.com","nombre":"Nombre Editado",
                                 "contrasena":"Usuario-fuerte-456"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("editado"))
                .andExpect(jsonPath("$.email").value("editado@example.com"))
                .andExpect(jsonPath("$.rol").value("USUARIO"));

        mockMvc.perform(delete("/api/usuarios/{id}", usuarioId)
                        .header("Authorization", "Bearer " + bearerToken(
                                "editado", "Usuario-fuerte-456")))
                .andExpect(status().isForbidden());
        long rolUsuarioId = rolRepository.findByNombre(NombreRol.USUARIO)
                .orElseThrow().getId();
        mockMvc.perform(delete("/api/roles/{id}", rolUsuarioId)
                        .header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isConflict());
        mockMvc.perform(delete("/api/usuarios/{id}", usuarioId)
                        .header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/usuarios/{id}", usuarioId)
                        .header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isNotFound());
    }

    @Test
    void adminPuedeConsultarCrearActualizarYEliminarRolesNoAsignados() throws Exception {
        String tokenAdminHeader = "Bearer " + bearerToken(ADMIN_USERNAME, "admin-test-password-123");
        long rolUsuarioId = rolRepository.findByNombre(NombreRol.USUARIO)
                .orElseThrow().getId();

        mockMvc.perform(get("/api/roles")
                        .header("Authorization", tokenAdminHeader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.nombre == 'USUARIO')]").exists())
                .andExpect(jsonPath("$[?(@.nombre == 'ADMIN')]").exists());
        mockMvc.perform(get("/api/roles/{id}", rolUsuarioId)
                        .header("Authorization", tokenAdminHeader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("USUARIO"));
        mockMvc.perform(get("/api/roles")).andExpect(status().isUnauthorized());
        mockMvc.perform(put("/api/roles/{id}", rolUsuarioId)
                        .header("Authorization", tokenAdminHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre":"USUARIO"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("USUARIO"));

        mockMvc.perform(delete("/api/roles/{id}", rolUsuarioId)
                        .header("Authorization", tokenAdminHeader))
                .andExpect(status().isNoContent());
        mockMvc.perform(post("/api/roles")
                        .header("Authorization", tokenAdminHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre":"USUARIO"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombre").value("USUARIO"));
        mockMvc.perform(post("/api/roles")
                        .header("Authorization", tokenAdminHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre":"USUARIO"}
                                """))
                .andExpect(status().isConflict());
    }

    private long registrarUsuario(String username, String email) throws Exception {
        var resultado = mockMvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"%s","email":"%s","nombre":"%s",
                                 "contrasena":"Usuario-fuerte-123"}
                                """.formatted(username, email, username)))
                .andExpect(status().isCreated())
                .andReturn();
        return new tools.jackson.databind.ObjectMapper()
                .readTree(resultado.getResponse().getContentAsString()).get("id").asLong();
    }

    private String bearerToken(String username, String password) throws Exception {
        var response = mockMvc.perform(post("/api/auth/token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"%s","contrasena":"%s"}
                                """.formatted(username, password)))
                .andExpect(status().isOk())
                .andReturn();
        return new tools.jackson.databind.ObjectMapper()
                .readTree(response.getResponse().getContentAsString()).get("accessToken").asString();
    }
}
