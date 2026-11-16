# JA Joyería · ms-catalogo

API de productos, categorías, imágenes y publicaciones de la tienda. Usa Java 21, Spring Boot 3.5, Spring Web, Validation, Spring Data JPA, Spring Security, Lombok, Liquibase y Maven Wrapper.

## Inicio local

Desde la carpeta del proyecto, con Java 21:

```powershell
.\mvnw.cmd clean verify
.\mvnw.cmd spring-boot:run
```

El puerto predeterminado es 8082. Abrir http://localhost:8082/v1/productos/3 o http://localhost:8082/v1/publicaciones. El primer arranque crea la base H2 persistente en data/catalogo. Liquibase carga 4 categorías, 14 productos, 9 artículos y conserva los IDs, slugs y fechas editoriales del frontend. Los 23 archivos JPG originales se incluyen como recursos para servir la tienda local. Las imágenes se consultan mediante /media/ seguido de imagenKey o portadaKey.

## Estructura

controller expone HTTP, service implementa las operaciones y transacciones, entity mapea el esquema catalogo, repository encapsula las consultas, dto define los contratos como records y config aplica autorización y CORS. application.properties contiene los valores compartidos. Los perfiles local y postgres tienen sus propios archivos .properties. Las migraciones están en src/main/resources/db/changelog. Hibernate valida las tablas al arrancar.

## API

| Método y ruta | Operación |
|---|---|
| GET /v1/categorias | Categorías para los filtros |
| GET /v1/productos | Productos visibles paginados |
| GET /v1/productos/{id} | Ficha actual de una pieza |
| GET /v1/publicaciones | Artículos publicados, del más reciente al más antiguo |
| GET /v1/publicaciones/{slug} | Artículo público |
| GET /media/{key} | Imagen del catálogo o del blog |
| POST /v1/admin/productos | Crear producto |
| PUT /v1/admin/productos/{id} | Actualizar producto |
| DELETE /v1/admin/productos/{id} | Archivar producto y retirarlo de consultas públicas y cotizaciones |
| GET /v1/admin/reportes/catalogo | Cantidades de productos visibles y destacados |
| GET /v1/admin/publicaciones | Consultar borradores y publicaciones |
| POST /v1/admin/publicaciones | Crear borrador o publicación |
| PUT /v1/admin/publicaciones/{id} | Editar, publicar o volver a borrador |
| POST /v1/admin/imagenes | Subir una imagen mediante multipart, campo imagen |
| POST /interno/v1/cotizaciones | Obtener precios CLP y disponibilidad por lote para ms-pedidos |

En GET /v1/productos, pagina comienza en cero y tamano admite de 1 a 100. Los filtros opcionales son q, categoria (slug), grupo (new o popular), insignia, destacado, minimo y maximo. Ambos precios son inclusivos. orden admite id, precio-asc, precio-desc, nombre-asc y nombre-desc. Ejemplo: /v1/productos?categoria=anillos&maximo=200000&orden=precio-asc.

Ejemplo de alta de producto:

```json
{
  "categoriaId": 1,
  "nombre": "Anillo de plata",
  "descripcion": "Anillo liso de plata",
  "precioClp": 35000,
  "imagenKey": "products/product-1.jpg",
  "etiquetas": ["plata"],
  "grupo": "new",
  "insignia": "Nuevo",
  "destacado": false,
  "valoracion": 0,
  "cantidadResenas": 0,
  "disponibilidad": "CONSULTAR"
}
```

Los estados de disponibilidad son DISPONIBLE, AGOTADO y CONSULTAR. La cotización recibe un objeto con productoIds, por ejemplo {"productoIds":[1,3]}, y devuelve ID, nombre, precio vigente y disponibilidad. Un ID inexistente o archivado produce 409 para que pedidos solicite una revisión del carrito. La operación no reserva unidades.

Para editar artículos se envían slug, titulo, autorVisible, parrafos (lista ordenada), portadaKey y estado (BORRADOR o PUBLICADO). Al pasar a PUBLICADO se requiere contenido y se asigna la fecha. Los lectores reciben 404 al consultar un borrador por slug.

## Seguridad y conexión con ms-auth

Las lecturas públicas funcionan al iniciar el servicio en local. Para administrar se necesita un access token de Cognito y ms-auth accesible mediante MS_AUTH_URL. En desarrollo la URL predeterminada es http://localhost:8081. El catálogo valida el JWT y consulta GET /v1/me con el mismo token. Solo acepta el perfil del titular con rol ADMIN y estado ACTIVO. Si no puede verificar el permiso, deniega la operación.

Para cotizaciones se configura un cliente de servicio Cognito separado con PEDIDOS_CLIENT_ID y el scope catalogo/cotizar. El token requiere ese cliente y ese scope. Al desplegar, se puede restringir además el acceso de red a la ruta interna en AWS.

## Configuración posterior

| Variable | Uso |
|---|---|
| SPRING_PROFILES_ACTIVE | local o postgres |
| DB_URL, DB_USERNAME, DB_PASSWORD | Credenciales JDBC del perfil postgres |
| DB_POOL_SIZE | Máximo de conexiones por instancia, por defecto 4 |
| COGNITO_ISSUER, COGNITO_CLIENT_ID | User Pool y cliente web utilizado también por ms-auth |
| MS_AUTH_URL | URL base de ms-auth |
| PEDIDOS_CLIENT_ID | Cliente OAuth de ms-pedidos |
| FRONTEND_ORIGIN, PORT | Origen permitido y puerto |
| MEDIA_STORAGE | local por defecto, s3 para las nuevas cargas en AWS |
| MEDIA_BUCKET, AWS_REGION | Bucket y región para el modo s3 |

Para Supabase, configurar TLS en la URL JDBC. Con el pooler en modo transacción, añadir prepareThreshold=0. Para ejecutar migraciones mediante otra conexión o identidad, usar SPRING_LIQUIBASE_URL, SPRING_LIQUIBASE_USER y SPRING_LIQUIBASE_PASSWORD. La identidad de migración necesita crear el esquema catalogo y las tablas de seguimiento. Otorgar a la identidad de ejecución acceso a las tablas y secuencias del esquema.

En desarrollo las cargas se guardan en data/imagenes. En modo s3 el SDK usa las credenciales del rol de ejecución de AWS y escribe en uploads/ dentro del bucket indicado. El rol requiere s3:PutObject y s3:GetObject sobre ese prefijo. Las claves se conservan en la base de datos. La activación de s3 afecta las nuevas cargas; para conservar imágenes previamente subidas en desarrollo, copiar esos archivos al prefijo uploads/ del bucket.

## Verificación

La suite de integración usa H2 y un servidor HTTP de prueba que reproduce la respuesta de ms-auth. Verifica la carga inicial, búsqueda con tildes y términos múltiples, permisos, archivo de productos, cotizaciones, transición de borrador a publicado, subida de imagen y creación y edición de un producto.
