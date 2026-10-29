# API de gestión musical

API REST con Spring Boot, Spring Data JPA y H2 para administrar artistas, álbumes, canciones, usuarios y roles. La base relacional persiste en `./data/musica` al ejecutar la aplicación.

## Documentación y guía breve

- [Manual de uso: Postman y tokens JWT](docs/manual-postman-y-tokens.docx) (Word): paso a paso para ejecutar la API, obtener un token por usuario y probar cada controlador desde Postman y desde la terminal, con tabla de rutas, permisos, limitaciones y lista de comprobación.
- [Colección Postman](postman/proyecto-musica.postman_collection.json)
- [Estructura de la base de datos](docs/estructura-base-datos.md)

Inicio rápido (detalles en el manual):

1. Define `JWT_SECRET` (mínimo 32 bytes; consérvalo entre reinicios) y, opcionalmente, `BOOTSTRAP_ADMIN_USERNAME` y `BOOTSTRAP_ADMIN_PASSWORD`; luego ejecuta `./gradlew bootRun`.
2. Registra una cuenta: `POST /api/usuarios` con `username`, `email`, `nombre` y `contrasena`.
3. Obtén su token: `POST /api/auth/token` con `username` y `contrasena`, y úsalo como `Authorization: Bearer <TOKEN>` (dura una hora).
4. Desde otro equipo de la red usa `http://<IP_LOCAL>:8080` y permite el puerto TCP 8080 en el firewall.

## Requisitos

- Java 17
- Gradle Wrapper incluido en el proyecto

## Ejecutar y probar

Desde la carpeta raíz, define un secreto JWT de al menos 32 bytes y las credenciales del administrador inicial:

```bash
export JWT_SECRET="$(openssl rand -hex 32)"
export BOOTSTRAP_ADMIN_USERNAME=admin-local
export BOOTSTRAP_ADMIN_PASSWORD='Admin-local-fuerte-123'
./gradlew bootRun
```

La aplicación escucha en todas las interfaces de red (`0.0.0.0:8080`). En la misma computadora queda disponible en `http://localhost:8080`. Para ejecutar las pruebas:

```bash
./gradlew test
```

### Acceso desde otro equipo de la misma red

1. Conecta ambos equipos al mismo Wi-Fi o router.
2. En la computadora que ejecuta la API, consulta su IP local con `hostname -I` (Linux) o `ipconfig` (Windows).
3. En Postman del otro equipo, cambia `baseUrl` a `http://<IP_LOCAL>:8080`, por ejemplo `http://192.168.1.25:8080`.
4. Si no conecta, permite conexiones entrantes al puerto TCP `8080` en el firewall de la computadora que ejecuta la API.

Esto permite usar la IP (o el nombre local que anuncie el router); no registra un dominio público ni hace que el servicio sea accesible desde Internet. Para exposición pública se requiere dominio/DNS, HTTPS y configuración de red adicional. No publiques este servidor de desarrollo directamente en Internet.

### Postman y tokens

Importa `postman/proyecto-musica.postman_collection.json`. Registra primero el usuario normal; luego ejecuta **Obtener token de usuario**. La colección guarda el JWT en `accessToken` y lo envía como `Authorization: Bearer <token>` en las rutas protegidas. Los tokens duran una hora. Para las rutas de administrador, configura el usuario administrador inicial con las variables de entorno anteriores y ejecuta **Admin: obtener token**; el token se guarda aparte en `adminAccessToken`.

El endpoint para obtener un token es `POST /api/auth/token`, con JSON `{"username":"...","contrasena":"..."}`. Para usuarios se requiere primero `POST /api/usuarios`; el endpoint público asigna siempre el rol `USUARIO`. Solo una cuenta `ADMIN` puede crear administradores o consultar la lista completa de usuarios.

### Datos de prueba

El archivo [seed-musica.sql](src/main/resources/seed-musica.sql) inserta exactamente 5,000 filas: 500 artistas, 1,000 álbumes y 3,500 canciones relacionadas. Ejecútalo una sola vez y únicamente cuando esas tres tablas estén vacías:

