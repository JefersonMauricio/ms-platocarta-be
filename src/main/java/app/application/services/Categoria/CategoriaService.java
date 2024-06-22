package app.application.services.Categoria;

import app.infrastructure.item.Categoria.dto.CategoriaDto;
import app.infrastructure.item.Categoria.repository.CategoriaRepository;
import app.infrastructure.kafka.producer.KafkaProducerService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;
    private final KafkaProducerService kafkaProducerService;

    public CategoriaService(CategoriaRepository categoriaRepository, KafkaProducerService kafkaProducerService) {
        this.categoriaRepository = categoriaRepository;
        this.kafkaProducerService = kafkaProducerService;
    }

    public Mono<CategoriaDto> obtenerCategoriaPorId(Integer id) {
        return categoriaRepository.findById(id);
    }

    public Flux<CategoriaDto> obtenerCategorias() {
        return categoriaRepository.findAll();
    }

    public Flux<CategoriaDto> obtenerCategoriasActivas() {
        return categoriaRepository.findAll()
                .filter(categoriaDto -> "A".equals(categoriaDto.getEstado()));
    }

    public Flux<CategoriaDto> obtenerCategoriasInactivas() {
        return categoriaRepository.findAll()
                .filter(categoriaDto -> "I".equals(categoriaDto.getEstado()));
    }

    public Mono<CategoriaDto> insertarCategoria(CategoriaDto categoriaDto) {
        categoriaDto.setEstado("A");
        return categoriaRepository.save(categoriaDto);
    }

    public Mono<ResponseEntity<CategoriaDto>> editarCategoriaPorId(Integer id, CategoriaDto categoriaDto) {
        String nombre = categoriaDto.getNombre();
        return categoriaRepository.findById(id)
                .flatMap(existingCategoriaDto -> {
                    existingCategoriaDto.setNombre(categoriaDto.getNombre());
                    return categoriaRepository.save(existingCategoriaDto)
                            .doOnSuccess(updatedCategoriaDto -> {
                                kafkaProducerService.enviarCategoriaAKafka(updatedCategoriaDto); // Enviar a Kafka después de guardar
                                System.out.println("Enviando edición de categoría " + nombre + " al tópico ");
                            })
                            .map(updatedCategoriaDto -> ResponseEntity.ok().body(updatedCategoriaDto));
                })
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    public Mono<ResponseEntity<String>> desactivarCategoriaPorId(Integer id) {
        return categoriaRepository.findById(id)
                .flatMap(categoriaDto -> {
                    categoriaDto.setEstado("I"); // Cambio el estado de "A" a "I"
                    return categoriaRepository.save(categoriaDto)
                            .doOnSuccess(updatedCategoriaDto -> {
                                kafkaProducerService.enviarCategoriaAKafka(updatedCategoriaDto); // Enviar a Kafka después de desactivar
                                System.out.println("Enviando desactivación de categoría " + id + " al tópico ");
                            })
                            .then(Mono.just(ResponseEntity.ok().body("Categoría " + id + " desactivada")));
                })
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    public Mono<ResponseEntity<String>> restaurarCategoriaPorId(Integer id) {
        return categoriaRepository.findById(id)
                .flatMap(categoriaDto -> {
                    categoriaDto.setEstado("A"); // Cambio el estado de "I" a "A"
                    return categoriaRepository.save(categoriaDto)
                            .doOnSuccess(updatedCategoriaDto -> {
                                kafkaProducerService.enviarCategoriaAKafka(updatedCategoriaDto); // Enviar a Kafka después de restaurar
                                System.out.println("Enviando restauración de categoría " + id + " al tópico ");
                            })
                            .then(Mono.just(ResponseEntity.ok().body("Categoría " + id + " restaurada")));
                })
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }
}