package app.infrastructure.kafka.producer;

import app.domain.ProductosItem;
import app.infrastructure.item.Categoria.dto.CategoriaDto;
import app.infrastructure.item.PlatoCarta.dto.PlatoCartaDto;
import app.infrastructure.item.Presentacion.dto.PresentacionDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;


@Service
public class KafkaProducerService {
    private static final String CRMSCREATE_TOPIC = "crmscreate";
    private static final String CRMSEDIT_TOPIC = "crmsedit";

    private static final String PCMS_CTG = "pcms-ctg";

    private static final String PCMS_PRT = "pcms-prt";

    @Autowired
    private KafkaTemplate<String, ProductosItem> kafkaTemplate; // Template de Kafka para enviar mensajes

    @Autowired
    private KafkaTemplate<String, ProductosItem> productosItemKafkaTemplate;

    @Autowired
    private KafkaTemplate<String, CategoriaDto> categoriaDtoKafkaTemplate;

    @Autowired
    private KafkaTemplate<String, PresentacionDto> presentacionDtoKafkaTemplate;

    // Método para enviar un mensaje al tópico "crmscreate" de Kafka
    public void enviarProductoATopic(PlatoCartaDto platoCartaDto, CategoriaDto categoriaDto, PresentacionDto presentacionDto) {
        // Convertir PlatoCartaDto a ProductosItem
        ProductosItem productosItem = convertToProductosItem(platoCartaDto, categoriaDto, presentacionDto);

        // Enviar el mensaje convertido al tópico de Kafka
        System.out.println("Enviando producto al tópico '" + CRMSCREATE_TOPIC + "': " + productosItem.toString());
        kafkaTemplate.send(CRMSCREATE_TOPIC, productosItem);
    }


    public void actualizarProductoATopic(PlatoCartaDto platoCartaDto, CategoriaDto categoriaDto, PresentacionDto presentacionDto) {
        ProductosItem productosItem = convertToProductosItem(platoCartaDto, categoriaDto, presentacionDto);
        kafkaTemplate.send(CRMSEDIT_TOPIC, productosItem);
        System.out.println("Actualizando producto en el tópico '" + CRMSEDIT_TOPIC + "': " + productosItem.toString());
    }

    // Método para desactivar un producto y enviarlo al tópico de Kafka
    public void desactivarProductoATopic(PlatoCartaDto platoCartaDto, CategoriaDto categoriaDto, PresentacionDto presentacionDto) {
        platoCartaDto.setEstado("I"); // Cambiar el estado del plato de carta a "I" (inactivo)
        actualizarProductoATopic(platoCartaDto, categoriaDto, presentacionDto); // Llamar al método para enviar el producto al tópico de Kafka
    }

    // Método para restaurar un producto y enviarlo al tópico de Kafka
    public void restaurarProductoATopic(PlatoCartaDto platoCartaDto, CategoriaDto categoriaDto, PresentacionDto presentacionDto) {
        platoCartaDto.setEstado("A"); // Cambiar el estado del plato de carta a "A" (activo)
        actualizarProductoATopic(platoCartaDto, categoriaDto, presentacionDto); // Llamar al método para enviar el producto al tópico de Kafka
    }


    // Método para convertir PlatoCartaDto a ProductosItem
    private ProductosItem convertToProductosItem(PlatoCartaDto platoCartaDto, CategoriaDto categoriaDto, PresentacionDto presentacionDto) {
        ProductosItem productosItem = new ProductosItem();
        productosItem.setId(platoCartaDto.getId());
        productosItem.setNombre(platoCartaDto.getNombre());
        productosItem.setDescripcion(platoCartaDto.getDescripcion());
        productosItem.setPrecio(platoCartaDto.getPrecio());
        productosItem.setStock(platoCartaDto.getStock());
        productosItem.setImage(platoCartaDto.getImage());
        productosItem.setEstado(platoCartaDto.getEstado());

        // Verificar si categoriaDto es nulo antes de acceder a sus métodos
        if (categoriaDto != null) {
            // Crear y asignar la categoría detalle si está disponible en PlatoCartaDto
            ProductosItem.Categoriadetalle categoriaDetalle = new ProductosItem.Categoriadetalle();
            categoriaDetalle.setId(platoCartaDto.getId_categoria());
            categoriaDetalle.setNombre(categoriaDto.getNombre()); // Agregar el nombre de la categoría si está disponible en PlatoCartaDto
            categoriaDetalle.setEstado(categoriaDto.getEstado()); // Agregar el estado de la categoría si está disponible en PlatoCartaDto
            productosItem.setCategoria_detalle(categoriaDetalle);
        }

        if (presentacionDto != null) {
            // Crear y asignar la presentación detalle si está disponible en PlatoCartaDto
            ProductosItem.Presentaciondetalle presentacionDetalle = new ProductosItem.Presentaciondetalle();
            presentacionDetalle.setId(platoCartaDto.getId_presentacion());
            presentacionDetalle.setTipo(presentacionDto.getTipo()); // Agregar el tipo de presentación si está disponible en PlatoCartaDto
            presentacionDetalle.setEstado(presentacionDto.getEstado()); // Agregar el estado de la presentación si está disponible en PlatoCartaDto
            productosItem.setPresentacion_detalle(presentacionDetalle);
        }

        return productosItem;
    }


    // Método para enviar un mensaje al tópico de Kafka
    public void enviarCategoriaAKafka(CategoriaDto categoriaDto) {
        categoriaDtoKafkaTemplate.send(PCMS_CTG, categoriaDto);
    }

    public void enviarPresentacionAKafka(PresentacionDto presentacionDto) {
        presentacionDtoKafkaTemplate.send(PCMS_PRT, presentacionDto);
    }

}
