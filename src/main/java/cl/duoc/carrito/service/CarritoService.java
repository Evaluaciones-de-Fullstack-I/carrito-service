package cl.duoc.carrito.service;
import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import cl.duoc.carrito.model.Carrito;
import cl.duoc.carrito.repository.CarritoRepository;
import cl.duoc.carrito.dto.UpdateRequestCarrito;
import cl.duoc.carrito.mapper.CarritoMapper;
import cl.duoc.carrito.exception.ResourceNotFoundException;


@Service
public class CarritoService {

    private final CarritoRepository carritoRepository;
    private final WebClient webClient;

    public CarritoService(
            CarritoRepository carritoRepository,
            WebClient webClient
    ) {
        this.carritoRepository = carritoRepository;
        this.webClient = webClient;
    }

    // LISTAR
    public List<Carrito> getCarritos() {
        return carritoRepository.findAll();
    }

    // GUARDAR
    public Carrito saveCarrito(Carrito carrito) {
        return carritoRepository.save(carrito);
    }

    // BUSCAR POR ID
    public Carrito getCarritoById(int id) {
        return carritoRepository.findById(id)
                .orElse(null);
    }

    // ACTUALIZAR
    public Carrito updateCarrito(
            int id,
            UpdateRequestCarrito request
    ) {

        Carrito carrito = carritoRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Carrito no encontrado"));

        CarritoMapper.updateCarrito(carrito, request);

        return carritoRepository.save(carrito);
    }

    // ELIMINAR
    public boolean deleteCarrito(int id) {

        Optional<Carrito> carrito =
                carritoRepository.findById(id);

        if (carrito.isPresent()) {

            carritoRepository.delete(carrito.get());
            return true;

        } else {

            throw new ResourceNotFoundException(
                    "Carrito no encontrado"
            );
        }
    }

    // BUSCAR POR CLIENTE
    public List<Carrito> buscarPorCliente(
            Integer clienteId
    ) {
        return carritoRepository.buscarPorCliente(clienteId);
    }

    // CAMBIAR ESTADO
    public void cambiarEstado(
            int id,
            String estado
    ) {

        Carrito carrito =
                carritoRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Carrito no encontrado"));

        carrito.setEstado(estado);

        carritoRepository.save(carrito);
    }
}

