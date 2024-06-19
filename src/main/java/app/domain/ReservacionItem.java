package app.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReservacionItem {

    private Integer id;
    private Integer cliente_id;
    private Integer restaurante_id;
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

    private List<ReservaDetalleItem> reserva_detalle;

    @Data
    public static class ReservaDetalleItem {

        private Integer id;
        private Integer id_reserva;
        private Integer id_carta;
        private Double subtotal;
        private Integer cantidad;
        private String estado;

        private PlatoCartaItem plato_carta;

        @Data
        public static class PlatoCartaItem {
            private Integer id;
            private String nombre;
            private double precio;
            private int stock;
            private String estado;
        }
    }
}