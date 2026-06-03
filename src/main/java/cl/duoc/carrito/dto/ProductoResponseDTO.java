package cl.duoc.carrito.dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductoResponseDTO {

private Long id;
    private String nombre;
    private Double precio;
    private Integer stock;
    private Boolean disponibilidad;
    private Integer vendedorId;
    private String nombreCategoria;
   
}
