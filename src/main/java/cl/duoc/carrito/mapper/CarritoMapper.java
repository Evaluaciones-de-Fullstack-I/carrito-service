package cl.duoc.carrito.mapper;
import cl.duoc.carrito.dto.CreateRequestCarrito;
import cl.duoc.carrito.dto.UpdateRequestCarrito;
import cl.duoc.carrito.model.Carrito;


public class CarritoMapper {

    // CREATE
    public static Carrito toCarrito( CreateRequestCarrito request) {

        Carrito carrito = new Carrito();

        carrito.setClienteId(request.clienteId());
        carrito.setProductoId(request.productoId());
        carrito.setCantidad(request.cantidad());
        carrito.setSubtotal(request.subtotal());

        carrito.setEstado("ACTIVO");

        return carrito;
    }

    // UPDATE
    public static void updateCarrito(
            Carrito carrito,
            UpdateRequestCarrito request
    ) {

        carrito.setClienteId(request.clienteId());
        carrito.setProductoId(request.productoId());
        carrito.setCantidad(request.cantidad());
        carrito.setSubtotal(request.subtotal());
        carrito.setEstado(request.estado());
    }
}

