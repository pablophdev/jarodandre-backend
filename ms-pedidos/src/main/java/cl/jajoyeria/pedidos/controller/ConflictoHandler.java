package cl.jajoyeria.pedidos.controller;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice
public class ConflictoHandler {
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail conflicto() {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, "Conflicto de escritura. Reintenta la operación.");
    }

    @ExceptionHandler(ArithmeticException.class)
    public ProblemDetail monto() {
        return ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_ENTITY, "El total excede el límite de importe.");
    }
}
