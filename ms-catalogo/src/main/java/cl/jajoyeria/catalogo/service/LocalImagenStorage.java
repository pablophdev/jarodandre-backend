package cl.jajoyeria.catalogo.service;

import java.io.*;
import java.nio.file.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.*;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Component
@ConditionalOnProperty(name = "app.media.storage", havingValue = "local")
public class LocalImagenStorage implements ImagenStorage {
    private final Path directorio;

    public LocalImagenStorage(@Value("${app.media.dir:./data/imagenes}") String ruta) {
        directorio = Path.of(ruta).toAbsolutePath().normalize();
    }

    public void guardar(String nombre, String tipo, InputStream datos, long longitud) throws IOException {
        Files.createDirectories(directorio);
        Path temporal = Files.createTempFile(directorio, "pendiente-", ".tmp");
        try {
            Files.copy(datos, temporal, StandardCopyOption.REPLACE_EXISTING);
            Files.move(temporal, directorio.resolve(nombre), StandardCopyOption.ATOMIC_MOVE);
        } finally {
            Files.deleteIfExists(temporal);
        }
    }

    public Resource leer(String nombre) {
        Path ruta = directorio.resolve(nombre);
        if (!Files.isRegularFile(ruta)) throw new ResponseStatusException(NOT_FOUND);
        return new FileSystemResource(ruta);
    }
}
