package cl.jajoyeria.catalogo.controller;

import cl.jajoyeria.catalogo.dto.*;
import cl.jajoyeria.catalogo.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "Catálogo Público", description = "Endpoints de consulta de categorías y productos para la tienda virtual")
public class CatalogoController {
    private final CatalogoService catalogo;

    @Operation(summary = "Listar categorías", description = "Retorna la lista completa de categorías activas ordenadas alfabéticamente por nombre.")
    @ApiResponse(responseCode = "200", description = "Categorías obtenidas exitosamente")
    @GetMapping("/v1/categorias")
    public List<CategoriaResponse> categorias() {
        return catalogo.categorias();
    }

    @Operation(summary = "Buscar y filtrar productos", description = "Búsqueda paginada de productos visibles con soporte de filtros combinables por término de búsqueda, categoría, grupo, rango de precios y orden.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Página de productos obtenida exitosamente"),
            @ApiResponse(responseCode = "400", description = "Parámetros de consulta inválidos (ej. mínimo > máximo u orden desconocido)")
    })
    @GetMapping("/v1/productos")
    public Page<ProductoResponse> productos(
            @Parameter(description = "Término de búsqueda textual (insensible a tildes y mayúsculas)") @RequestParam(required = false) @Size(max = 180) String q,
            @Parameter(description = "Slug de la categoría (ej. 'anillos', 'collares')") @RequestParam(required = false) String categoria,
            @Parameter(description = "Grupo visual ('new' o 'popular')") @RequestParam(required = false) String grupo,
            @Parameter(description = "Insignia visual") @RequestParam(required = false) String insignia,
            @Parameter(description = "Filtrar únicamente productos destacados") @RequestParam(required = false) Boolean destacado,
            @Parameter(description = "Precio mínimo en CLP (inclusivo)") @RequestParam(required = false) @PositiveOrZero Long minimo,
            @Parameter(description = "Precio máximo en CLP (inclusivo)") @RequestParam(required = false) @PositiveOrZero Long maximo,
            @Parameter(description = "Criterio de ordenamiento: 'id', 'precio-asc', 'precio-desc', 'nombre-asc', 'nombre-desc'") @RequestParam(defaultValue = "id") String orden,
            @Parameter(description = "Número de página (iniciando en 0)") @RequestParam(defaultValue = "0") @Min(0) int pagina,
            @Parameter(description = "Cantidad de elementos por página (de 1 a 100)") @RequestParam(defaultValue = "24") @Min(1) @Max(100) int tamano) {
        return catalogo.buscar(q, categoria, grupo, insignia, destacado, minimo, maximo, orden, pagina, tamano);
    }

    @Operation(summary = "Detalle de una pieza", description = "Retorna la información completa de una pieza de joyería visible a partir de su ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Producto encontrado"),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado o archivado")
    })
    @GetMapping("/v1/productos/{id}")
    public ProductoResponse producto(
            @Parameter(description = "Identificador numérico del producto", example = "3") @PathVariable long id) {
        return catalogo.obtener(id);
    }

    @Operation(summary = "Resumen de inventario", description = "Obtiene las métricas de cantidad de productos visibles y destacados para el panel administrativo.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Resumen obtenido exitosamente"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "No autorizado (se requiere rol ADMIN activo)")
    })
    @SecurityRequirement(name = "BearerAuth")
    @Tag(name = "Administración - Catálogo", description = "Operaciones privilegiadas para administración del catálogo")
    @GetMapping("/v1/admin/reportes/catalogo")
    public ResumenCatalogo resumen() {
        return catalogo.resumen();
    }

    @Operation(summary = "Crear nuevo producto", description = "Registra una nueva pieza de joyería en el catálogo.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Producto creado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos o categoría inexistente"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "No autorizado (se requiere rol ADMIN activo)")
    })
    @SecurityRequirement(name = "BearerAuth")
    @Tag(name = "Administración - Catálogo")
    @PostMapping("/v1/admin/productos")
    @ResponseStatus(HttpStatus.CREATED)
    public ProductoResponse crear(@Valid @RequestBody ProductoRequest datos) {
        return catalogo.guardar(null, datos);
    }

    @Operation(summary = "Actualizar producto", description = "Actualiza la información, precio, imágenes o disponibilidad de un producto existente.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Producto actualizado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o categoría inexistente"),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado o archivado"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "No autorizado")
    })
    @SecurityRequirement(name = "BearerAuth")
    @Tag(name = "Administración - Catálogo")
    @PutMapping("/v1/admin/productos/{id}")
    public ProductoResponse editar(
            @Parameter(description = "Identificador del producto a modificar", example = "3") @PathVariable long id,
            @Valid @RequestBody ProductoRequest datos) {
        return catalogo.guardar(id, datos);
    }

    @Operation(summary = "Archivar producto", description = "Realiza la baja lógica de un producto. El producto deja de aparecer en listados públicos y no puede ser cotizado.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Producto archivado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "No autorizado")
    })
    @SecurityRequirement(name = "BearerAuth")
    @Tag(name = "Administración - Catálogo")
    @DeleteMapping("/v1/admin/productos/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void archivar(
            @Parameter(description = "Identificador del producto a archivar", example = "3") @PathVariable long id) {
        catalogo.archivar(id);
    }

    @Operation(summary = "Cotización de precios en lote", description = "Endpoint de uso interno exclusivo para ms-pedidos. Valida existencia y retorna el precio CLP vigente y disponibilidad de cada pieza.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cotización completada"),
            @ApiResponse(responseCode = "409", description = "Conflicto: uno o más productos ya no existen o fueron archivados"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "Token inválido o falta scope 'catalogo/cotizar'")
    })
    @SecurityRequirement(name = "PedidosServiceAuth")
    @Tag(name = "Interno - Cotizaciones", description = "Comunicación inter-servicio máquina a máquina (M2M)")
    @PostMapping("/interno/v1/cotizaciones")
    public List<PrecioResponse> cotizar(@Valid @RequestBody CotizacionRequest request) {
        return catalogo.cotizar(request.productoIds());
    }
}
