package cl.duoc.carrito.exception;

import java.time.Instant;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.http.ResponseEntity; 
import java.util.HashMap;  


import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(ResourceNotFoundException ex) {

        Map<String, Object> response = new HashMap<>();
        response.put("error", "No Encontrado");
        response.put("mensaje", ex.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {

        Map<String, Object> response = new HashMap<>();

        response.put("error", "ERROR DE VALIDACION");
        response.put("mensaje", "Error de validación en los datos enviados");

        Map<String, String> campos = new HashMap<>();

        ex.getBindingResult().getFieldErrors()
                .forEach(err -> campos.put(err.getField(), err.getDefaultMessage()));

        response.putAll(campos); // 👈 esto hace que quede "a la derecha"

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleJson(HttpMessageNotReadableException ex) {

        Map<String, Object> response = new HashMap<>();
        response.put("error", "JSON_INVALIDO");
        response.put("mensaje", "El JSON enviado es inválido");

        return ResponseEntity.badRequest().body(response);
    }

      @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneral(Exception ex) throws Exception {
        
        // Si el error es porque no encuentra un recurso de Swagger (como el favicon o estilos),
        // le decimos a Java que tire el error hacia afuera para que Spring lo resuelva nativamente.
        if (ex.getClass().getName().contains("NoResourceFoundException") || 
            ex instanceof jakarta.servlet.ServletException) {
            throw ex;
        }
        Map<String, Object> response = new HashMap<>();
        response.put("error", "INTERNAL_ERROR");
        response.put("mensaje", "Error interno del servidor");

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}