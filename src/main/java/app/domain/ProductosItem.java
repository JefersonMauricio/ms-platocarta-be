package app.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProductosItem {

    private int id;

    private String nombre;

    private String descripcion;

    private double precio;

    private Categoriadetalle categoria_detalle;

    private Presentaciondetalle presentacion_detalle;

    private int stock;

    private String image;

    private String estado;

    private  int id_restaurante;

    @Data
    public static class Categoriadetalle {

        private int id;

        private String nombre;

        private String estado;

    }

    @Data
    public static class Presentaciondetalle {

        private int id;

        private String tipo;

        private String estado;


    }


}
