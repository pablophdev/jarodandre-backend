package cl.jajoyeria.catalogo.service;

import org.springframework.core.io.Resource;
import java.io.*;

public interface ImagenStorage {
    void guardar(String nombre, String tipo, InputStream datos, long longitud) throws IOException;
    Resource leer(String nombre);
}
