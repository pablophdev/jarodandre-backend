# JA Joyería · Backend

Backend distribuido para la tienda de comercio electrónico **JA Joyería**, desarrollado en **Java 21 LTS** y **Spring Boot 3.5.16**.

## Arquitectura de Microservicios

| Microservicio | Puerto | Descripción | Base de Datos | Documentación Interactiva (Local) |
|---|---|---|---|---|
| **[ms-auth](file:///C:/Users/Pablo/Desktop/jarodandre-backend/ms-auth)** | `8081` | Identidad, perfiles, validación de RUT chileno, regiones y comunas, control de roles y estados. | H2 (`data/identidad`) / PostgreSQL (`identidad`) | Rutas base `/v1/regiones`, `/v1/me` |
| **[ms-catalogo](file:///C:/Users/Pablo/Desktop/jarodandre-backend/ms-catalogo)** | `8082` | Catálogo de joyas, categorías, blog editorial, recursos multimedia (S3/disco) y cotizaciones por lote. | H2 (`data/catalogo`) / PostgreSQL (`catalogo`) | [Swagger UI](http://localhost:8082/swagger-ui.html) · [OpenAPI JSON](http://localhost:8082/v3/api-docs) |
| **[ms-pedidos](file:///C:/Users/Pablo/Desktop/jarodandre-backend/ms-pedidos)** | `8083` | Carritos anónimos y de usuario, checkout idempotente, pagos simulados, comprobantes BOL y reportes. | H2 (`data/ventas`) / PostgreSQL (`ventas`) | Rutas base `/v1/carrito`, `/v1/checkout` |
| **[ms-comunicaciones](file:///C:/Users/Pablo/Desktop/jarodandre-backend/ms-comunicaciones)** | `8084` | Formulario de contacto público, suscripciones a newsletter y bandeja de entrada administrativa. | H2 (`data/comunicaciones`) / PostgreSQL (`comunicaciones`) | [Swagger UI](http://localhost:8084/swagger-ui.html) · [OpenAPI JSON](http://localhost:8084/v3/api-docs) |

---

## Ejecución en Desarrollo Local

Cada microservicio cuenta con su propio **Maven Wrapper** (`mvnw.cmd` en Windows / `mvnw` en Linux/macOS) y perfil `local` predeterminado con base de datos H2 embebida persistente en `./data/`.

Para ejecutar un servicio:

```powershell
cd ms-catalogo        # O ms-auth, ms-pedidos, ms-comunicaciones
.\mvnw.cmd spring-boot:run
```

Para ejecutar las pruebas automatizadas:

```powershell
cd ms-catalogo
.\mvnw.cmd test
```

---

## Documentación Swagger / OpenAPI

Los microservicios documentados exponen la interfaz interactiva de Swagger UI durante el desarrollo local:

- **ms-catalogo:** [http://localhost:8082/swagger-ui.html](http://localhost:8082/swagger-ui.html) (definición JSON en `/v3/api-docs`)
- **ms-comunicaciones:** [http://localhost:8084/swagger-ui.html](http://localhost:8084/swagger-ui.html) (definición JSON en `/v3/api-docs`)

### Autenticación en Swagger UI
En la interfaz visual, puedes hacer clic en el botón verde **"Authorize"** e ingresar un token Bearer JWT para probar endpoints administrativos protegidos (`/v1/admin/**`).

---

## Seguridad e Integración con AWS Cognito

- **Usuarios y Administradores:** Los clientes y administradores inician sesión mediante el Hosted UI de AWS Cognito (OAuth 2.0 Authorization Code + PKCE). El frontend envía el Bearer JWT a las APIs.
- **Comunicación M2M:** La cotización de precios entre `ms-pedidos` y `ms-catalogo` utiliza OAuth 2.0 Client Credentials con el scope `catalogo/cotizar`.
