package app.application.controller.Categoria;

import app.infrastructure.item.Categoria.dto.CategoriaDto;
import app.infrastructure.item.Categoria.repository.CategoriaRepository;
import app.infrastructure.kafka.producer.KafkaProducerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/v1/categoria")
public class CategoriaController {

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private KafkaProducerService kafkaProducerService; // Inyectar KafkaProducerService


    @GetMapping("/obtener/{id}")
    public Mono<CategoriaDto> obtenerCategoriaPorId(@PathVariable Integer id) {
        return categoriaRepository.findById(id);
    }

    @GetMapping("/obtener")
    public Flux<CategoriaDto> obtenerCategorias() {
        return categoriaRepository.findAll();
    }

    @GetMapping("/obtener/activo")
    public Flux<CategoriaDto> obtenerCategoriasActivas() {
        return categoriaRepository.findAll()
                .filter(categoriaDto -> "A".equals(categoriaDto.getEstado()));
    }

    @GetMapping("/obtener/inactivo")
    public Flux<CategoriaDto> obtenerCategoriasInactivas() {
        return categoriaRepository.findAll()
                .filter(categoriaDto -> "I".equals(categoriaDto.getEstado()));
    }

    @PostMapping("/crear")
    public Mono<CategoriaDto> insertarCategoria(@RequestBody CategoriaDto categoriaDto) {
        categoriaDto.setEstado("A");
        return categoriaRepository.save(categoriaDto);
    }

    @PutMapping("/editar/{id}")
    public Mono<ResponseEntity<CategoriaDto>> editarCategoriaPorId(@PathVariable Integer id, @RequestBody CategoriaDto categoriaDto) {
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


    @PatchMapping("/desactivar/{id}")
    public Mono<ResponseEntity<String>> desactivarCategoriaPorId(@PathVariable Integer id) {
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

    @PatchMapping("/restaurar/{id}")
    public Mono<ResponseEntity<String>> restaurarCategoriaPorId(@PathVariable Integer id) {
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
