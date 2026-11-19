# JA Joyería · Backend

Microservicios Java 21 y Spring Boot 3.5.16 para la tienda de joyería.

| Servicio | Puerto | Esquema SQL |
|---|---|---|
| ms-auth | 8081 | identidad |
| ms-catalogo | 8082 | catalogo |
| ms-pedidos | 8083 | ventas |
| ms-comunicaciones | 8084 | comunicaciones |

## Estado

- No hay un proveedor de identidad configurado. Registro, login y administración permanecen bloqueados.
- No se incluye una base de datos embebida ni credenciales de una base remota.
- Se conservan las entidades, repositorios y migraciones de negocio.
- El frontend sigue consumiendo las APIs públicas y de invitado cuando los servicios tienen una base PostgreSQL configurada.

## Persistencia

La infraestructura SQL es genérica para PostgreSQL. Antes de iniciar un servicio, define `DB_URL` (JDBC), `DB_USERNAME` y `DB_PASSWORD` en su entorno. El perfil predeterminado es `postgres`.

No se crea ni se conecta automáticamente a una base de un proveedor externo.

```powershell
cd ms-pedidos # o ms-auth, ms-catalogo, ms-comunicaciones
.\mvnw.cmd spring-boot:run
```

## Comunicación y seguridad

- Las rutas de usuarios y administración exigen autenticación y rechazan todos los tokens mientras no se configure un proveedor.
- Catálogo y pedidos comparten `CATALOGO_INTERNAL_TOKEN` para las cotizaciones internas. Nunca se envía al navegador.
- `MS_AUTH_URL` y `MS_CATALOGO_URL` permiten configurar las direcciones entre servicios.
- CORS admite únicamente `FRONTEND_ORIGIN` (predeterminado `http://localhost:5173`).
- `PAGO_SIMULADO=true` habilita pagos de demostración sin cobros reales.
- `SWAGGER_ENABLED=true` habilita `/swagger-ui/index.html` y `/v3/api-docs`.

## Pruebas

```powershell
.\mvnw.cmd clean test
```

Las pruebas SQL utilizan PostgreSQL efímero con Testcontainers y requieren Docker activo. Si no está disponible, se omiten explícitamente. Las pruebas unitarias no necesitan Docker.
