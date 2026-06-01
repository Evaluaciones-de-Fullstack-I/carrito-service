package cl.duoc.carrito.dto;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Positive;

public record UpdateRequestCarrito (

    @NotNull(message = "El id del cliente es obligatorio")
    Integer clienteId,

    @NotNull(message = "El id del producto es obligatorio")
    Integer productoId,

    @NotNull(message = "La cantidad es obligatoria")
    @Positive(message = "La cantidad debe ser mayor a 0")
    Integer cantidad,

    @NotNull(message = "El subtotal es obligatorio")
    Double subtotal,

    @NotNull(message = "El estado es obligatorio")
    String estado

) {
}

