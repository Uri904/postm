package com.example.proyecto.musica.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/privacidad")
public class InformacionPrivacidadController {
    private final String contacto;

    public InformacionPrivacidadController(@Value("${app.privacy.contact-email:}") String contacto) {
        this.contacto = contacto;
    }

    @GetMapping("/politicas-seguridad")
    public PoliticaSeguridad politicasSeguridad() {
        return new PoliticaSeguridad(
                "Política informativa de privacidad y seguridad",
                "Aviso general del proyecto API de gestión musical. El responsable del despliegue debe identificar "
                        + "a la organización responsable, revisar este aviso y adaptarlo a la legislación aplicable "
                        + "antes de utilizarlo con datos reales.",
                List.of(
                        "Datos de cuenta: nombre de usuario, correo electrónico y nombre proporcionados al registrarse.",
                        "Credenciales: la contraseña se recibe para verificar el acceso y se conserva como hash BCrypt; "
                                + "la API no devuelve el hash.",
                        "Autorización: el token de acceso contiene el nombre de usuario, los roles y sus marcas de "
                                + "emisión y expiración; no contiene la contraseña.",
                        "Catálogo musical: nombres de artistas, país, fechas de formación, títulos de álbumes y canciones, "
                                + "géneros, fechas de lanzamiento y relaciones entre estos recursos."
                ),
                List.of(
                        "Crear y administrar cuentas y perfiles de usuario.",
                        "Autenticar a los usuarios y aplicar permisos según los roles USUARIO y ADMIN.",
                        "Emitir tokens temporales para permitir el acceso a las operaciones protegidas.",
                        "Administrar y consultar el catálogo musical y sus relaciones."
                ),
                List.of(
                        "Solicitar solo los datos necesarios para las funciones descritas y mantenerlos razonablemente "
                                + "actualizados.",
                        "No utilizar los datos de cuenta para publicidad ni finalidades incompatibles; el proyecto no "
                                + "implementa funciones publicitarias.",
                        "Limitar el acceso a perfiles: cada usuario puede consultar y actualizar su propia cuenta; las "
                                + "operaciones de administración requieren rol ADMIN.",
                        "No asignar el rol ADMIN mediante el registro público."
                ),
                List.of(
                        "Las contraseñas se protegen con BCrypt y no se incluyen en las respuestas de la API.",
                        "Los endpoints protegidos requieren tokens JWT firmados con HS256 y con expiración de una hora.",
                        "La clave JWT y las credenciales del administrador se configuran mediante variables de entorno; "
                                + "no deben guardarse en el repositorio.",
                        "El acceso de red de producción debe configurarse con HTTPS, control de acceso al servidor y "
                                + "protección de secretos.",
                        "La consola H2 se deshabilita por defecto y solo debe habilitarse temporalmente en un entorno "
                                + "local controlado."
                ),
                "El proyecto no aplica una política automática de vencimiento o borrado de cuentas. El responsable del "
                        + "despliegue debe definir cuánto tiempo conserva los datos y cómo elimina copias de seguridad "
                        + "conforme a sus necesidades y obligaciones.",
                "El proyecto no configura por sí mismo transferencias a proveedores externos. El responsable debe "
                        + "documentar cualquier servicio, alojamiento o proveedor añadido al despliegue.",
                contacto,
                "Este contenido es informativo, no constituye asesoría legal y no sustituye un aviso de privacidad "
                        + "revisado para la organización, jurisdicción y operación concretas."
        );
    }

    @GetMapping("/derechos-arco")
    public DerechosArco derechosArco() {
        return new DerechosArco("Derechos ARCO", List.of(
                new Derecho("Acceso", "Conocer qué datos personales se tienen y cómo se utilizan."),
                new Derecho("Rectificación", "Solicitar la corrección de datos personales inexactos o incompletos."),
                new Derecho("Cancelación", "Solicitar la eliminación o bloqueo de datos cuando proceda conforme a la "
                        + "legislación aplicable y a las obligaciones de conservación."),
                new Derecho("Oposición", "Solicitar que cese un tratamiento específico cuando la legislación aplicable "
                        + "lo permita.")
        ),
                "Para ejercer un derecho, contacta al responsable del tratamiento mediante el contacto indicado. "
                        + "La organización debe proporcionar un canal verificable y explicar los requisitos y plazos "
                        + "aplicables. No envíes contraseñas ni tokens.",
                "La organización puede necesitar información razonable para verificar la identidad y localizar los "
                        + "datos. Incluye el derecho que deseas ejercer, una descripción suficiente de la solicitud y "
                        + "un medio seguro para recibir respuesta; aporta documentos solo si son necesarios.",
                "Los requisitos, excepciones y plazos dependen de la jurisdicción y del responsable. La API no determina "
                        + "ni garantiza un plazo legal de respuesta.",
                contacto,
                "Este endpoint solo presenta información general: no recibe, registra, verifica ni resuelve solicitudes "
                        + "ARCO. La actualización del perfil permite cambiar ciertos campos de la cuenta; no sustituye "
                        + "un procedimiento formal para ejercer todos los derechos."
        );
    }

    public record PoliticaSeguridad(
            String titulo,
            String alcance,
            List<String> datosPersonales,
            List<String> finalidades,
            List<String> principios,
            List<String> medidas,
            String conservacion,
            String transferencias,
            String contactoPrivacidad,
            String nota
    ) {
    }

    public record DerechosArco(
            String titulo,
            List<Derecho> derechos,
            String comoEjercer,
            String informacionSolicitud,
            String plazoRespuesta,
            String contactoPrivacidad,
            String nota
    ) {
    }

    public record Derecho(String nombre, String descripcion) {
    }
}
