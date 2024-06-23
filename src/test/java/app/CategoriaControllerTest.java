package app;

import app.application.controller.Categoria.CategoriaController;
import app.application.services.Categoria.CategoriaService;
import app.infrastructure.item.Categoria.dto.CategoriaDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@WebFluxTest(CategoriaController.class)
class CategoriaControllerTest {

    @MockBean
    private CategoriaService categoriaService;

    private WebTestClient webTestClient;

    @BeforeEach
    void setUp() {
        webTestClient = WebTestClient.bindToController(new CategoriaController(categoriaService)).build();
    }

    @Test
    void testObtenerCategoriaPorId() {
        CategoriaDto categoria = new CategoriaDto();
        categoria.setId(1);
        categoria.setNombre("Test Categoria");

        when(categoriaService.obtenerCategoriaPorId(1)).thenReturn(Mono.just(categoria));

        webTestClient.get()
                .uri("/api/v1/categoria/obtener/1")
                .exchange()
                .expectStatus().isOk()
                .expectBody(CategoriaDto.class)
                .consumeWith(response -> {
                    System.out.println("Categoría obtenida: " + response.getResponseBody());
                });
    }

    @Test
    void testObtenerCategorias() {
        CategoriaDto categoria1 = new CategoriaDto();
        categoria1.setId(1);
        categoria1.setNombre("Categoria 1");

        CategoriaDto categoria2 = new CategoriaDto();
        categoria2.setId(2);
        categoria2.setNombre("Categoria 2");

        when(categoriaService.obtenerCategorias()).thenReturn(Flux.just(categoria1, categoria2));

        webTestClient.get()
                .uri("/api/v1/categoria/obtener")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(CategoriaDto.class)
                .consumeWith(response -> {
                    System.out.println("Categorías obtenidas: " + response.getResponseBody());
                });
    }

    @Test
    void testObtenerCategoriasActivas() {
        CategoriaDto categoria1 = new CategoriaDto();
        categoria1.setId(1);
        categoria1.setNombre("Categoria 1");
        categoria1.setEstado("A");

        CategoriaDto categoria2 = new CategoriaDto();
        categoria2.setId(2);
        categoria2.setNombre("Categoria 2");
        categoria2.setEstado("A");

        when(categoriaService.obtenerCategoriasActivas()).thenReturn(Flux.just(categoria1, categoria2));

        webTestClient.get()
                .uri("/api/v1/categoria/obtener/activo")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(CategoriaDto.class)
                .consumeWith(response -> {
                    System.out.println("Categorías activas: " + response.getResponseBody());
                });
    }

    @Test
    void testObtenerCategoriasInactivas() {
        CategoriaDto categoria1 = new CategoriaDto();
        categoria1.setId(1);
        categoria1.setNombre("Categoria 1");
        categoria1.setEstado("I");

        CategoriaDto categoria2 = new CategoriaDto();
        categoria2.setId(2);
        categoria2.setNombre("Categoria 2");
        categoria2.setEstado("I");

        when(categoriaService.obtenerCategoriasInactivas()).thenReturn(Flux.just(categoria1, categoria2));

        webTestClient.get()
                .uri("/api/v1/categoria/obtener/inactivo")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(CategoriaDto.class)
                .consumeWith(response -> {
                    System.out.println("Categorías inactivas: " + response.getResponseBody());
                });
    }

    @Test
    void testInsertarCategoria() {
        CategoriaDto categoria = new CategoriaDto();
        categoria.setNombre("Nueva Categoria");

        when(categoriaService.insertarCategoria(any(CategoriaDto.class))).thenReturn(Mono.just(categoria));

        webTestClient.post()
                .uri("/api/v1/categoria/crear")
                .contentType(APPLICATION_JSON)
                .bodyValue(categoria)
                .exchange()
                .expectStatus().isOk()
                .expectBody(CategoriaDto.class)
                .consumeWith(response -> {
                    System.out.println("Categoría insertada: " + response.getResponseBody());
                });
    }

    @Test
    void testEditarCategoriaPorId() {
        CategoriaDto categoria = new CategoriaDto();
        categoria.setId(1);
        categoria.setNombre("Categoria Editada");

        when(categoriaService.editarCategoriaPorId(anyInt(), any(CategoriaDto.class)))
                .thenReturn(Mono.just(ResponseEntity.ok(categoria)));

        webTestClient.put()
                .uri("/api/v1/categoria/editar/1")
                .contentType(APPLICATION_JSON)
                .bodyValue(categoria)
                .exchange()
                .expectStatus().isOk()
                .expectBody(CategoriaDto.class)
                .consumeWith(response -> {
                    System.out.println("Categoría editada: " + response.getResponseBody());
                });
    }

    @Test
    void testDesactivarCategoriaPorId() {
        when(categoriaService.desactivarCategoriaPorId(1))
                .thenReturn(Mono.just(ResponseEntity.ok("Categoría 1 desactivada")));

        webTestClient.patch()
                .uri("/api/v1/categoria/desactivar/1")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .consumeWith(response -> {
                    System.out.println("Respuesta desactivación: " + response.getResponseBody());
                });
    }

    @Test
    void testRestaurarCategoriaPorId() {
        when(categoriaService.restaurarCategoriaPorId(1))
                .thenReturn(Mono.just(ResponseEntity.ok("Categoría 1 restaurada")));

        webTestClient.patch()
                .uri("/api/v1/categoria/restaurar/1")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .consumeWith(response -> {
                    System.out.println("Respuesta restauración: " + response.getResponseBody());
                });
    }
}
