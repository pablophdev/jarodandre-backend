package cl.jajoyeria.catalogo.service;

import cl.jajoyeria.catalogo.dto.ImagenResponse;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import static org.springframework.http.HttpStatus.*;

@Service
@RequiredArgsConstructor
public class ImagenService {
    private final ImagenStorage storage;

    public ImagenResponse guardar(MultipartFile imagen) throws IOException {
        if (imagen.isEmpty() || imagen.getSize() > 10 * 1024 * 1024) {
            throw new ResponseStatusException(BAD_REQUEST, "La imagen debe pesar entre 1 byte y 10 MB");
        }
        try (var stream = new BufferedInputStream(imagen.getInputStream())) {
            stream.mark(16);
            String extension = extension(stream.readNBytes(16));
            stream.reset();
            String nombre = UUID.randomUUID() + "." + extension;
            String tipo = extension.equals("jpg") ? "image/jpeg" : "image/" + extension;
            storage.guardar(nombre, tipo, stream, imagen.getSize());
            return new ImagenResponse("uploads/" + nombre);
        }
    }

    private String extension(byte[] b) {
        if (b.length >= 3 && b[0] == (byte)0xff && b[1] == (byte)0xd8 && b[2] == (byte)0xff) return "jpg";
        if (b.length >= 8 && Arrays.equals(Arrays.copyOf(b, 8), new byte[]{(byte)137,80,78,71,13,10,26,10})) return "png";
        if (b.length >= 16 && new String(b, 0, 4, StandardCharsets.US_ASCII).equals("RIFF")
                && new String(b, 8, 4, StandardCharsets.US_ASCII).equals("WEBP")) return "webp";
        throw new ResponseStatusException(UNSUPPORTED_MEDIA_TYPE, "Selecciona una imagen JPG, PNG o WebP");
    }

    public Resource leer(String nombre) {
        if (!nombre.matches("[0-9a-f-]{36}\\.(jpg|png|webp)")) throw new ResponseStatusException(NOT_FOUND);
        return storage.leer(nombre);
    }
}
