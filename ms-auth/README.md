# ms-auth

Registro, login, perfiles, roles y territorios de JA Joyería. Puerto `8081`, base MySQL `identidad` y paquete `cl.jajoyeria.auth`.

## Datos y autenticación

Liquibase crea `regiones`, `comunas` y `usuarios` y carga 3 regiones y 11 comunas. Usuarios incluye RUT y correo únicos, comuna, rol, estado, hash BCrypt y control de versión optimista. Los UUID se almacenan como `binary(16)`.

Las contraseñas del registro admiten entre 8 y 72 caracteres, con un máximo efectivo de 72 bytes para BCrypt. El RUT se valida con su dígito verificador. El correo se normaliza en minúsculas. Los tokens RSA contienen identidad, audiencia y vencimiento; la clave privada permanece en auth y la pública se publica como JWKS.

## API

| Método y ruta | Operación y acceso |
| --- | --- |
| `POST /v1/auth/registro` | Registro público de cliente y emisión de sesión. |
| `POST /v1/auth/login` | Login de cuenta activa. |
| `GET /v1/auth/jwks` | Clave pública para los demás servicios. |
| `GET /v1/regiones` | Regiones públicas. |
| `GET /v1/regiones/{id}/comunas` | Comunas de una región. |
| `GET /v1/me` | Perfil del titular activo del JWT. |
| `PUT /v1/me` | Cambiar nombre, teléfono y comuna del titular. |
| `GET /v1/admin/usuarios` | Lista paginada de cuentas, con `page` y `size`. Requiere administrador. |
| `POST /v1/admin/usuarios` | Crear una cuenta con rol y estado desde administración. |
| `PATCH /v1/admin/usuarios/{id}/acceso` | Modificar rol y estado como administrador. |

El controlador conserva `POST /v1/me` como punto de integración de perfiles de identidades externas. Su verificación de correo externo sigue pendiente; el registro local utiliza `/v1/auth/registro`.

Ejemplo de registro:

```json
{
  "correo": "ana@example.cl",
  "password": "Clave1234",
  "rut": "12345678-5",
  "nombre": "Ana Soto",
  "telefono": "912345678",
  "comunaId": 1
}
```

La respuesta contiene `accessToken`, `expiresIn` y `usuario`. Las rutas privadas reciben `Authorization: Bearer ...`. Para crear una cuenta como administrador, enviar `registro` con esos mismos campos más `rol` y `estado` en el objeto exterior. El administrador tiene restringida la retirada de su propio acceso mediante la operación de cambio de permisos.

## Ejecución

```powershell
.\mvnw.cmd spring-boot:run
.\mvnw.cmd clean verify
```

La conexión predeterminada es `jdbc:mysql://localhost:3307/identidad`, usuario `ja`, contraseña `123123`, con las opciones del perfil `mysql`. `LOCAL_ADMIN_ENABLED=true` habilita la cuenta de desarrollo. Sus claves RSA se generan en `data/keys` cuando faltan ambos archivos y la generación local está habilitada.

La [guía principal](../README.md) documenta Docker, variables, credenciales y persistencia. Con `SWAGGER_ENABLED=true`, abrir [Swagger de auth](http://localhost:8081/swagger-ui/index.html).
