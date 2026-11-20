# ms-comunicaciones

Recepción de mensajes y gestión de newsletter. Puerto `8084`, base MySQL `comunicaciones` y paquete `cl.jajoyeria.comunicaciones`.

## Datos y comportamiento

Liquibase crea `mensajes_contacto` y `suscripciones`. Los visitantes pueden enviar un mensaje o suscribirse sin cuenta. El correo se guarda en minúsculas y sin espacios exteriores.

Contacto admite nombres de letras y espacios de hasta 120 caracteres, correo de hasta 254 y mensajes de 1 a 2000. La bandeja registra la fecha de recepción y una fecha de lectura que puede quitarse para volver a marcar el mensaje como nuevo.

Las suscripciones tienen un correo único, fecha de alta y fecha de baja. Repetir un alta activa devuelve `YA_ACTIVA`; un correo dado de baja puede reactivarse con el mismo ID. La unicidad de MySQL evita duplicados concurrentes. El alcance actual es recibir y administrar los registros; el envío de campañas y respuestas por correo requiere una integración posterior.

## API

| Método y ruta | Operación |
| --- | --- |
| `POST /v1/contacto` | Recibir un mensaje, respuesta `201`. |
| `POST /v1/suscripciones` | Alta `201`, alta ya activa o reactivación `200`. |
| `GET /v1/admin/mensajes` | Bandeja paginada y búsqueda. |
| `GET /v1/admin/mensajes/resumen` | Total y cantidad sin leer. |
| `PATCH /v1/admin/mensajes/{id}/lectura` | Enviar `{"leido":true}` o `{"leido":false}`. |
| `DELETE /v1/admin/mensajes/{id}` | Eliminar el mensaje. |
| `GET /v1/admin/suscriptores` | Suscriptores activos. |
| `DELETE /v1/admin/suscriptores/{id}` | Registrar la baja. |

Los listados usan `pagina` desde cero y `tamano` entre 1 y 100, por defecto 25. `q` busca en nombre, correo o contenido de mensajes y en el correo de suscripciones. La bandeja admite `lectura=todos`, `leidos` o `sin-leer`. El orden es de más reciente a más antiguo. Las respuestas paginadas incluyen `content` y `totalElements`.

Ejemplos de cuerpos públicos:

```json
{"nombre":"Ana Soto","correo":"ana@example.cl","mensaje":"Quisiera consultar por un anillo."}
```

```json
{"correo":"ana@example.cl"}
```

Las rutas `/admin` exigen un JWT válido y un perfil `ADMIN` y `ACTIVO` consultado en auth. Un mensaje de contacto y una compra no crean por sí mismos una suscripción al newsletter.

## Ejecución

```powershell
.\mvnw.cmd spring-boot:run
.\mvnw.cmd clean verify
```

El perfil `mysql` usa `localhost:3307/comunicaciones`, usuario `ja`, contraseña `123123`. `MS_AUTH_URL` apunta por defecto a `http://localhost:8081`. Los formularios públicos pueden probarse con este servicio y MySQL; la administración requiere auth.

Consultar la [guía principal](../README.md) y [Swagger de comunicaciones](http://localhost:8084/swagger-ui/index.html), habilitado con `SWAGGER_ENABLED=true`.
