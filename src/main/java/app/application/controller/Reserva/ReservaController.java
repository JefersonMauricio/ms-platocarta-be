package app.application.controller.Reserva;

import app.application.services.Reserva.ReservaService;
import app.domain.ReservacionItem;
import app.infrastructure.item.Reserva.dto.ReservaDto;
import app.infrastructure.item.Reserva.dto.ReservaRequestDto;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/v1/reserva")
public class ReservaController {

    private final ReservaService reservaService;

    public ReservaController(ReservaService reservaService) {
        this.reservaService = reservaService;
    }

    // Obtener todas las reservas
    @GetMapping("/obtener/todos")
    public Flux<ReservacionItem> getAllReservacionItems() {
        return reservaService.getAllReservacionItems();
    }

    // Obtener reservas por id
    @GetMapping("/obtener/{id}")
    public Mono<ReservacionItem> getReservacionItemById(@PathVariable Integer id) {
        return reservaService.getReservacionItemById(id);
    }

    // Obtener todas las reservas activas
    @GetMapping("/obtener/activo")
    public Flux<ReservacionItem> getReservacionItemsActivos() {
        return reservaService.getReservacionItemsByEstado("A");
    }


    // Obtener todas las reservas inactivas
    @GetMapping("/obtener/inactivo")
    public Flux<ReservacionItem> getReservacionItemsInactivos() {
        return reservaService.getReservacionItemsByEstado("I");
    }


    // Obtener reservas activas por restaurante 
    @GetMapping("/obtener/restaurante/{restaurante_id}/activo")
    public Flux<ReservacionItem> getReservacionItemsActivosByRestauranteId(@PathVariable Integer restaurante_id) {
        return reservaService.getReservacionItemsByRestauranteIdAndEstado(restaurante_id, "A");
    }

    // Obtener reservas inactivas por restaurante
    @GetMapping("/obtener/restaurante/{restaurante_id}/inactivo")
    public Flux<ReservacionItem> getReservacionItemsInactivosByRestauranteId(@PathVariable Integer restaurante_id) {
        return reservaService.getReservacionItemsByRestauranteIdAndEstado(restaurante_id, "I");
    }

    // Obtener reservas especificas del restaurante
    @GetMapping("/obtener/restaurante/{restaurante_id}/reserva/{id}")
    public Mono<ReservacionItem> getReservacionItemByRestauranteIdAndReservaId(@PathVariable Integer restaurante_id, @PathVariable Integer id) {
        return reservaService.getReservacionItemByRestauranteIdAndReservaId(restaurante_id, id);
    }

    // Obtener reservas activas del cliente
    @GetMapping("/obtener/cliente/{cliente_id}/activo")
    public Flux<ReservacionItem> getReservacionItemsActivosByClienteId(@PathVariable Integer cliente_id) {
        return reservaService.getReservacionItemsByClienteIdAndEstado(cliente_id, "A");
    }

    // Obtener reservas inactivas del cliente
    @GetMapping("/obtener/cliente/{cliente_id}/inactivo")
    public Flux<ReservacionItem> getReservacionItemsInactivosByClienteId(@PathVariable Integer cliente_id) {
        return reservaService.getReservacionItemsByClienteIdAndEstado(cliente_id, "I");
    }

    // Obtener reservas especificas del cliente
    @GetMapping("/obtener/cliente/{cliente_id}/reserva/{id}")
    public Mono<ReservacionItem> getReservacionItemByClienteIdAndReservaId(@PathVariable Integer cliente_id, @PathVariable Integer id) {
        return reservaService.getReservacionItemByClienteIdAndReservaId(cliente_id, id);
    }


    // Creacion de una reserva
    @PostMapping("/crear")
    public Mono<ReservacionItem> crearReserva(@RequestBody ReservaRequestDto reservaRequestDto) {
        return reservaService.createReserva(reservaRequestDto);
    }

    //  Creacion de una reserva para restaurante
    @PostMapping("/crear/restaurante/{restaurante_id}")
    public Mono<ReservacionItem> createReservaByRestauranteId(@PathVariable Integer restaurante_id, @RequestBody ReservaRequestDto reservaRequestDto) {
        reservaRequestDto.setRestaurante_id(restaurante_id);
        return reservaService.createReserva(reservaRequestDto);
    }

    //  Creacion de una reserva para el cliente
    @PostMapping("/crear/cliente/{cliente_id}")
    public Mono<ReservacionItem> createReservaByClienteId(@PathVariable Integer cliente_id, @RequestBody ReservaRequestDto reservaRequestDto) {
        reservaRequestDto.setCliente_id(cliente_id);
        return reservaService.createReserva(reservaRequestDto);
    }


    // Editar una reserva
    @PutMapping("/editar/{id}")
    public Mono<ReservacionItem> editReservaById(@PathVariable Integer id, @RequestBody ReservaRequestDto reservaRequestDto) {
        return reservaService.editReservaById(id, reservaRequestDto);
    }

    // Editar una reserva especifica del restaurante
    @PutMapping("/editar/restaurante/{restaurante_id}/reserva/{id}")
    public Mono<ReservacionItem> editReservaByRestauranteIdAndReservaId(@PathVariable Integer restaurante_id, @PathVariable Integer id, @RequestBody ReservaRequestDto reservaRequestDto) {
        return reservaService.editReservaByRestauranteIdAndReservaId(restaurante_id, id, reservaRequestDto);
    }

    // desactivar una reserva
    @PatchMapping("/desactivar/{id}")
    public Mono<ReservaDto> desactivarReserva(@PathVariable Integer id) {
        return reservaService.desactivarReserva(id);
    }

    // restaurar una reserva
    @PatchMapping("/restaurar/{id}")
    public Mono<ReservaDto> restaurarReserva(@PathVariable Integer id) {
        return reservaService.restaurarReserva(id);
    }


}