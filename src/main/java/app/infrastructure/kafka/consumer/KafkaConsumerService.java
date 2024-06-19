package app.infrastructure.kafka.consumer;

import app.domain.ProductosItem;
import app.infrastructure.item.Categoria.dto.CategoriaDto;
import app.infrastructure.item.Categoria.repository.CategoriaRepository;
import app.infrastructure.item.PlatoCarta.dto.PlatoCartaDto;
import app.infrastructure.item.PlatoCarta.repository.PlatoCartaRepository;
import app.infrastructure.item.Presentacion.dto.PresentacionDto;
import app.infrastructure.item.Presentacion.repository.PresentacionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class KafkaConsumerService {

    @Autowired
    private KafkaTemplate<String, ProductosItem> kafkaTemplate;

    @Autowired
    private PlatoCartaRepository platoCartaRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private PresentacionRepository presentacionRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @KafkaListener(topics = "pcmscreate", groupId = "my-consumer-group")
    public void consumeCreate(ConsumerRecord<String, String> record) {
        try {
            String jsonString = record.value();
            PlatoCartaDto platoCartaDto = objectMapper.readValue(jsonString, PlatoCartaDto.class);

            // Guardar el plato de carta en la base de datos
            platoCartaRepository.save(platoCartaDto)
                    .flatMap(savedPlatoCarta -> {
                        // Obtener la categoría y presentación correspondientes
                        Mono<CategoriaDto> categoriaDtoMono = categoriaRepository.findById(savedPlatoCarta.getId_categoria());
                        Mono<PresentacionDto> presentacionDtoMono = presentacionRepository.findById(savedPlatoCarta.getId_presentacion());

                        // Combinar el resultado de las consultas de categoría y presentación con el resultado del plato de carta guardado
                        return Mono.zip(categoriaDtoMono, presentacionDtoMono)
                                .flatMap(tuple -> {
                                    CategoriaDto categoriaDto = tuple.getT1();
                                    PresentacionDto presentacionDto = tuple.getT2();

                                    // Convertir el platoCartaDto a ProductosItem
                                    ProductosItem productosItem = convertToProductosItem(savedPlatoCarta, categoriaDto, presentacionDto);

                                    // Enviar el mensaje convertido al tópico de Kafka
                                    kafkaTemplate.send("crmscreate", productosItem);
                                    System.out.println("Enviando producto al tópico '" + "crmscreate" + "': " + productosItem.toString());

                                    return Mono.just(savedPlatoCarta);
                                });
                    })
                    .subscribe(); // Utiliza subscribe para activar la operación
        } catch (Exception e) {
            // Manejar la excepción según tus necesidades
            e.printStackTrace();
        }
    }


    @KafkaListener(topics = "pcmsedit", groupId = "my-consumer-group")
    public void consumeEdit(ConsumerRecord<String, String> record) {
        try {
            String jsonString = record.value();
            PlatoCartaDto platoCartaDto = objectMapper.readValue(jsonString, PlatoCartaDto.class);

            // Actualizar el plato de carta en la base de datos
            platoCartaRepository.save(platoCartaDto)
                    .flatMap(updatedPlatoCarta -> {
                        // Obtener la categoría y presentación correspondientes
                        Mono<CategoriaDto> categoriaDtoMono = categoriaRepository.findById(updatedPlatoCarta.getId_categoria());
                        Mono<PresentacionDto> presentacionDtoMono = presentacionRepository.findById(updatedPlatoCarta.getId_presentacion());

                        // Combinar el resultado de las consultas de categoría y presentación con el resultado del plato de carta actualizado
                        return Mono.zip(categoriaDtoMono, presentacionDtoMono)
                                .flatMap(tuple -> {
                                    CategoriaDto categoriaDto = tuple.getT1();
                                    PresentacionDto presentacionDto = tuple.getT2();

                                    // Convertir el platoCartaDto a ProductosItem
                                    ProductosItem productosItem = convertToProductosItem(updatedPlatoCarta, categoriaDto, presentacionDto);

                                    // Enviar el mensaje convertido al tópico de Kafka
                                    kafkaTemplate.send("crmsedit", productosItem);
                                    System.out.println("Enviando producto al tópico '" + "crmsedit" + "': " + productosItem.toString());

                                    return Mono.just(updatedPlatoCarta);
                                });
                    })
                    .subscribe(); // Utiliza subscribe para activar la operación
        } catch (Exception e) {
            // Manejar la excepción según tus necesidades
            e.printStackTrace();
        }
    }

    @KafkaListener(topics = "pcmsdesactivate", groupId = "my-consumer-group")
    public void consumeDesactivate(ConsumerRecord<String, String> record) {
        try {
            String jsonString = record.value();
            PlatoCartaDto platoCartaDto = objectMapper.readValue(jsonString, PlatoCartaDto.class);

            // Buscar el plato de carta en la base de datos usando el id
            platoCartaRepository.findById(platoCartaDto.getId())
                    .flatMap(existingPlatoCarta -> {
                        // Cambiar el estado del plato de carta a "I" (inactivo)
                        existingPlatoCarta.setEstado("I");

                        // Actualizar el estado del plato de carta en la base de datos
                        return platoCartaRepository.updateEstadoById(existingPlatoCarta.getId(), existingPlatoCarta.getEstado())
                                .then(Mono.just(existingPlatoCarta));
                    })
                    .flatMap(updatedPlatoCarta -> {
                        // Obtener la categoría y presentación correspondientes
                        Mono<CategoriaDto> categoriaDtoMono = categoriaRepository.findById(updatedPlatoCarta.getId_categoria());
                        Mono<PresentacionDto> presentacionDtoMono = presentacionRepository.findById(updatedPlatoCarta.getId_presentacion());

                        // Combinar el resultado de las consultas de categoría y presentación con el resultado del plato de carta
                        return Mono.zip(categoriaDtoMono, presentacionDtoMono, Mono.just(updatedPlatoCarta));
                    })
                    .flatMap(tuple -> {
                        CategoriaDto categoriaDto = tuple.getT1();
                        PresentacionDto presentacionDto = tuple.getT2();
                        PlatoCartaDto updatedPlatoCarta = tuple.getT3();

                        // Convertir el platoCartaDto a ProductosItem
                        ProductosItem productosItem = convertToProductosItem(updatedPlatoCarta, categoriaDto, presentacionDto);

                        // Enviar el mensaje convertido al tópico de Kafka
                        kafkaTemplate.send("crmsedit", productosItem);
                        System.out.println("Enviando producto desactivado al tópico '" + "crmsedit" + "': " + productosItem.toString());

                        return Mono.just(updatedPlatoCarta);
                    })
                    .subscribe(); // Utiliza subscribe para activar la operación
        } catch (Exception e) {
            // Manejar la excepción según tus necesidades
            e.printStackTrace();
        }
    }


    @KafkaListener(topics = "pcmsrestore", groupId = "my-consumer-group")
    public void consumeRestore(ConsumerRecord<String, String> record) {
        try {
            String jsonString = record.value();
            PlatoCartaDto platoCartaDto = objectMapper.readValue(jsonString, PlatoCartaDto.class);

            // Buscar el plato de carta en la base de datos usando el id
            platoCartaRepository.findById(platoCartaDto.getId())
                    .flatMap(existingPlatoCarta -> {
                        // Cambiar el estado del plato de carta a "A" (activo)
                        existingPlatoCarta.setEstado("A");

                        // Actualizar el estado del plato de carta en la base de datos
                        return platoCartaRepository.updateEstadoById(existingPlatoCarta.getId(), existingPlatoCarta.getEstado())
                                .then(Mono.just(existingPlatoCarta));
                    })
                    .flatMap(updatedPlatoCarta -> {
                        // Obtener la categoría y presentación correspondientes
                        Mono<CategoriaDto> categoriaDtoMono = categoriaRepository.findById(updatedPlatoCarta.getId_categoria());
                        Mono<PresentacionDto> presentacionDtoMono = presentacionRepository.findById(updatedPlatoCarta.getId_presentacion());

                        // Combinar el resultado de las consultas de categoría y presentación con el resultado del plato de carta
                        return Mono.zip(categoriaDtoMono, presentacionDtoMono, Mono.just(updatedPlatoCarta));
                    })
                    .flatMap(tuple -> {
                        CategoriaDto categoriaDto = tuple.getT1();
                        PresentacionDto presentacionDto = tuple.getT2();
                        PlatoCartaDto updatedPlatoCarta = tuple.getT3();

                        // Convertir el platoCartaDto a ProductosItem
                        ProductosItem productosItem = convertToProductosItem(updatedPlatoCarta, categoriaDto, presentacionDto);

                        // Enviar el mensaje convertido al tópico de Kafka
                        kafkaTemplate.send("crmsedit", productosItem);
                        System.out.println("Enviando producto restaurado al tópico '" + "crmsedit" + "': " + productosItem.toString());

                        return Mono.just(updatedPlatoCarta);
                    })
                    .subscribe(); // Utiliza subscribe para activar la operación
        } catch (Exception e) {
            // Manejar la excepción según tus necesidades
            e.printStackTrace();
        }
    }


    private ProductosItem convertToProductosItem(PlatoCartaDto platoCartaDto, CategoriaDto categoriaDto, PresentacionDto presentacionDto) {
        ProductosItem productosItem = new ProductosItem();
        productosItem.setId(platoCartaDto.getId());
        productosItem.setNombre(platoCartaDto.getNombre());
        productosItem.setDescripcion(platoCartaDto.getDescripcion());
        productosItem.setPrecio(platoCartaDto.getPrecio());
        productosItem.setStock(platoCartaDto.getStock());
        productosItem.setImage(platoCartaDto.getImage());
        productosItem.setEstado(platoCartaDto.getEstado());

        if (categoriaDto != null) {
            ProductosItem.Categoriadetalle categoriaDetalle = new ProductosItem.Categoriadetalle();
            categoriaDetalle.setId(categoriaDto.getId());
            categoriaDetalle.setNombre(categoriaDto.getNombre());
            categoriaDetalle.setEstado(categoriaDto.getEstado());
            productosItem.setCategoria_detalle(categoriaDetalle);
        }

        if (presentacionDto != null) {
            ProductosItem.Presentaciondetalle presentacionDetalle = new ProductosItem.Presentaciondetalle();
            presentacionDetalle.setId(presentacionDto.getId());
            presentacionDetalle.setTipo(presentacionDto.getTipo());
            presentacionDetalle.setEstado(presentacionDto.getEstado());
            productosItem.setPresentacion_detalle(presentacionDetalle);
        }

        return productosItem;
    }


}
