package app.infrastructure.item.PlatoCarta.repository;

import app.infrastructure.item.PlatoCarta.dto.PlatoCartaDto;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface PlatoCartaRepository extends ReactiveCrudRepository<PlatoCartaDto, Integer> {

    @Modifying
    @Query("UPDATE PLATO_CARTA SET estado = :estado WHERE id = :id")
    Mono<Void> updateEstadoById(@Param("id") Integer id, @Param("estado") String estado);

}
