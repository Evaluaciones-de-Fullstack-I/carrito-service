package cl.duoc.carrito.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "carrito")
@Data
@NoArgsConstructor
@AllArgsConstructor

public class Carrito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private Integer clienteId;

    @Column(nullable = false)
    private Integer productoId;

    @Column(nullable = false)
    private Integer cantidad;

    @Column(nullable = false)
    private Double subtotal;

    @Column(nullable = false, length = 30)
    private String estado;
}


//id → identificador del registro del carrito
//clienteId → cliente que agregó el producto.
//productoId → producto agregado al carrito.
//cantidad → cantidad de unidades.
//subtotal → valor de esa línea del carrito.
//estado → por ejemplo:
//"ACTIVO"
//"COMPRADO"
//"ELIMINADO"