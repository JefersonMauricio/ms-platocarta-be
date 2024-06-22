package app.application.services.Presentacion;

import app.infrastructure.item.Presentacion.dto.PresentacionDto;
import app.infrastructure.item.Presentacion.repository.PresentacionRepository;
import app.infrastructure.kafka.producer.KafkaProducerService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class PresentacionService {

    private final PresentacionRepository presentacionRepository;
    private final KafkaProducerService kafkaProducerService;

    public PresentacionService(PresentacionRepository presentacionRepository, KafkaProducerService kafkaProducerService) {
        this.presentacionRepository = presentacionRepository;
        this.kafkaProducerService = kafkaProducerService;
    }

    public Mono<PresentacionDto> obtenerPresentacionPorId(Integer id) {
        return presentacionRepository.findById(id);
    }

    public Flux<PresentacionDto> obtenerPresentaciones() {
        return presentacionRepository.findAll();
    }

    public Flux<PresentacionDto> obtenerPresentacionesActivas() {
        return presentacionRepository.findAll()
                .filter(presentacionDto -> "A".equals(presentacionDto.getEstado()));
    }

    public Flux<PresentacionDto> obtenerPresentacionesInactivas() {
        return presentacionRepository.findAll()
                .filter(presentacionDto -> "I".equals(presentacionDto.getEstado()));
    }

    public Mono<PresentacionDto> insertarPresentacion(PresentacionDto presentacionDto) {
        presentacionDto.setEstado("A");
        return presentacionRepository.save(presentacionDto);
    }

    public Mono<ResponseEntity<PresentacionDto>> editarPresentacionPorId(Integer id, PresentacionDto presentacionDto) {
        return presentacionRepository.findById(id)
                .flatMap(existingPresentacionDto -> {
                    existingPresentacionDto.setTipo(presentacionDto.getTipo());
                    return presentacionRepository.save(existingPresentacionDto)
                            .doOnSuccess(updatedPresentacionDto -> {
                                kafkaProducerService.enviarPresentacionAKafka(updatedPresentacionDto); // Enviar a Kafka después de guardar
                            })
                            .map(updatedPresentacionDto -> ResponseEntity.ok().body(updatedPresentacionDto));
                })
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    public Mono<ResponseEntity<String>> desactivarPresentacionPorId(Integer id) {
        return presentacionRepository.findById(id)
                .flatMap(presentacionDto -> {
                    presentacionDto.setEstado("I"); // Cambio el estado de "A" a "I"
                    return presentacionRepository.save(presentacionDto)
                            .doOnSuccess(updatedPresentacionDto -> {
                                kafkaProducerService.enviarPresentacionAKafka(updatedPresentacionDto); // Enviar a Kafka después de desactivar
                            })
                            .then(Mono.just(ResponseEntity.ok().body("Presentacion " + id + " desactivada")));
                })
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    public Mono<ResponseEntity<String>> restaurarPresentacionPorId(Integer id) {
        return presentacionRepository.findById(id)
                .flatMap(presentacionDto -> {
                    presentacionDto.setEstado("A"); // Cambio el estado de "I" a "A"
                    return presentacionRepository.save(presentacionDto)
                            .doOnSuccess(updatedPresentacionDto -> {
                                kafkaProducerService.enviarPresentacionAKafka(updatedPresentacionDto); // Enviar a Kafka después de restaurar
                            })
                            .then(Mono.just(ResponseEntity.ok().body("Presentacion " + id + " restaurada")));
                })
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }
}