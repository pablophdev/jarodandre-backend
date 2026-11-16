package cl.jajoyeria.catalogo.controller;

import cl.jajoyeria.catalogo.dto.*;
import cl.jajoyeria.catalogo.service.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class CatalogoController {
    private final CatalogoService catalogo;

    @GetMapping("/v1/categorias")
    public List<CategoriaResponse> categorias() {
        return catalogo.categorias();
    }

    @GetMapping("/v1/productos")
    public Page<ProductoResponse> productos(@RequestParam(required = false) @Size(max = 180) String q,
            @RequestParam(required = false) String categoria, @RequestParam(required = false) String grupo,
            @RequestParam(required = false) String insignia, @RequestParam(required = false) Boolean destacado,
            @RequestParam(required = false) @PositiveOrZero Long minimo, @RequestParam(required = false) @PositiveOrZero Long maximo,
            @RequestParam(defaultValue = "id") String orden, @RequestParam(defaultValue = "0") @Min(0) int pagina,
            @RequestParam(defaultValue = "24") @Min(1) @Max(100) int tamano) {
        return catalogo.buscar(q, categoria, grupo, insignia, destacado, minimo, maximo, orden, pagina, tamano);
    }

    @GetMapping("/v1/productos/{id}")
    public ProductoResponse producto(@PathVariable long id) {
        return catalogo.obtener(id);
    }

    @GetMapping("/v1/admin/reportes/catalogo")
    public ResumenCatalogo resumen() {
        return catalogo.resumen();
    }

    @PostMapping("/v1/admin/productos")
    @ResponseStatus(HttpStatus.CREATED)
    public ProductoResponse crear(@Valid @RequestBody ProductoRequest datos) {
        return catalogo.guardar(null, datos);
    }

    @PutMapping("/v1/admin/productos/{id}")
    public ProductoResponse editar(@PathVariable long id, @Valid @RequestBody ProductoRequest datos) {
        return catalogo.guardar(id, datos);
    }

    @DeleteMapping("/v1/admin/productos/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void archivar(@PathVariable long id) {
        catalogo.archivar(id);
    }

    @PostMapping("/interno/v1/cotizaciones")
    public List<PrecioResponse> cotizar(@Valid @RequestBody CotizacionRequest request) {
        return catalogo.cotizar(request.productoIds());
    }
}
