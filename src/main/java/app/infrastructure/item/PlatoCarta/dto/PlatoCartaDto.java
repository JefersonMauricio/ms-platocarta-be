package app.infrastructure.item.PlatoCarta.dto;


import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Getter
@Setter
@Table(name = "PLATO_CARTA")
public class PlatoCartaDto {

    @Id
    private Integer id;

    private String nombre;

    private String descripcion;

    private double precio;

    private int id_categoria;

    private int id_presentacion;

    private int stock;

    private String image;

    private String estado;

}
