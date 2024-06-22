package app.application.controller.PlatoCarta;

import app.application.services.PlatoCarta.PlatoCartaService;
import app.infrastructure.item.PlatoCarta.dto.PlatoCartaDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/v1/plato-carta")
public class PlatoCartaController {

    private final PlatoCartaService platoCartaService;

    public PlatoCartaController(PlatoCartaService platoCartaService) {
        this.platoCartaService = platoCartaService;
    }

    @GetMapping("/obtener/{id}")
    public Mono<PlatoCartaDto> obtenerPlatoCartaPorId(@PathVariable Integer id) {
        return platoCartaService.obtenerPlatoCartaPorId(id);
    }

    @GetMapping("/obtener")
    public Flux<PlatoCartaDto> obtenerPlatosCarta() {
        return platoCartaService.obtenerPlatosCarta();
    }

    @GetMapping("/obtener/activo")
    public Flux<PlatoCartaDto> obtenerPlatosCartaActivos() {
        return platoCartaService.obtenerPlatosCartaActivos();
    }

    @GetMapping("/obtener/inactivo")
    public Flux<PlatoCartaDto> obtenerPlatosCartaInactivos() {
        return platoCartaService.obtenerPlatosCartaInactivos();
    }

    @PostMapping("/crear")
    public Mono<PlatoCartaDto> insertarPlatoCarta(@RequestBody PlatoCartaDto platoCartaDto) {
        return platoCartaService.insertarPlatoCarta(platoCartaDto);
    }

    @PutMapping("/editar/{id}")
    public Mono<ResponseEntity<PlatoCartaDto>> editarPlatoCartaPorId(@PathVariable Integer id, @RequestBody PlatoCartaDto platoCartaDto) {
        return platoCartaService.editarPlatoCartaPorId(id, platoCartaDto);
    }

    @DeleteMapping("/eliminar/{id}")
    public Mono<ResponseEntity<String>> eliminarPlatoCartaPorId(@PathVariable Integer id) {
        return platoCartaService.eliminarPlatoCartaPorId(id);
    }

    @PatchMapping("/desactivar/{id}")
    public Mono<ResponseEntity<String>> desactivarPlatoCartaPorId(@PathVariable Integer id) {
        return platoCartaService.desactivarPlatoCartaPorId(id);
    }

    @PatchMapping("/restaurar/{id}")
    public Mono<ResponseEntity<String>> restaurarPlatoCartaPorId(@PathVariable Integer id) {
        return platoCartaService.restaurarPlatoCartaPorId(id);
    }

}