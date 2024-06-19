package app.infrastructure.item.Categoria.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;


@Getter
@Setter
@Table(name = "CATEGORIA")
public class CategoriaDto {

    @Id
    private Integer id;

    private String nombre;

    private String estado;

}
