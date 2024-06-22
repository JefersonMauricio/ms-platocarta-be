package app.application.services.PlatoCarta;

import app.infrastructure.item.PlatoCarta.dto.PlatoCartaDto;
import app.infrastructure.item.PlatoCarta.repository.PlatoCartaRepository;
import app.infrastructure.kafka.producer.KafkaProducerService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class PlatoCartaService {

    private final PlatoCartaRepository platoCartaRepository;
    private final KafkaProducerService kafkaProducerService;

    public PlatoCartaService(PlatoCartaRepository platoCartaRepository, KafkaProducerService kafkaProducerService) {
        this.platoCartaRepository = platoCartaRepository;
        this.kafkaProducerService = kafkaProducerService;
    }

    public Mono<PlatoCartaDto> obtenerPlatoCartaPorId(Integer id) {
        return platoCartaRepository.findById(id);
    }

    public Flux<PlatoCartaDto> obtenerPlatosCarta() {
        return platoCartaRepository.findAll();
    }

    public Flux<PlatoCartaDto> obtenerPlatosCartaActivos() {
        return platoCartaRepository.findAll()
                .filter(platoCartaDto -> "A".equals(platoCartaDto.getEstado()));
    }

    public Flux<PlatoCartaDto> obtenerPlatosCartaInactivos() {
        return platoCartaRepository.findAll()
                .filter(platoCartaDto -> "I".equals(platoCartaDto.getEstado()));
    }

    public Mono<PlatoCartaDto> insertarPlatoCarta(PlatoCartaDto platoCartaDto) {
        platoCartaDto.setEstado("A");
        return platoCartaRepository.save(platoCartaDto);
    }

    public Mono<ResponseEntity<PlatoCartaDto>> editarPlatoCartaPorId(Integer id, PlatoCartaDto platoCartaDto) {
        return platoCartaRepository.findById(id)
                .flatMap(existingPlatoDto -> {
                    existingPlatoDto.setNombre(platoCartaDto.getNombre());
                    existingPlatoDto.setDescripcion(platoCartaDto.getDescripcion());
                    existingPlatoDto.setPrecio(platoCartaDto.getPrecio());
                    existingPlatoDto.setId_presentacion(platoCartaDto.getId_presentacion());
                    existingPlatoDto.setId_categoria(platoCartaDto.getId_categoria());
                    existingPlatoDto.setStock(platoCartaDto.getStock());
                    existingPlatoDto.setImage(platoCartaDto.getImage());
                    return platoCartaRepository.save(existingPlatoDto)
                            .map(updatedPlatoDto -> ResponseEntity.ok().body(updatedPlatoDto));
                })
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    public Mono<ResponseEntity<String>> eliminarPlatoCartaPorId(Integer id) {
        return platoCartaRepository.findById(id)
                .flatMap(platoCartaDto -> platoCartaRepository.deleteById(id)
                        .then(Mono.just(ResponseEntity.ok().body("Plato de Carta " + id + " eliminado"))))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    public Mono<ResponseEntity<String>> desactivarPlatoCartaPorId(Integer id) {
        return platoCartaRepository.findById(id)
                .flatMap(platoCartaDto -> {
                    platoCartaDto.setEstado("I"); // Cambio el estado de "A" a "I"
                    return platoCartaRepository.save(platoCartaDto)
                            .then(Mono.just(ResponseEntity.ok().body("Plato de Carta " + id + " desactivado")));
                })
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    public Mono<ResponseEntity<String>> restaurarPlatoCartaPorId(Integer id) {
        return platoCartaRepository.findById(id)
                .flatMap(platoCartaDto -> {
                    platoCartaDto.setEstado("A"); // Cambio el estado de "I" a "A"
                    return platoCartaRepository.save(platoCartaDto)
                            .then(Mono.just(ResponseEntity.ok().body("Plato de Carta " + id + " restaurado")));
                })
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

}