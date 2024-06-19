package app.infrastructure.item.ReservaDetalle.dto;


import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;


@Getter
@Setter
@Table(name = "RESERVA_DETALLE")
public class ReservaDetalleDto {


    @Id
    private Integer id;
    @Column("id_reserva")
    private Integer idReserva;
    private Integer id_carta;
    private Double subtotal;
    private Integer cantidad;
    private String estado;
}
