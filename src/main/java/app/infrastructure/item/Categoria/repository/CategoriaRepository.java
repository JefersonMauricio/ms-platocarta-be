package app.infrastructure.item.Categoria.repository;

import app.infrastructure.item.Categoria.dto.CategoriaDto;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface CategoriaRepository extends ReactiveCrudRepository<CategoriaDto, Integer> {
}
