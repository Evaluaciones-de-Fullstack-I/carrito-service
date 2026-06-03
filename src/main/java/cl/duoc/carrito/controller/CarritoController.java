package cl.duoc.carrito.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

import cl.duoc.carrito.dto.CreateRequestCarrito;
import cl.duoc.carrito.dto.ProductoResponseDTO;
import cl.duoc.carrito.dto.UpdateRequestCarrito;
import cl.duoc.carrito.exception.ResourceNotFoundException;
import cl.duoc.carrito.model.Carrito;
import cl.duoc.carrito.service.CarritoService;
import jakarta.validation.Valid;

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

    // CREAR carrito (comunicacion con cliente para agregar producto al carrito)
    @PostMapping
    public ResponseEntity<Map<String, Object>> crearCarrito(
            @Valid @RequestBody CreateRequestCarrito request
    ) {
        System.out.println("LLAMANDO A CATALOGO");
        System.out.println(" CLIENTE ID: " + request.clienteId());

        // LLAMADA AL CATÁLOGO (WebClient en controller)
        ProductoResponseDTO producto = webClient
                .get()
                .uri("http://localhost:8090/api/productos/" + request.productoId())
                .retrieve()
                .bodyToMono(ProductoResponseDTO.class)
                .block();

        // validar si existe producto
        if (producto == null) {
            throw new ResourceNotFoundException("Producto no encontrado en catálogo");
        }

        // 🛒 crear carrito
        Carrito carrito = new Carrito();
        carrito.setClienteId(request.clienteId());
        carrito.setProductoId(producto.getId().intValue());
        carrito.setCantidad(request.cantidad());

        // calcular subtotal real
        double subtotal = producto.getPrecio() * request.cantidad();
        carrito.setSubtotal(subtotal);
        carrito.setEstado("ACTIVO");

        // guardar en BD 
        Carrito guardado = carritoService.saveCarrito(carrito);

        // 📤 respuesta
        Map<String, Object> response = new HashMap<>();
        response.put("mensaje", "Producto agregado al carrito");
        response.put("id", guardado.getId());

        System.out.println("✅ Carrito creado para cliente ID: " + request.clienteId());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // BUSCAR POR ID
    @GetMapping("/{id}")
    public ResponseEntity<Carrito> buscarCarrito(
            @PathVariable int id
    ) {
        Carrito carrito = carritoService.getCarritoById(id);

        if (carrito == null) {
            throw new ResourceNotFoundException("Carrito no encontrado");
        }
        return ResponseEntity.ok(carrito);
    }

    // ACTUALIZAR
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> actualizarCarrito(
            @PathVariable int id,
            @Valid @RequestBody UpdateRequestCarrito request
    ) {
        Carrito carrito = carritoService.updateCarrito(id, request);
        Map<String, Object> response = new HashMap<>();
        response.put("mensaje", "Carrito actualizado correctamente");
        response.put("id", carrito.getId());
        return ResponseEntity.ok(response);
    }

    // ELIMINAR
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> eliminarCarrito(
            @PathVariable int id
    ) {
        carritoService.deleteCarrito(id);
        Map<String, String> response = new HashMap<>();
        response.put("mensaje", "Producto eliminado del carrito");
        return ResponseEntity.ok(response);
    }

    // VER CARRITO DE CLIENTE
    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<List<Carrito>> buscarPorCliente(
            @PathVariable Integer clienteId
    ) {
        return ResponseEntity.ok(
                carritoService.buscarPorCliente(clienteId)
        );
    }

    // CAMBIAR ESTADO
    @PutMapping("/{id}/estado")
    public ResponseEntity<Map<String, String>> cambiarEstado(
            @PathVariable int id,
            @RequestBody Map<String, String> request
    ) {
        carritoService.cambiarEstado(id, request.get("estado"));
        Map<String, String> response = new HashMap<>();
        response.put("mensaje", "Estado actualizado correctamente");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/cliente/{clienteId}/agregar")
    public ResponseEntity<Void> agregarProductoCliente(
            @PathVariable Integer clienteId
    ) {
        System.out.println(" CARRITO recibió solicitud del CLIENTE ID: " + clienteId);
        return ResponseEntity.ok().build();
    }

    // =====================================================================================
    // 🛒 NUEVO ENDPOINT: ACCIÓN DE COMPRAR (Checkout y envío a MS Pedidos de Carlos - 8087)
    // =====================================================================================
    @PostMapping("/cliente/{clienteId}/checkout")
    public ResponseEntity<Map<String, Object>> realizarCheckout(
            @PathVariable Integer clienteId,
            @RequestBody Map<String, String> requestBody
    ) {
        // 1. Buscar todos los ítems en el carrito del cliente
        List<Carrito> carritosCliente = carritoService.buscarPorCliente(clienteId);
        
        if (carritosCliente == null || carritosCliente.isEmpty()) {
            throw new ResourceNotFoundException("El carrito está vacío para el cliente: " + clienteId);
        }

        // 2. Sumar el total de los productos "ACTIVOS"
        double montoTotal = 0;
        for (Carrito c : carritosCliente) {
            if ("ACTIVO".equals(c.getEstado())) {
                montoTotal += c.getSubtotal();
            }
        }

        if (montoTotal == 0) {
            throw new ResourceNotFoundException("No hay productos activos en el carrito para cobrar.");
        }

        // 3. Armar el DTO exacto que pide el MS Pedidos de Carlos
        Map<String, Object> pedidoRequest = new HashMap<>();
        pedidoRequest.put("clienteId", clienteId);
        pedidoRequest.put("montoTotal", montoTotal);
        // Rescatamos la dirección enviada en el JSON, si no manda nada ponemos una por defecto
        pedidoRequest.put("direccionEnvio", requestBody.getOrDefault("direccionEnvio", "Dirección Central 123"));

        // 4. Llamar al Microservicio de Pedidos (Puerto 8087)
        System.out.println("📦 [CONEXIÓN] Enviando carrito del Cliente " + clienteId + " a MS Pedidos...");
        
        try {
            webClient.post()
                    .uri("http://localhost:8087/api/pedidos") // Apunta a tu MS
                    .bodyValue(pedidoRequest)
                    .retrieve()
                    .bodyToMono(Void.class)
                    .block();
            
            // 5. Cambiar el estado del carrito a "COMPRADO" para que quede limpio
            for (Carrito c : carritosCliente) {
                if ("ACTIVO".equals(c.getEstado())) {
                    carritoService.cambiarEstado(c.getId(), "COMPRADO");
                }
            }

            Map<String, Object> response = new HashMap<>();
            response.put("mensaje", "Checkout exitoso. La orden fue enviada a Pedidos.");
            response.put("montoTotalPagado", montoTotal);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.out.println("❌ [ERROR] Falló la comunicación con MS Pedidos: " + e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "No se pudo generar la orden. Verifica que MS Pedidos (8087) esté encendido.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}