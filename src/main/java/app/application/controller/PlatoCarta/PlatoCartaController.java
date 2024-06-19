package app.application.controller.PlatoCarta;

import app.infrastructure.item.Categoria.dto.CategoriaDto;
import app.infrastructure.item.Categoria.repository.CategoriaRepository;
import app.infrastructure.item.PlatoCarta.dto.PlatoCartaDto;
import app.infrastructure.item.PlatoCarta.repository.PlatoCartaRepository;
import app.infrastructure.item.Presentacion.dto.PresentacionDto;
import app.infrastructure.item.Presentacion.repository.PresentacionRepository;
import app.infrastructure.kafka.producer.KafkaProducerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/v1/plato-carta")
public class PlatoCartaController {

    @Autowired
    PresentacionRepository presentacionRepository;
    @Autowired
    CategoriaRepository categoriaRepository;
    @Autowired
    private PlatoCartaRepository platoCartaRepository;
    @Autowired
    private KafkaProducerService kafkaProducerService;


    @GetMapping("/obtener/{id}")
    public Mono<PlatoCartaDto> obtenerPlatoCartaPorId(@PathVariable Integer id) {
        return platoCartaRepository.findById(id);
    }

    @GetMapping("/obtener")
    public Flux<PlatoCartaDto> obtenerPlatosCarta() {
        return platoCartaRepository.findAll();
    }

    @GetMapping("/obtener/activo")
    public Flux<PlatoCartaDto> obtenerPlatosCartaActivos() {
        return platoCartaRepository.findAll()
                .filter(platoCartaDto -> "A".equals(platoCartaDto.getEstado()));
    }

    @GetMapping("/obtener/inactivo")
    public Flux<PlatoCartaDto> obtenerPlatosCartaInactivos() {
        return platoCartaRepository.findAll()
                .filter(platoCartaDto -> "I".equals(platoCartaDto.getEstado()));
    }

    @PostMapping("/crear")
    public Mono<PlatoCartaDto> insertarPlatoCarta(@RequestBody PlatoCartaDto platoCartaDto) {
        platoCartaDto.setEstado("A");

        // Obtener la categoría y presentación correspondientes
        Mono<CategoriaDto> categoriaDtoMono = categoriaRepository.findById(platoCartaDto.getId_categoria());
        Mono<PresentacionDto> presentacionDtoMono = presentacionRepository.findById(platoCartaDto.getId_presentacion());

        // Guardar el plato de carta en la base de datos
        Mono<PlatoCartaDto> savedPlatoCarta = platoCartaRepository.save(platoCartaDto);

        // Combinar el resultado de las consultas de categoría y presentación con el resultado del plato de carta guardado
        Mono<PlatoCartaDto> combinedMono = Mono.zip(savedPlatoCarta, categoriaDtoMono, presentacionDtoMono)
                .flatMap(tuple -> {
                    PlatoCartaDto savedPlato = tuple.getT1();
                    CategoriaDto categoriaDto = tuple.getT2();
                    PresentacionDto presentacionDto = tuple.getT3();
                    kafkaProducerService.enviarProductoATopic(savedPlato, categoriaDto, presentacionDto);
                    return Mono.just(savedPlato);
                });

        return combinedMono;
    }

    @PutMapping("/editar/{id}")
    public Mono<ResponseEntity<PlatoCartaDto>> editarPlatoCartaItemPorId(@PathVariable Integer id, @RequestBody PlatoCartaDto platoCartaDto) {
        return platoCartaRepository.findById(id)
                .flatMap(existingPlatoDto -> {
                    existingPlatoDto.setNombre(platoCartaDto.getNombre());
                    existingPlatoDto.setDescripcion(platoCartaDto.getDescripcion());
                    existingPlatoDto.setPrecio(platoCartaDto.getPrecio());
                    existingPlatoDto.setId_presentacion(platoCartaDto.getId_presentacion());
                    existingPlatoDto.setId_categoria(platoCartaDto.getId_categoria());
                    existingPlatoDto.setStock(platoCartaDto.getStock());
                    existingPlatoDto.setImage(platoCartaDto.getImage()); // Agregar esta línea

                    // Obtener CategoriaDto y PresentacionDto desde sus respectivos repositorios
                    Mono<CategoriaDto> categoriaMono = categoriaRepository.findById(platoCartaDto.getId_categoria());
                    Mono<PresentacionDto> presentacionMono = presentacionRepository.findById(platoCartaDto.getId_presentacion());

                    // Llamar al método para actualizar en Kafka con CategoriaDto y PresentacionDto obtenidos
                    return Mono.zip(categoriaMono, presentacionMono)
                            .flatMap(tuple -> {
                                CategoriaDto categoriaDto = tuple.getT1();
                                PresentacionDto presentacionDto = tuple.getT2();
                                return Mono.just(tuple).then(Mono.just(existingPlatoDto))
                                        .flatMap(plato -> {
                                            kafkaProducerService.actualizarProductoATopic(plato, categoriaDto, presentacionDto);
                                            return platoCartaRepository.save(plato)
                                                    .map(updatedPlatoDto -> ResponseEntity.ok().body(updatedPlatoDto));
                                        });
                            });
                })
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }


