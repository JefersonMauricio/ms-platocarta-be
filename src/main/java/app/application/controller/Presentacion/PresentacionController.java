package app.application.controller.Presentacion;

import app.application.services.Presentacion.PresentacionService;
import app.infrastructure.item.Presentacion.dto.PresentacionDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/v1/presentacion")
public class PresentacionController {

    private final PresentacionService presentacionService;

    public PresentacionController(PresentacionService presentacionService) {
        this.presentacionService = presentacionService;
    }

    @GetMapping("/obtener/{id}")
    public Mono<PresentacionDto> obtenerPresentacionPorId(@PathVariable Integer id) {
        return presentacionService.obtenerPresentacionPorId(id);
    }

    @GetMapping("/obtener")
    public Flux<PresentacionDto> obtenerPresentaciones() {
        return presentacionService.obtenerPresentaciones();
    }

    @GetMapping("/obtener/activo")
    public Flux<PresentacionDto> obtenerPresentacionesActivas() {
        return presentacionService.obtenerPresentacionesActivas();
    }

    @GetMapping("/obtener/inactivo")
    public Flux<PresentacionDto> obtenerPresentacionesInactivas() {
        return presentacionService.obtenerPresentacionesInactivas();
    }

    @PostMapping("/crear")
    public Mono<PresentacionDto> insertarPresentacion(@RequestBody PresentacionDto presentacionDto) {
        return presentacionService.insertarPresentacion(presentacionDto);
    }

    @PutMapping("/editar/{id}")
    public Mono<ResponseEntity<PresentacionDto>> editarPresentacionPorId(@PathVariable Integer id, @RequestBody PresentacionDto presentacionDto) {
        return presentacionService.editarPresentacionPorId(id, presentacionDto);
    }

    @PatchMapping("/desactivar/{id}")
    public Mono<ResponseEntity<String>> desactivarPresentacionPorId(@PathVariable Integer id) {
        return presentacionService.desactivarPresentacionPorId(id);
    }

    @PatchMapping("/restaurar/{id}")
    public Mono<ResponseEntity<String>> restaurarPresentacionPorId(@PathVariable Integer id) {
        return presentacionService.restaurarPresentacionPorId(id);
    }
}