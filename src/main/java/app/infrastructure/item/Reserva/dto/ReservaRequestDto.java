package app.infrastructure.item.Reserva.dto;

import app.infrastructure.item.ReservaDetalle.dto.ReservaDetalleDto;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class ReservaRequestDto {
    private Integer cliente_id;
    private Integer restaurante_id;
    private String email;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime fecha_destino;
    private Integer personas;
    private Double monto;
    private String observacion;
    private String situacion;
    private List<ReservaDetalleDto> reserva_detalle;
}