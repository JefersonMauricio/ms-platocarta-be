package app.application.services.productositem;

import app.domain.ProductosItem;
import app.infrastructure.item.Categoria.dto.CategoriaDto;
import app.infrastructure.item.Categoria.repository.CategoriaRepository;
import app.infrastructure.item.PlatoCarta.dto.PlatoCartaDto;
import app.infrastructure.item.PlatoCarta.repository.PlatoCartaRepository;
import app.infrastructure.item.Presentacion.dto.PresentacionDto;
import app.infrastructure.item.Presentacion.repository.PresentacionRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class ProductosItemService {

    private final PlatoCartaRepository platoCartaRepository;

    private final CategoriaRepository categoriaRepository;

    private final PresentacionRepository presentacionRepository;

    public ProductosItemService(PlatoCartaRepository platoCartaRepository, CategoriaRepository categoriaRepository, PresentacionRepository presentacionRepository) {
        this.platoCartaRepository = platoCartaRepository;
        this.categoriaRepository = categoriaRepository;
        this.presentacionRepository = presentacionRepository;
    }

    public Flux<ProductosItem> obtenerPlatosCartaItems() {
        return platoCartaRepository.findAll()
                .flatMap(this::constructPlatoCartaItem);
    }

    public Mono<ProductosItem> obtenerPlatoCartaItemPorId(Integer id) {
        return platoCartaRepository.findById(id)
                .flatMap(this::constructPlatoCartaItem);
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
                    productosItem.setId_restaurante(platoDto.getId_restaurante());
                    productosItem.setCategoria_detalle(categoriaDetalle);
                    productosItem.setPresentacion_detalle(presentacionDetalle);
                    productosItem.setStock(platoDto.getStock());
                    productosItem.setImage(platoDto.getImage());
                    productosItem.setEstado(platoDto.getEstado());

                    return productosItem;
                });
    }
}