package app.infrastructure.item.ReservaDetalle.repository;

import app.infrastructure.item.ReservaDetalle.dto.ReservaDetalleDto;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface ReservaDetalleRepository extends ReactiveCrudRepository<ReservaDetalleDto, Integer> {
    Flux<ReservaDetalleDto> findAllByIdReserva(Integer idReserva);


}
