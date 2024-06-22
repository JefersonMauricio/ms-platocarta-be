package app.application.controller.ProductosItem;

import app.application.services.productositem.ProductosItemService;
import app.domain.ProductosItem;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/v1/productos-carta")
public class TransaccionalProductosItemController {


    private final ProductosItemService productosItemService;

    public TransaccionalProductosItemController(ProductosItemService productosItemService) {
        this.productosItemService = productosItemService;
    }

    @GetMapping("/obtener")
    public Flux<ProductosItem> obtenerPlatosCartaItems() {
        return productosItemService.obtenerPlatosCartaItems();
    }

    @GetMapping("/obtener/{id}")
    public Mono<ProductosItem> obtenerPlatoCartaItemPorId(@PathVariable Integer id) {
        return productosItemService.obtenerPlatoCartaItemPorId(id);
    }
}