    @DeleteMapping("/eliminar/{id}")
    public Mono<ResponseEntity<String>> eliminarPlatoCartaPorId(@PathVariable Integer id) {
        return platoCartaRepository.findById(id)
                .flatMap(platoCartaDto -> platoCartaRepository.deleteById(id)
                        .then(Mono.just(ResponseEntity.ok().body("Plato de Carta " + id + " eliminado"))))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    // Método para desactivar un plato de carta por su ID
    @PatchMapping("/desactivar/{id}")
    public Mono<ResponseEntity<String>> desactivarPlatoCartaPorId(@PathVariable Integer id) {
        return platoCartaRepository.findById(id)
                .flatMap(platoCartaDto -> {
                    Mono<CategoriaDto> categoriaMono = categoriaRepository.findById(platoCartaDto.getId_categoria());
                    Mono<PresentacionDto> presentacionMono = presentacionRepository.findById(platoCartaDto.getId_presentacion());

                    return Mono.zip(categoriaMono, presentacionMono)
                            .flatMap(tuple -> {
                                CategoriaDto categoriaDto = tuple.getT1();
                                PresentacionDto presentacionDto = tuple.getT2();
                                kafkaProducerService.desactivarProductoATopic(platoCartaDto, categoriaDto, presentacionDto); // Llamada al método de KafkaProducerService para desactivar el plato de carta
                                return platoCartaRepository.save(platoCartaDto)
                                        .then(Mono.just(ResponseEntity.ok().body("Plato de Carta " + id + " desactivado")));
                            });
                })
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    // Método para restaurar un plato de carta por su ID
    @PatchMapping("/restaurar/{id}")
    public Mono<ResponseEntity<String>> restaurarPlatoCartaPorId(@PathVariable Integer id) {
        return platoCartaRepository.findById(id)
                .flatMap(platoCartaDto -> {
                    Mono<CategoriaDto> categoriaMono = categoriaRepository.findById(platoCartaDto.getId_categoria());
                    Mono<PresentacionDto> presentacionMono = presentacionRepository.findById(platoCartaDto.getId_presentacion());

                    return Mono.zip(categoriaMono, presentacionMono)
                            .flatMap(tuple -> {
                                CategoriaDto categoriaDto = tuple.getT1();
                                PresentacionDto presentacionDto = tuple.getT2();
                                kafkaProducerService.restaurarProductoATopic(platoCartaDto, categoriaDto, presentacionDto); // Llamada al método de KafkaProducerService para restaurar el plato de carta
                                return platoCartaRepository.save(platoCartaDto)
                                        .then(Mono.just(ResponseEntity.ok().body("Plato de Carta " + id + " restaurado")));
                            });
                })
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }


    @PostMapping("/reservar/{id}/{cantidadreservaplato}")
    public Mono<ResponseEntity<PlatoCartaDto>> reservarPlatoCartaPorId(@PathVariable Integer id, @PathVariable Integer cantidadreservaplato) {
        return platoCartaRepository.findById(id)
                .flatMap(existingPlatoDto -> {
                    int newStock = existingPlatoDto.getStock() - cantidadreservaplato;
                    if (newStock < 0) {
                        return Mono.error(new RuntimeException("Stock insuficiente para realizar la reserva"));
                    }
                    existingPlatoDto.setStock(newStock);

                    // Obtener CategoriaDto y PresentacionDto desde sus respectivos repositorios
                    Mono<CategoriaDto> categoriaMono = categoriaRepository.findById(existingPlatoDto.getId_categoria());
                    Mono<PresentacionDto> presentacionMono = presentacionRepository.findById(existingPlatoDto.getId_presentacion());

                    // Llamar al método para actualizar en Kafka con CategoriaDto y PresentacionDto obtenidos
                    return Mono.zip(categoriaMono, presentacionMono)
                            .flatMap(tuple -> {
                                CategoriaDto categoriaDto = tuple.getT1();
                                PresentacionDto presentacionDto = tuple.getT2();
                                return Mono.just(tuple).then(Mono.just(existingPlatoDto))
                                        .flatMap(plato -> {
                                            kafkaProducerService.actualizarProductoATopic(plato, categoriaDto, presentacionDto);
                                            return platoCartaRepository.save(plato)
                                                    .map(updatedPlatoDto -> ResponseEntity.ok().body(updatedPlatoDto));
                                        });
                            });
                })
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

}
