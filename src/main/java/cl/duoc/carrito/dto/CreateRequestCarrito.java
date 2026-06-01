package cl.duoc.carrito.dto;

import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data; 
import java.time.LocalDate;



public record CreateRequestCarrito (

    @NotNull(message = "El id del cliente es obligatorio")
    Integer clienteId,

    @NotNull(message = "El id del producto es obligatorio")
    Integer productoId,

    @NotNull(message = "La cantidad es obligatoria")
    @Positive(message = "La cantidad debe ser mayor a 0")
    Integer cantidad,

    @NotNull(message = "El subtotal es obligatorio")
    Double subtotal)
    {

}
//el estado no se incluye porque al crear un carrito se le asignaautomaticamente desde el mapper