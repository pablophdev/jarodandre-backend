package cl.jajoyeria.catalogo.service;

import java.io.InputStream;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.*;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Component
@ConditionalOnProperty(name = "app.media.storage", havingValue = "s3")
public class S3ImagenStorage implements ImagenStorage {
    private final S3Client s3;
    private final String bucket;

    public S3ImagenStorage(@Value("${app.media.bucket}") String bucket) {
        if (bucket.isBlank()) throw new IllegalArgumentException("Configura MEDIA_BUCKET para usar S3");
        this.bucket = bucket;
        this.s3 = S3Client.create();
    }

    public void guardar(String nombre, String tipo, InputStream datos, long longitud) {
        s3.putObject(r -> r.bucket(bucket).key("uploads/" + nombre).contentType(tipo),
                RequestBody.fromInputStream(datos, longitud));
    }

    public Resource leer(String nombre) {
        try {
            byte[] bytes = s3.getObjectAsBytes(r -> r.bucket(bucket).key("uploads/" + nombre)).asByteArray();
            return new ByteArrayResource(bytes);
        } catch (NoSuchKeyException ex) {
            throw new ResponseStatusException(NOT_FOUND);
        }
    }

    @PreDestroy
    public void cerrar() {
        s3.close();
    }
}