1. Detén la aplicación con `Ctrl+C`.
2. Desde la raíz del proyecto, inicia temporalmente la consola H2. Conserva el mismo `JWT_SECRET` que usas normalmente:

   ```bash
   export H2_CONSOLE_ENABLED=true
   ./gradlew bootRun --args='--server.address=127.0.0.1'
   ```

   Si abriste una terminal nueva, define también `JWT_SECRET` con tu clave guardada antes de iniciar. El argumento limita temporalmente el servidor a la computadora anfitriona mientras la consola H2 está habilitada.
3. Abre `http://localhost:8080/h2-console`, conéctate con JDBC URL `jdbc:h2:file:./data/musica`, usuario `sa` y contraseña vacía.
4. Antes de importar, verifica que `artistas`, `albumes` y `canciones` tengan cero filas. Si ya hay datos, no ejecutes el archivo: los IDs podrían duplicarse.
5. En el campo de consulta de H2 Console ejecuta:

```sql
RUNSCRIPT FROM 'src/main/resources/seed-musica.sql';
```

6. Detén la aplicación con `Ctrl+C`, quita la variable y arranca normalmente:

   ```bash
   unset H2_CONSOLE_ENABLED
   ./gradlew bootRun
   ```

La consola H2 está deshabilitada por defecto. No la habilites cuando la computadora anfitriona esté en una red con equipos no confiables.

## Usuarios, roles y privacidad

La primera ejecución crea los roles `USUARIO` y `ADMIN`. La contraseña de administrador debe tener entre 12 y 72 bytes UTF-8. `JWT_SECRET` es obligatorio y se usa para firmar tokens; consérvalo fuera del repositorio y no lo cambies mientras los tokens emitidos sigan vigentes.

Un usuario puede consultar y actualizar `/api/usuarios/me` y su propio `/api/usuarios/{id}`; listar/eliminar otras cuentas requiere `ADMIN`. Los endpoints `GET /api/privacidad/politicas-seguridad` y `GET /api/privacidad/derechos-arco` exponen un aviso informativo sobre datos tratados, finalidades, conservación, medidas de seguridad y derechos de acceso, rectificación, cancelación y oposición. El proyecto no recibe ni resuelve solicitudes ARCO: el responsable del tratamiento debe habilitar y documentar un canal de contacto, definir el procedimiento y adaptar el aviso a la legislación aplicable. El contenido no constituye asesoría legal. Usa HTTPS en producción.

## Recursos REST

Todos aceptan y devuelven JSON. Las operaciones de creación responden `201 Created` y `Location`; las eliminaciones correctas responden `204 No Content`.

| Recurso | Rutas |
|---|---|
| Artistas | `GET/POST /api/artistas`, `GET/PUT/DELETE /api/artistas/{id}` |
| Álbumes | `GET/POST /api/albumes`, `GET/PUT/DELETE /api/albumes/{id}` |
| Canciones | `GET/POST /api/canciones`, `GET/PUT/DELETE /api/canciones/{id}` |
| Usuarios | `GET/POST /api/usuarios`, `GET/PUT/DELETE /api/usuarios/{id}` |
| Roles (solo ADMIN) | `GET/POST /api/roles`, `GET/PUT/DELETE /api/roles/{id}` |

`POST /api/usuarios` crea cuentas `USUARIO`; `POST /api/usuarios/admin` crea cuentas `ADMIN` y requiere un token de administrador. Un usuario puede leer y actualizar su propia cuenta; solo `ADMIN` puede listar y eliminar usuarios. Los roles del sistema son `USUARIO` y `ADMIN`; solo se puede eliminar un rol sin usuarios asignados. Los controladores auxiliares de autenticación y privacidad no son recursos CRUD.

Ejemplos de cuerpos:

```json
{"nombre":"Los Ejemplos","pais":"México","fechaFormacion":"2001-01-01"}
```

```json
{"titulo":"Primer álbum","fechaLanzamiento":"2024-01-01","genero":"Rock","artistaId":1}
```

```json
{"titulo":"Tema inicial","numeroPista":1,"duracionSegundos":210,"albumId":1}
```

Una referencia a un artista o álbum inexistente devuelve `404`; los datos inválidos devuelven `400`; no se permite borrar artistas con álbumes ni álbumes con canciones (`409`). El número de pista es único dentro de cada álbum. Canciones se borran primero para poder borrar después su álbum y artista.

## Estructura de datos

La estructura relacional y las claves están descritas en [docs/estructura-base-datos.md](docs/estructura-base-datos.md) y mapeadas en las entidades Java de `src/main/java/com/example/proyecto/musica/model`.
