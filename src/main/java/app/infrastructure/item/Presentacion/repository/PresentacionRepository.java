package app.infrastructure.item.Presentacion.repository;

import app.infrastructure.item.Presentacion.dto.PresentacionDto;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface PresentacionRepository extends ReactiveCrudRepository<PresentacionDto, Integer> {
}
