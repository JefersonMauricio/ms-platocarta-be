package app.infrastructure.item.Reserva.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Getter
@Setter
@Table(name = "RESERVA")
public class ReservaDto {


    @Id
    private Integer id;
    @Column("cliente_id")
    private Integer clienteId;
    @Column("restaurante_id")
    private Integer restauranteId;
    private String email;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime fecha_registro;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime fecha_destino;
    private Integer personas;
    private Double monto;
    private String observacion;
    private String situacion;
    private String estado;
}
