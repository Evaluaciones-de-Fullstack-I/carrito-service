package cl.duoc.carrito.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import cl.duoc.carrito.model.Carrito;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface CarritoRepository extends JpaRepository<Carrito, Integer> {

    // QUERY METHODS

    List<Carrito> findByClienteId(Integer clienteId);

    List<Carrito> findByProductoId(Integer productoId);

    List<Carrito> findByEstado(String estado);

    // CUSTOM QUERY

    @Query(
        value = "SELECT * FROM carrito WHERE cliente_id = :clienteId",
        nativeQuery = true
    )
    List<Carrito> buscarPorCliente(
            @Param("clienteId") Integer clienteId
    );
}

