package cl.duoc.carrito.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;



 @Configuration
public class WebClientConfig {


 @Bean //ejemplo de configuracion de un webclient para consumir una api externa
    public WebClient webClient(WebClient.Builder builder) {
        return builder.baseUrl("\"/api/productos/{id}") // <-- Conectado directamente a tu Catálogo
                .build();

    }
}
