package cl.jajoyeria.pedidos.controller;

import java.util.*;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class ValidacionHandler extends ResponseEntityExceptionHandler {
    private static final Map<String,String> NOMBRES = Map.ofEntries(
            Map.entry("correo", "Correo"), Map.entry("password", "Contraseña"),
            Map.entry("rut", "RUT"), Map.entry("nombre", "Nombre"),
            Map.entry("telefono", "Teléfono"), Map.entry("comunaId", "Comuna"),
            Map.entry("categoriaId", "Categoría"), Map.entry("precioClp", "Precio"),
            Map.entry("descripcion", "Descripción"), Map.entry("imagenKey", "Imagen"),
            Map.entry("direccion", "Dirección"), Map.entry("ciudad", "Ciudad"),
            Map.entry("mensaje", "Mensaje"), Map.entry("estrellas", "Estrellas"),
            Map.entry("comentario", "Reseña"));

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        Map<String,String> errores = new LinkedHashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            String field = error.getField();
            String simple = field.substring(field.lastIndexOf('.') + 1);
            errores.putIfAbsent(field, nombre(simple) + ": " + mensaje(simple, error.getCode()));
        }
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status,
                String.join(" ", errores.values()));
        problem.setTitle("Revisa los datos del formulario");
        problem.setProperty("errores", errores);
        return handleExceptionInternal(ex, problem, headers, status, request);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        var problem = ProblemDetail.forStatusAndDetail(status,
                "No pudimos leer los datos. Revisa los números, las opciones seleccionadas y los campos obligatorios.");
        problem.setTitle("Datos del formulario no válidos");
        return handleExceptionInternal(ex, problem, headers, status, request);
    }

    @Override
    protected ResponseEntity<Object> handleHandlerMethodValidationException(HandlerMethodValidationException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        var problem = ProblemDetail.forStatusAndDetail(status,
                "Revisa los filtros, las cantidades y las opciones seleccionadas.");
        problem.setTitle("Parámetros no válidos");
        return handleExceptionInternal(ex, problem, headers, status, request);
    }

    private static String nombre(String field) {
        return NOMBRES.getOrDefault(field, field);
    }

    private static String mensaje(String field, String code) {
        if ("Size".equals(code) && "password".equals(field)) return "debe tener entre 8 y 72 caracteres.";
        if ("Size".equals(code) && "nombre".equals(field)) return "revisa su longitud; en el registro debe tener entre 3 y 120 caracteres.";
        if ("Pattern".equals(code) && "telefono".equals(field)) return "ingresa entre 8 y 12 números, o deja el campo vacío.";
        if ("Pattern".equals(code) && "nombre".equals(field)) return "utiliza solo letras y espacios.";
        return switch (code == null ? "" : code) {
            case "NotBlank", "NotNull", "NotEmpty" -> "completa este campo.";
            case "Email" -> "ingresa un correo válido, por ejemplo ana@correo.cl.";
            case "Size" -> "el contenido es demasiado corto o demasiado largo.";
            case "Min", "Max", "DecimalMin", "DecimalMax" -> "elige un valor dentro del rango permitido.";
            case "Positive", "PositiveOrZero" -> "ingresa un número válido.";
            case "Pattern" -> "revisa el formato de este campo.";
            default -> "revisa el valor ingresado.";
        };
    }
}
