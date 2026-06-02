package cl.duoc.carrito.controller;

import java.util.HashMap;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import java.util.Map;
import cl.duoc.carrito.dto.CreateRequestCarrito;
import cl.duoc.carrito.dto.UpdateRequestCarrito;
import cl.duoc.carrito.exception.ResourceNotFoundException;
import cl.duoc.carrito.mapper.CarritoMapper;
import cl.duoc.carrito.model.Carrito;
import cl.duoc.carrito.service.CarritoService;
import jakarta.validation.Valid;
import java.util.List;


@RestController
@RequestMapping("/api/v1/carritos")
public class CarritoController {

    private final CarritoService carritoService;
    private final WebClient webClient;

    public CarritoController(
            CarritoService carritoService,
            WebClient webClient
    ) {
        this.carritoService = carritoService;
        this.webClient = webClient;
    }

    // LISTAR
    @GetMapping
    public ResponseEntity<List<Carrito>> listarCarritos() {

        return ResponseEntity.ok(
                carritoService.getCarritos()
        );
    }

    // CREAR carrito  comunciacion con cliente para agregar producto al carrito
    @PostMapping
    public ResponseEntity<Map<String, Object>> crearCarrito(
            @Valid @RequestBody CreateRequestCarrito request
    ) {
    System.out.println(
            "📥 CARRITO recibió solicitud del CLIENTE ID: "
            +request.clienteId());
        Carrito carrito =
                carritoService.saveCarrito(
                        CarritoMapper.toCarrito(request)
                );

        Map<String, Object> response =
                new HashMap<>();

        response.put(
                "mensaje",
                "Producto agregado al carrito"
        );

        response.put("id", carrito.getId());
System.out.println("✅ CARRITO guardó producto para CLIENTE ID: ");
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    // BUSCAR POR ID
    @GetMapping("/{id}")
    public ResponseEntity<Carrito> buscarCarrito(
            @PathVariable int id
    ) {

        Carrito carrito =
                carritoService.getCarritoById(id);

        if (carrito == null) {

            throw new ResourceNotFoundException(
                    "Carrito no encontrado"
            );
        }

        return ResponseEntity.ok(carrito);
    }

    // ACTUALIZAR
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>>
    actualizarCarrito(
            @PathVariable int id,
            @Valid @RequestBody
            UpdateRequestCarrito request
    ) {

        Carrito carrito =
                carritoService.updateCarrito(
                        id,
                        request
                );

        Map<String, Object> response =
                new HashMap<>();

        response.put(
                "mensaje",
                "Carrito actualizado correctamente"
        );

        response.put("id", carrito.getId());

        return ResponseEntity.ok(response);
    }

    // ELIMINAR
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>>
    eliminarCarrito(
            @PathVariable int id
    ) {

        carritoService.deleteCarrito(id);

        Map<String, String> response =
                new HashMap<>();

        response.put(
                "mensaje",
                "Producto eliminado del carrito"
        );

        return ResponseEntity.ok(response);
    }

    // VER CARRITO DE CLIENTE
    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<List<Carrito>>
    buscarPorCliente(
            @PathVariable Integer clienteId
    ) {

        return ResponseEntity.ok(
                carritoService.buscarPorCliente(
                        clienteId
                )
        );
    }

    // CAMBIAR ESTADO
    @PutMapping("/{id}/estado")
    public ResponseEntity<Map<String, String>>
    cambiarEstado(
            @PathVariable int id,
            @RequestBody Map<String, String> request
    ) {

        carritoService.cambiarEstado(
                id,
                request.get("estado")
        );

        Map<String, String> response =
                new HashMap<>();

        response.put(
                "mensaje",
                "Estado actualizado correctamente"
        );

        return ResponseEntity.ok(response);
    }

@PostMapping("/cliente/{clienteId}/agregar")
public ResponseEntity<Void> agregarProductoCliente(
        @PathVariable Integer clienteId
) {

    System.out.println(
            "📥 CARRITO recibió solicitud del CLIENTE ID: "
            + clienteId
    );

    return ResponseEntity.ok().build();
}

    }
 







