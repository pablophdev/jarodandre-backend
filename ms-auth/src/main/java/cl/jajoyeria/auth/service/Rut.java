package cl.jajoyeria.auth.service;

import java.util.Locale;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

final class Rut {
    private Rut() {}

    static String normalizar(String entrada) {
        String rut = entrada.strip().replace(".", "").toUpperCase(Locale.ROOT);
        if (!rut.matches("[1-9][0-9]{6,7}-[0-9K]")) throw invalido();
        String cuerpo = rut.substring(0, rut.length() - 2);
        int suma = 0;
        for (int i = 0; i < cuerpo.length(); i++) {
            int digito = cuerpo.charAt(cuerpo.length() - 1 - i) - '0';
            suma += digito * (2 + i % 6);
        }
        char verificador = "0K987654321".charAt(suma % 11);
        if (verificador != rut.charAt(rut.length() - 1)) throw invalido();
        return rut;
    }

    private static ResponseStatusException invalido() {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, "RUT inválido");
    }
}
