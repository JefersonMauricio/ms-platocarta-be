package app.application.controller.ProductosItem;


import app.domain.ProductosItem;
import app.infrastructure.item.Categoria.dto.CategoriaDto;
import app.infrastructure.item.Categoria.repository.CategoriaRepository;
import app.infrastructure.item.PlatoCarta.dto.PlatoCartaDto;
import app.infrastructure.item.PlatoCarta.repository.PlatoCartaRepository;
import app.infrastructure.item.Presentacion.dto.PresentacionDto;
import app.infrastructure.item.Presentacion.repository.PresentacionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/v1/productos-carta")
public class TransaccionalProductosItemController {


    @Autowired
    private PlatoCartaRepository platoCartaRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private PresentacionRepository presentacionRepository;

    @GetMapping("/obtener")
    public Flux<ProductosItem> obtenerPlatosCartaItems() {
        return platoCartaRepository.findAll()
                .flatMap(platoCartaDto -> constructPlatoCartaItem(platoCartaDto));
    }


    @GetMapping("/obtener/{id}")
    public Mono<ProductosItem> obtenerPlatoCartaItemPorId(@PathVariable Integer id) {
        return platoCartaRepository.findById(id)
                .flatMap(platoCartaDto -> constructPlatoCartaItem(platoCartaDto));
    }

    private Mono<ProductosItem> constructPlatoCartaItem(PlatoCartaDto platoCartaDto) {
        return Mono.zip(
                        Mono.just(platoCartaDto),
                        categoriaRepository.findById(platoCartaDto.getId_categoria()),
                        presentacionRepository.findById(platoCartaDto.getId_presentacion())
                )
                .map(tuple -> {
                    PlatoCartaDto platoDto = tuple.getT1();
                    CategoriaDto categoriaDto = tuple.getT2();
                    PresentacionDto presentacionDto = tuple.getT3();

                    ProductosItem.Categoriadetalle categoriaDetalle = new ProductosItem.Categoriadetalle();
                    categoriaDetalle.setId(categoriaDto.getId());
                    categoriaDetalle.setNombre(categoriaDto.getNombre());
                    categoriaDetalle.setEstado(categoriaDto.getEstado());

                    ProductosItem.Presentaciondetalle presentacionDetalle = new ProductosItem.Presentaciondetalle();
                    presentacionDetalle.setId(presentacionDto.getId());
                    presentacionDetalle.setTipo(presentacionDto.getTipo());
                    presentacionDetalle.setEstado(presentacionDto.getEstado());

                    ProductosItem productosItem = new ProductosItem();
                    productosItem.setId(platoDto.getId());
                    productosItem.setNombre(platoDto.getNombre());
                    productosItem.setDescripcion(platoDto.getDescripcion());
                    productosItem.setPrecio(platoDto.getPrecio());
                    productosItem.setCategoria_detalle(categoriaDetalle);
                    productosItem.setPresentacion_detalle(presentacionDetalle);
                    productosItem.setStock(platoDto.getStock());
                    productosItem.setImage(platoDto.getImage());
                    productosItem.setEstado(platoDto.getEstado());

                    return productosItem;
                });
    }
}
