# JA Joyería · ms-comunicaciones

API de contacto y suscripciones de la tienda. Los visitantes pueden enviar mensajes y suscribirse sin cuenta. Los administradores gestionan la bandeja y las bajas. El alcance actual es la recepción y gestión de datos. El envío de correos y campañas queda para una integración posterior.

## Ejecutar en local

Con Java 21, desde esta carpeta:

```powershell
.\mvnw.cmd clean verify
.\mvnw.cmd spring-boot:run
```

Puerto predeterminado 8084. El perfil local usa H2 persistente en data/comunicaciones. Liquibase crea las tablas mensajes_contacto y suscripciones en el esquema comunicaciones. Las migraciones viven en src/main/resources/db/changelog. Hibernate valida las tablas al iniciar. La aplicación sigue la organización controller, entity, repository, service, dto y config de los otros microservicios.

En desarrollo local, la documentación interactiva OpenAPI / Swagger UI está disponible en:
- **Swagger UI:** http://localhost:8084/swagger-ui.html
- **Especificación OpenAPI (JSON):** http://localhost:8084/v3/api-docs

## Formularios públicos

```powershell
$api = 'http://localhost:8084/v1'
Invoke-RestMethod -Method Post "$api/contacto" -ContentType 'application/json' -Body '{"nombre":"Ana Soto","correo":"ana@example.cl","mensaje":"Quisiera consultar por un anillo."}'
Invoke-RestMethod -Method Post "$api/suscripciones" -ContentType 'application/json' -Body '{"correo":"ana@example.cl"}'
```

Contacto devuelve 201. El nombre acepta letras y espacios con hasta 120 caracteres, el correo hasta 254 y el mensaje entre 1 y 2000. La suscripción devuelve un campo estado. ALTA crea el registro y devuelve 201. YA_ACTIVA conserva su fecha original y devuelve 200. REACTIVADA reutiliza el ID y actualiza la fecha de alta. El correo se guarda en minúsculas y sin espacios exteriores. La base impone unicidad incluso para solicitudes simultáneas.

## Administración

Enviar el access token de Cognito como Authorization: Bearer. La API comprueba firma, vigencia, emisor, tipo y cliente del JWT y consulta GET /v1/me en ms-auth. Deben coincidir la identidad del token y la del perfil, con rol ADMIN y estado ACTIVO.

| Método y ruta | Uso |
|---|---|
| GET /v1/admin/mensajes | Bandeja de más reciente a más antiguo. q busca en nombre, correo y contenido. lectura admite todos, leidos y sin-leer. |
| GET /v1/admin/mensajes/resumen | Cantidad total de mensajes y cantidad sin leer. |
| PATCH /v1/admin/mensajes/{id}/lectura | Enviar {"leido":true} o {"leido":false}. |
| DELETE /v1/admin/mensajes/{id} | Eliminar el mensaje. |
| GET /v1/admin/suscriptores | Suscriptores activos en orden de alta descendente. q busca por correo. |
| DELETE /v1/admin/suscriptores/{id} | Registrar la fecha de baja y retirar la suscripción de la lista activa. |

Ambos listados aceptan pagina (desde 0) y tamano (1 a 100, predeterminado 25). Las respuestas incluyen content y totalElements. Los formularios públicos funcionan con ms-comunicaciones solo. La administración requiere además Cognito y ms-auth.

## PostgreSQL y configuración

Los archivos application.properties, application-local.properties y application-postgres.properties definen los perfiles. Para el despliegue posterior, configurar:

| Variable | Uso |
|---|---|
| SPRING_PROFILES_ACTIVE | postgres para la conexión externa |
| DB_URL, DB_USERNAME, DB_PASSWORD | URL JDBC y credenciales de PostgreSQL |
| DB_POOL_SIZE | Máximo de conexiones por instancia, por defecto 4 |
| MS_AUTH_URL | URL base de ms-auth. En local se usa http://localhost:8081 |
| COGNITO_ISSUER, COGNITO_CLIENT_ID | Emisor y cliente web del User Pool |
| FRONTEND_ORIGIN, PORT | Origen CORS y puerto del servicio |

Para Supabase usar TLS en DB_URL. Con el pooler en modo transacción añadir prepareThreshold=0. Para ejecutar Liquibase con otra identidad o conexión directa, usar SPRING_LIQUIBASE_URL, SPRING_LIQUIBASE_USER y SPRING_LIQUIBASE_PASSWORD. La identidad de migración requiere crear esquema, tablas y secuencias. Otorgar los permisos de ejecución correspondientes en comunicaciones.

Las pruebas automatizadas usan H2 en memoria y un servidor HTTP que reproduce las respuestas de perfil de ms-auth.
