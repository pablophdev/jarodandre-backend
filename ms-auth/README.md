# JA Joyería · ms-auth

Servicio de perfiles y autorización de la tienda. Java 21, Spring Boot, Maven Wrapper, Web, Validation, JPA, Security, Lombok y Liquibase.

## Ejecutar en Windows

Desde esta carpeta, con un JDK 21 y JAVA_HOME configurado:

```powershell
.\mvnw.cmd clean verify
.\mvnw.cmd spring-boot:run
```

El perfil predeterminado local crea una base H2 en data/identidad y aplica las migraciones. GET http://localhost:8081/v1/regiones permite comprobar el arranque. Sin configurar Cognito, las rutas privadas devuelven 401. Los tests ejecutan esas rutas con identidades de prueba en una base en memoria.

En desarrollo local, la documentación interactiva OpenAPI / Swagger UI está disponible en:
- **Swagger UI:** http://localhost:8081/swagger-ui.html
- **Especificación OpenAPI (JSON):** http://localhost:8081/v3/api-docs

## Estructura compartida

Bajo src/main/java/cl/jajoyeria/auth, controller expone HTTP, service aplica reglas y delimita transacciones, repository accede a JPA, entity mapea tablas, dto contiene records y config configura seguridad y CORS. En src/main/resources se encuentran application.properties, los archivos de perfil y db/changelog. Para evolucionar la base, agregar un changeset y referenciarlo desde el changelog maestro.

## Identidad y acceso

El cliente usará el inicio de sesión administrado de Cognito con Authorization Code + PKCE, un cliente público y los scopes openid y email. El registro de credenciales, login y recuperación de contraseña ocurren en Cognito. Tras verificar su correo, el cliente envía el access token como Bearer a esta API. Spring Security valida firma, emisor, vigencia, token_use y client_id. Para crear el perfil, el servicio consulta userInfo, comprueba el sub y toma el correo verificado del proveedor.

| Método y ruta | Uso |
|---|---|
| GET /v1/regiones | Regiones disponibles |
| GET /v1/regiones/{region}/comunas | Comunas del formulario |
| POST /v1/me | Crear perfil de cliente de la identidad autenticada |
| GET /v1/me | Leer perfil activo |
| PUT /v1/me | Actualizar nombre, teléfono y comuna |
| GET /v1/admin/usuarios?page=0&size=20 | Listar perfiles como administrador activo |
| PATCH /v1/admin/usuarios/{id}/acceso | Cambiar rol y estado como administrador activo |

Ejemplo de cuerpo para crear el perfil:

```json
{"rut":"12.345.678-5","nombre":"Ana Soto","telefono":"912345678","comunaId":1}
```

PUT usa los mismos campos excepto rut. Se puede omitir telefono. Para modificar acceso se envían rol (CLIENTE, EMPLEADO o ADMIN) y estado (ACTIVO, PENDIENTE o BLOQUEADO). El primer administrador se provisiona promoviendo explícitamente un perfil verificado por su UUID en identidad.usuarios. El bloqueo afecta las operaciones privadas de este servicio inmediatamente. Los futuros microservicios deberán comprobar el estado y los permisos de negocio para sus operaciones protegidas.

## Conexión posterior a PostgreSQL y Cognito

Activar SPRING_PROFILES_ACTIVE=postgres y definir las siguientes variables en el entorno del proceso.

| Variable | Valor esperado |
|---|---|
| DB_URL | URL JDBC PostgreSQL con host, puerto, base y TLS. Para Supavisor en modo transacción usar sslmode=require y prepareThreshold=0. |
| DB_USERNAME, DB_PASSWORD | Credenciales de la base |
| COGNITO_ISSUER | https://cognito-idp.REGION.amazonaws.com/USER_POOL_ID |
| COGNITO_CLIENT_ID | ID del cliente de la tienda |
| COGNITO_USERINFO_URL | https://DOMINIO-COGNITO/oauth2/userInfo |
| FRONTEND_ORIGIN | Origen HTTPS del frontend. En desarrollo se usa http://localhost:5173 |
| PORT | Puerto HTTP, por defecto 8081 |

Liquibase crea identidad.regiones, identidad.comunas e identidad.usuarios y carga las 3 regiones y 11 comunas del frontend. Hibernate valida el esquema en cada arranque. Al desplegar con permisos separados para migraciones se pueden configurar SPRING_LIQUIBASE_URL, SPRING_LIQUIBASE_USER y SPRING_LIQUIBASE_PASSWORD con una conexión directa o de sesión. Esa identidad necesita crear el esquema y las tablas de seguimiento de Liquibase; otorgar al usuario de ejecución acceso al esquema identidad y a sus tablas.
