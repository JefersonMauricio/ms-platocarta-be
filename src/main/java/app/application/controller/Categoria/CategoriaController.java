package app.application.controller.Categoria;

import app.application.services.Categoria.CategoriaService;
import app.infrastructure.item.Categoria.dto.CategoriaDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/v1/categoria")
public class CategoriaController {

    private final CategoriaService categoriaService;

    public CategoriaController(CategoriaService categoriaService) {
        this.categoriaService = categoriaService;
    }

    @GetMapping("/obtener/{id}")
    public Mono<CategoriaDto> obtenerCategoriaPorId(@PathVariable Integer id) {
        return categoriaService.obtenerCategoriaPorId(id);
    }

    @GetMapping("/obtener")
    public Flux<CategoriaDto> obtenerCategorias() {
        return categoriaService.obtenerCategorias();
    }

    @GetMapping("/obtener/activo")
    public Flux<CategoriaDto> obtenerCategoriasActivas() {
        return categoriaService.obtenerCategoriasActivas();
    }

    @GetMapping("/obtener/inactivo")
    public Flux<CategoriaDto> obtenerCategoriasInactivas() {
        return categoriaService.obtenerCategoriasInactivas();
    }

    @PostMapping("/crear")
    public Mono<CategoriaDto> insertarCategoria(@RequestBody CategoriaDto categoriaDto) {
        return categoriaService.insertarCategoria(categoriaDto);
    }

    @PutMapping("/editar/{id}")
    public Mono<ResponseEntity<CategoriaDto>> editarCategoriaPorId(@PathVariable Integer id, @RequestBody CategoriaDto categoriaDto) {
        return categoriaService.editarCategoriaPorId(id, categoriaDto);
    }

    @PatchMapping("/desactivar/{id}")
    public Mono<ResponseEntity<String>> desactivarCategoriaPorId(@PathVariable Integer id) {
        return categoriaService.desactivarCategoriaPorId(id);
    }

    @PatchMapping("/restaurar/{id}")
    public Mono<ResponseEntity<String>> restaurarCategoriaPorId(@PathVariable Integer id) {
        return categoriaService.restaurarCategoriaPorId(id);
    }
}