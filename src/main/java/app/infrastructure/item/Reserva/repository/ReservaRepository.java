package app.infrastructure.item.Reserva.repository;

import app.infrastructure.item.Reserva.dto.ReservaDto;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface ReservaRepository extends ReactiveCrudRepository<ReservaDto, Integer> {

    Flux<ReservaDto> findAllByEstado(String estado);

    Flux<ReservaDto> findAllByRestauranteIdAndEstado(Integer restauranteId, String estado);

    Flux<ReservaDto> findAllByClienteIdAndEstado(Integer clienteId, String estado);

}
