# ms-catalogo

Productos, categorías, imágenes, publicaciones y cotizaciones de JA Joyería. Puerto `8082`, base MySQL `catalogo` y paquete `cl.jajoyeria.catalogo`.

## Datos

Liquibase crea `categorias`, `productos` y `publicaciones`. Carga 4 categorías, 14 productos con sus IDs iniciales y 9 artículos con sus slugs, contenido y fechas. Etiquetas y párrafos se guardan en JSON. El precio se expresa en CLP enteros. Editar un producto conserva los snapshots de las compras anteriores en pedidos.

Las disponibilidades son `DISPONIBLE`, `AGOTADO` y `CONSULTAR`. Archivar un producto lo retira de consultas públicas y cotizaciones. Los artículos tienen estado `BORRADOR` o `PUBLICADO`; publicar exige contenido y asigna una fecha. Volver a borrador retira el artículo de la API pública.

## API

| Método y ruta | Operación |
| --- | --- |
| `GET /v1/categorias` | Categorías del catálogo. |
| `GET /v1/productos` | Productos visibles paginados y filtrados. |
| `GET /v1/productos/{id}` | Ficha pública. |
| `GET /v1/publicaciones` | Artículos publicados. |
| `GET /v1/publicaciones/{slug}` | Artículo por slug. |
| `GET /media/{key}` | Recursos iniciales o imágenes subidas. |
| `POST /v1/admin/productos` | Crear producto. |
| `PUT /v1/admin/productos/{id}` | Editar producto. |
| `DELETE /v1/admin/productos/{id}` | Archivar producto. |
| `GET /v1/admin/reportes/catalogo` | Cantidad de productos visibles y destacados. |
| `POST /v1/admin/imagenes` | Subir JPG, PNG o WebP mediante el campo multipart `imagen`. |
| `GET /v1/admin/publicaciones` | Borradores y artículos publicados. |
| `POST /v1/admin/publicaciones` | Crear artículo. |
| `PUT /v1/admin/publicaciones/{id}` | Editar, publicar o volver a borrador. |
| `POST /interno/v1/cotizaciones` | Precios y disponibilidad por lote para pedidos. |

Los filtros de productos son `q`, `categoria` por slug, `grupo`, `insignia`, `destacado`, `minimo` y `maximo`. `pagina` comienza en cero y `tamano` admite de 1 a 100. `orden` admite `id`, `precio-asc`, `precio-desc`, `nombre-asc` y `nombre-desc`. Los extremos del rango de precios son inclusivos.

Ejemplo de cotización interna:

```json
{"productoIds": [1, 3]}
```

Requiere `X-Internal-Token` con el mismo `CATALOGO_INTERNAL_TOKEN` de pedidos. Un producto retirado o inexistente produce `409`. La cotización devuelve disponibilidad declarada y precios; las reservas por unidades corresponden a una evolución del inventario.

## Imágenes y descripciones

Las imágenes iniciales están bajo `src/main/resources/static/media`. Las cargas nuevas se guardan en `data/imagenes` cuando `MEDIA_STORAGE=local`, con volumen persistente en Compose. La subida valida firma binaria y un tamaño máximo de 10 MB. Con `MEDIA_STORAGE=s3`, configurar bucket, región y credenciales del rol de ejecución. Las cargas locales anteriores requieren copiar sus archivos si se cambia el almacenamiento.

El frontend utiliza TipTap para editar `descripcion` y DOMPurify para limpiar el HTML al enviarlo y mostrarlo. El DTO admite hasta 8000 caracteres y la base conserva la cadena. Los textos anteriores siguen siendo compatibles con el renderizado del frontend.

## Ejecución y acceso

```powershell
$env:CATALOGO_INTERNAL_TOKEN='local-jarodandre-interno-2026'
.\mvnw.cmd spring-boot:run
.\mvnw.cmd clean verify
```

El perfil `mysql` conecta a `localhost:3307/catalogo`. Las operaciones administrativas validan el JWT y consultan el perfil en `MS_AUTH_URL`, predeterminado `http://localhost:8081`. Exigen un administrador activo.

La [guía principal](../README.md) explica Compose y las variables. Con `SWAGGER_ENABLED=true`, abrir [Swagger de catálogo](http://localhost:8082/swagger-ui/index.html).
