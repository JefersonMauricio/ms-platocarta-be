package app.application.controller.Presentacion;


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
@RequestMapping("/api/v1/presentacion")
public class PresentacionController {

    @Autowired
    private PresentacionRepository presentacionRepository;

    @Autowired
    private KafkaProducerService kafkaProducerService; // Inyectar KafkaProducerService


    @GetMapping("/obtener/{id}")
    public Mono<PresentacionDto> obtenerPresentacionPorId(@PathVariable Integer id) {
        return presentacionRepository.findById(id);
    }


    @GetMapping("/obtener")
    public Flux<PresentacionDto> obtenerPresentacion() {
        return presentacionRepository.findAll();
    }

    @GetMapping("/obtener/activo")
    public Flux<PresentacionDto> obtenerPresentacionesActivas() {
        return presentacionRepository.findAll()
                .filter(presentacionDto -> "A".equals(presentacionDto.getEstado()));
    }

    @GetMapping("/obtener/inactivo")
    public Flux<PresentacionDto> obtenerPresentacionesInactivas() {
        return presentacionRepository.findAll()
                .filter(presentacionDto -> "I".equals(presentacionDto.getEstado()));
    }


    @PostMapping("/crear")
    public Mono<PresentacionDto> insertarPresentacion(@RequestBody PresentacionDto presentacionDto) {
        presentacionDto.setEstado("A");

        return presentacionRepository.save(presentacionDto);
    }

    @PutMapping("/editar/{id}")
    public Mono<ResponseEntity<PresentacionDto>> editarPresentacionPorId(@PathVariable Integer id, @RequestBody PresentacionDto presentacionDto) {
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


    @PatchMapping("/desactivar/{id}")
    public Mono<ResponseEntity<String>> desactivarPresentacionPorId(@PathVariable Integer id) {
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

    @PatchMapping("/restaurar/{id}")
    public Mono<ResponseEntity<String>> restaurarPresentacionPorId(@PathVariable Integer id) {
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

