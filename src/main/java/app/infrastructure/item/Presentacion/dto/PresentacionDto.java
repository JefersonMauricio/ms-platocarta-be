package app.infrastructure.item.Presentacion.dto;


import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Getter
@Setter
@Table(name = "PRESENTACION")
public class PresentacionDto {

    @Id
    private Integer id;

    private String tipo;

    private String estado;

}
