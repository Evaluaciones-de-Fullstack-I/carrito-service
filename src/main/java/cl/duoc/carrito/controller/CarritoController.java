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
import cl.duoc.carrito.dto.ProductoResponseDTO;
import cl.duoc.carrito.dto.UpdateRequestCarrito;
import cl.duoc.carrito.exception.ResourceNotFoundException;
import cl.duoc.carrito.mapper.CarritoMapper;
import cl.duoc.carrito.model.Carrito;
import cl.duoc.carrito.service.CarritoService;
import jakarta.validation.Valid;
import lombok.Data;
import cl.  duoc.carrito.repository.CarritoRepository   ;
import java.util.List;
//importaciones necesarias para swagger

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;



@RestController
@RequestMapping("/api/v1/carritos")
@Tag(name = "Carrito Controller", description = "Endpoints para la gestión del carrito de compras e integración con Catálogo")

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
    @Operation(summary = "Listar carritos", description = "Obtiene la lista de todos los carritos")
    @ApiResponse(responseCode = "200", description = "Lista de carritos obtenida correctamente")        
    @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    public ResponseEntity<List<Carrito>> listarCarritos() {

        return ResponseEntity.ok(
                carritoService.getCarritos()
        );
    }

    // CREAR carrito   para agregar producto al carrito
    
@PostMapping
@Operation(summary = "Agregar producto al carrito", description = "Valida el producto contra el microservicio de Catálogo, calcula subtotales y registra el ítem en la base de datos.")
    @ApiResponse(responseCode = "201", description = "Producto agregado correctamente al carrito")
    @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos")
    @ApiResponse(responseCode = "404", description = "Producto no encontrado en el catálogo")
    @ApiResponse(responseCode = "500", description = "Error interno del servidor")
public ResponseEntity<Map<String, Object>> crearCarrito(
     
     @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Estructura JSON necesaria para registrar un producto en el carrito",
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = CreateRequestCarrito.class),
                examples = @ExampleObject(
                    name = "Ejemplo agregar producto",
                    value = "{\n  \"clienteId\": 1,\n  \"productoId\": 1,\n  \"cantidad\": 2\n}"
                )
            )
        )

     
        @Valid @RequestBody CreateRequestCarrito request
) {
    System.out.println("LLAMANDO A CATALOGO");
    System.out.println(" CLIENTE ID: " + request.clienteId());

    //  LLAMADA AL CATÁLOGO (WebClient en controller)
    ProductoResponseDTO producto = webClient
            .get()
            .uri("http://localhost:8090/api/productos/" + request.productoId())
            .retrieve()
            .bodyToMono(ProductoResponseDTO.class)
            .block();
//  post http://localhost:8086/api/v1/carritos -----> para comprobar que se conecta con catalogo
//body para postman
//*{
 // "clienteId": 1,
  //"productoId": 1,
  //"cantidad": 2
 //}  

// validar si existe producto
    if (producto == null) {
        throw new ResourceNotFoundException("Producto no encontrado en catálogo");
    }

    // 🛒 crear carrito
    Carrito carrito = new Carrito();
    carrito.setClienteId(request.clienteId());
    carrito.setProductoId(producto.getId().intValue());
    carrito.setCantidad(request.cantidad());

    //  calcular subtotal real
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
    @Operation(summary = "Buscar carrito por ID", description = "Recupera los detalles de un ítem de carrito específico mediante su identificador numérico.")
    @ApiResponse(responseCode = "200", description = "Carrito encontrado exitosamente")
    @ApiResponse(responseCode = "404", description = "El ID del carrito solicitado no existe")

    public ResponseEntity<Carrito> buscarCarrito( @PathVariable int id
    ) {

        Carrito carrito = carritoService.getCarritoById(id);
          if (carrito == null) {
         throw new ResourceNotFoundException("Carrito no encontrado"
            );
        }

        return ResponseEntity.ok(carrito);
    }



    
    // ACTUALIZAR
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar cantidad del carrito", description = "Modifica la cantidad o datos de un registro de carrito existente.")
    @ApiResponse(responseCode = "200", description = "Carrito actualizado correctamente")
    @ApiResponse(responseCode = "400", description = "Cuerpo de la petición inválido")
    @ApiResponse(responseCode = "404", description = "Carrito no encontrado")
    public ResponseEntity<Map<String, Object>>actualizarCarrito(
            @PathVariable int id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "JSON con la nueva configuración de la actualización",
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = UpdateRequestCarrito.class),
                examples = @ExampleObject(
                    name = "Ejemplo actualizar cantidad",
                    value = "{\n  \"cantidad\": 5\n}"
                )
            )
        )
            @Valid @RequestBody UpdateRequestCarrito request
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
    @Operation(summary = "Eliminar producto del carrito", description = "Elimina de forma física el registro del carrito de la base de datos.")
    @ApiResponse(responseCode = "200", description = "Producto removido exitosamente")
    @ApiResponse(responseCode = "404", description = "El ID de carrito no existe")
    public ResponseEntity<Map<String, String>>eliminarCarrito(  @PathVariable int id
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
    @Operation(summary = "Obtener carrito de un cliente específico", description = "Retorna el listado de productos activos asociados a un ID de cliente determinado.")
    @ApiResponse(responseCode = "200", description = "Lista de productos del cliente recuperada")
    public ResponseEntity<List<Carrito>>buscarPorCliente(@PathVariable Integer clienteId
    ) {

        return ResponseEntity.ok(
                carritoService.buscarPorCliente(
                        clienteId
                )
        );
    }

    // CAMBIAR ESTADO
    @PutMapping("/{id}/estado")
    @Operation(summary = "Modificar estado del carrito", description = "Cambia el estado operativo del carrito (ej: de 'ACTIVO' a 'PROCESADO' o 'COMPRADO').")
    @ApiResponse(responseCode = "200", description = "Estado cambiado con éxito")
    public ResponseEntity<Map<String, String>>
    cambiarEstado( @PathVariable int id,
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Mapa con el nuevo estado",
            required = true,
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Ejemplo cambio de estado",
                    value = "{\n  \"estado\": \"PROCESADO\"\n}"
                )
            )
        )
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
@Operation(summary = "Simular solicitud desde Cliente", description = "Endpoint de control interno para verificar comunicación asíncrona o trazas desde el microservicio de Clientes.")
@ApiResponse(responseCode = "200", description = "Trazabilidad simulada correctamente")
public ResponseEntity<Void> agregarProductoCliente(
        @PathVariable Integer clienteId
) {

    System.out.println(
            " CARRITO recibió solicitud del CLIENTE ID: "
            + clienteId
    );

    return ResponseEntity.ok().build();
}

}


    







