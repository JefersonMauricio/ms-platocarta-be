package app;

import app.application.services.Categoria.CategoriaService;
import app.infrastructure.item.Categoria.dto.CategoriaDto;
import app.infrastructure.item.Categoria.repository.CategoriaRepository;
import app.infrastructure.kafka.producer.KafkaProducerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

class CategoriaServiceTest {

    @Mock
    private CategoriaRepository categoriaRepository;

    @Mock
    private KafkaProducerService kafkaProducerService;

    private CategoriaService categoriaService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        categoriaService = new CategoriaService(categoriaRepository, kafkaProducerService);
    }

    @Test
    void testObtenerCategoriaPorId() {
        CategoriaDto categoria = new CategoriaDto();
        categoria.setId(1);
        categoria.setNombre("Categoria 1");
        categoria.setEstado("A");

        when(categoriaRepository.findById(1)).thenReturn(Mono.just(categoria));

        Mono<CategoriaDto> result = categoriaService.obtenerCategoriaPorId(1);

        StepVerifier.create(result)
                .expectNextMatches(c -> {
                    System.out.println("Categoría obtenida: " + c);
                    return c.getId().equals(1) && c.getNombre().equals("Categoria 1");
                })
                .verifyComplete();
    }

    @Test
    void testObtenerCategorias() {
        CategoriaDto categoria1 = new CategoriaDto();
        categoria1.setId(1);
        categoria1.setNombre("Categoria 1");
        categoria1.setEstado("A");

        CategoriaDto categoria2 = new CategoriaDto();
        categoria2.setId(2);
        categoria2.setNombre("Categoria 2");
        categoria2.setEstado("I");

        when(categoriaRepository.findAll()).thenReturn(Flux.just(categoria1, categoria2));

        Flux<CategoriaDto> result = categoriaService.obtenerCategorias();

        StepVerifier.create(result)
                .expectNextMatches(c -> {
                    System.out.println("Categoría obtenida: " + c);
                    return c.getId().equals(1) && c.getNombre().equals("Categoria 1");
                })
                .expectNextMatches(c -> {
                    System.out.println("Categoría obtenida: " + c);
                    return c.getId().equals(2) && c.getNombre().equals("Categoria 2");
                })
                .verifyComplete();
    }

    @Test
    void testObtenerCategoriasActivas() {
        CategoriaDto categoria = new CategoriaDto();
        categoria.setId(1);
        categoria.setNombre("Categoria 1");
        categoria.setEstado("A");

        when(categoriaRepository.findAll()).thenReturn(Flux.just(categoria));

        Flux<CategoriaDto> result = categoriaService.obtenerCategoriasActivas();

        StepVerifier.create(result)
                .expectNextMatches(c -> {
                    System.out.println("Categoría activa obtenida: " + c);
                    return "A".equals(c.getEstado());
                })
                .verifyComplete();
    }

    @Test
    void testObtenerCategoriasInactivas() {
        CategoriaDto categoria = new CategoriaDto();
        categoria.setId(1);
        categoria.setNombre("Categoria 1");
        categoria.setEstado("I");

        when(categoriaRepository.findAll()).thenReturn(Flux.just(categoria));

        Flux<CategoriaDto> result = categoriaService.obtenerCategoriasInactivas();

        StepVerifier.create(result)
                .expectNextMatches(c -> {
                    System.out.println("Categoría inactiva obtenida: " + c);
                    return "I".equals(c.getEstado());
                })
                .verifyComplete();
    }

    @Test
    void testInsertarCategoria() {
        CategoriaDto categoria = new CategoriaDto();
        categoria.setNombre("Nueva Categoria");

        when(categoriaRepository.save(any(CategoriaDto.class))).thenReturn(Mono.just(categoria));

        Mono<CategoriaDto> result = categoriaService.insertarCategoria(categoria);

        StepVerifier.create(result)
                .expectNextMatches(c -> {
                    System.out.println("Categoría insertada: " + c);
                    return "Nueva Categoria".equals(c.getNombre());
                })
                .verifyComplete();
    }

    @Test
    void testEditarCategoriaPorId() {
        CategoriaDto categoria = new CategoriaDto();
        categoria.setId(1);
        categoria.setNombre("Categoria Editada");

        when(categoriaRepository.findById(1)).thenReturn(Mono.just(categoria));
        when(categoriaRepository.save(any(CategoriaDto.class))).thenReturn(Mono.just(categoria));

        Mono<ResponseEntity<CategoriaDto>> result = categoriaService.editarCategoriaPorId(1, categoria);

        StepVerifier.create(result)
                .expectNextMatches(response -> {
                    System.out.println("Categoría editada: " + response.getBody());
                    return response.getBody().getNombre().equals("Categoria Editada");
                })
                .verifyComplete();
    }

    @Test
    void testDesactivarCategoriaPorId() {
        CategoriaDto categoria = new CategoriaDto();
        categoria.setId(1);
        categoria.setEstado("A");

        when(categoriaRepository.findById(1)).thenReturn(Mono.just(categoria));
        when(categoriaRepository.save(any(CategoriaDto.class))).thenReturn(Mono.just(categoria));

        Mono<ResponseEntity<String>> result = categoriaService.desactivarCategoriaPorId(1);

        StepVerifier.create(result)
                .expectNextMatches(response -> {
                    System.out.println("Categoría desactivada: " + response.getBody());
                    return response.getBody().equals("Categoría 1 desactivada");
                })
                .verifyComplete();
    }

    @Test
    void testRestaurarCategoriaPorId() {
        CategoriaDto categoria = new CategoriaDto();
        categoria.setId(1);
        categoria.setEstado("I");

        when(categoriaRepository.findById(1)).thenReturn(Mono.just(categoria));
        when(categoriaRepository.save(any(CategoriaDto.class))).thenReturn(Mono.just(categoria));

        Mono<ResponseEntity<String>> result = categoriaService.restaurarCategoriaPorId(1);

        StepVerifier.create(result)
                .expectNextMatches(response -> {
                    System.out.println("Categoría restaurada: " + response.getBody());
                    return response.getBody().equals("Categoría 1 restaurada");
                })
                .verifyComplete();
    }
}
