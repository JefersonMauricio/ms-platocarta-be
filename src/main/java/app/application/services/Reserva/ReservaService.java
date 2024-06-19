package app.application.services.Reserva;

import app.domain.ReservacionItem;
import app.infrastructure.email.EmailService;
import app.infrastructure.item.PlatoCarta.repository.PlatoCartaRepository;
import app.infrastructure.item.Reserva.dto.ReservaDto;
import app.infrastructure.item.Reserva.dto.ReservaRequestDto;
import app.infrastructure.item.Reserva.repository.ReservaRepository;
import app.infrastructure.item.ReservaDetalle.dto.ReservaDetalleDto;
import app.infrastructure.item.ReservaDetalle.repository.ReservaDetalleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReservaService {

    @Autowired
    private ReservaRepository reservaRepository;

    @Autowired
    private ReservaDetalleRepository reservaDetalleRepository;

    @Autowired
    private PlatoCartaRepository platoCartaRepository;

    @Autowired
    private EmailService  emailService;

    public Flux<ReservacionItem> getAllReservacionItems() {
        Flux<ReservacionItem> reservacionItems = reservaRepository.findAll()
                .map(this::toReservacionItem);

        Flux<ReservacionItem.ReservaDetalleItem> reservaDetalleItems = reservaDetalleRepository.findAll()
                .flatMap(this::toReservaDetalleItem);

        Mono<Map<Integer, List<ReservacionItem.ReservaDetalleItem>>> groupedReservaDetalleItems = reservaDetalleItems
                .collectMultimap(ReservacionItem.ReservaDetalleItem::getId_reserva)
                .map(map -> map.entrySet().stream()
                        .collect(Collectors.toMap(Map.Entry::getKey, e -> new ArrayList<>(e.getValue()))));

        return groupedReservaDetalleItems.flatMapMany(grouped -> reservacionItems.flatMap(reservacionItem -> {
            List<ReservacionItem.ReservaDetalleItem> detalles = grouped.get(reservacionItem.getId());
            if (detalles != null) {
                reservacionItem.setReserva_detalle(detalles);
            }
            return Mono.just(reservacionItem);
        }));
    }

    public Mono<ReservacionItem> getReservacionItemById(Integer id) {
        Mono<ReservaDto> reservaDtoMono = reservaRepository.findById(id);
        Flux<ReservaDetalleDto> reservaDetalleDtoFlux = reservaDetalleRepository.findAllByIdReserva(id);

        return reservaDtoMono.flatMap(reservaDto -> {
            ReservacionItem reservacionItem = toReservacionItem(reservaDto);
            return reservaDetalleDtoFlux.flatMap(this::toReservaDetalleItem).collectList()
                    .doOnNext(reservacionItem::setReserva_detalle)
                    .thenReturn(reservacionItem);
        });
    }

    public Flux<ReservacionItem> getReservacionItemsByEstado(String estado) {
        Flux<ReservaDto> reservaDtoFlux = reservaRepository.findAllByEstado(estado);
        return reservaDtoFlux.flatMap(reservaDto -> {
            ReservacionItem reservacionItem = toReservacionItem(reservaDto);
            return reservaDetalleRepository.findAllByIdReserva(reservaDto.getId())
                    .flatMap(this::toReservaDetalleItem).collectList()
                    .doOnNext(reservacionItem::setReserva_detalle)
                    .thenReturn(reservacionItem);
        });
    }

    public Flux<ReservacionItem> getReservacionItemsByRestauranteIdAndEstado(Integer restauranteId, String estado) {
        Flux<ReservaDto> reservaDtoFlux = reservaRepository.findAllByRestauranteIdAndEstado(restauranteId, estado);
        return reservaDtoFlux.flatMap(reservaDto -> {
            ReservacionItem reservacionItem = toReservacionItem(reservaDto);
            return reservaDetalleRepository.findAllByIdReserva(reservaDto.getId())
                    .flatMap(this::toReservaDetalleItem).collectList()
                    .doOnNext(reservacionItem::setReserva_detalle)
                    .thenReturn(reservacionItem);
        });
    }

    public Flux<ReservacionItem> getReservacionItemsByClienteIdAndEstado(Integer clienteId, String estado) {
        Flux<ReservaDto> reservaDtoFlux = reservaRepository.findAllByClienteIdAndEstado(clienteId, estado);
        return reservaDtoFlux.flatMap(reservaDto -> {
            ReservacionItem reservacionItem = toReservacionItem(reservaDto);
            return reservaDetalleRepository.findAllByIdReserva(reservaDto.getId())
                    .flatMap(this::toReservaDetalleItem).collectList()
                    .doOnNext(reservacionItem::setReserva_detalle)
                    .thenReturn(reservacionItem);
        });
    }

    public Mono<ReservacionItem> getReservacionItemByRestauranteIdAndReservaId(Integer restauranteId, Integer id) {
        return reservaRepository.findById(id)
                .filter(reservaDto -> reservaDto.getRestauranteId().equals(restauranteId))
                .map(this::toReservacionItem);
    }

    public Mono<ReservacionItem> getReservacionItemByClienteIdAndReservaId(Integer clienteId, Integer id) {
        return reservaRepository.findById(id)
                .filter(reservaDto -> reservaDto.getClienteId().equals(clienteId))
                .map(this::toReservacionItem);
    }

    public Mono<ReservacionItem> createReserva(ReservaRequestDto reservaRequestDto) {
        ReservaDto reservaDto = new ReservaDto();
        reservaDto.setClienteId(reservaRequestDto.getCliente_id());
        reservaDto.setRestauranteId(reservaRequestDto.getRestaurante_id());
        reservaDto.setEmail(reservaRequestDto.getEmail());
        reservaDto.setFecha_destino(reservaRequestDto.getFecha_destino());
        reservaDto.setPersonas(reservaRequestDto.getPersonas());
        reservaDto.setMonto(reservaRequestDto.getMonto());
        reservaDto.setObservacion(reservaRequestDto.getObservacion());
        reservaDto.setSituacion(reservaRequestDto.getSituacion());
        reservaDto.setEstado("A");

        return reservaRepository.save(reservaDto)
                .flatMap(savedReservaDto -> {
                    List<ReservaDetalleDto> reservaDetalleDtos = reservaRequestDto.getReserva_detalle();
                    reservaDetalleDtos.forEach(reservaDetalleDto -> reservaDetalleDto.setIdReserva(savedReservaDto.getId()));
                    return reservaDetalleRepository.saveAll(reservaDetalleDtos)
                            .then(Mono.just(savedReservaDto));
                })
                .flatMap(savedReservaDto -> {
                    emailService.sendSimpleMessage(reservaRequestDto.getEmail(), reservaRequestDto.getEmail(), "Reserva creada", "Tu reserva ha sido creada con éxito.");
                    return Mono.just(toReservacionItem(savedReservaDto));
                });
    }

    public Mono<Void> updateReservaDetalle(List<ReservaDetalleDto> reservaDetalleDtos) {
        return Flux.fromIterable(reservaDetalleDtos)
                .flatMap(reservaDetalleDto -> reservaDetalleRepository.findById(reservaDetalleDto.getId())
                        .flatMap(existingReservaDetalleDto -> {
                            existingReservaDetalleDto.setId_carta(reservaDetalleDto.getId_carta());
                            existingReservaDetalleDto.setSubtotal(reservaDetalleDto.getSubtotal());
                            existingReservaDetalleDto.setCantidad(reservaDetalleDto.getCantidad());
                            return reservaDetalleRepository.save(existingReservaDetalleDto);
                        }))
                .then();
    }


    public Mono<ReservacionItem> editReservaById(Integer id, ReservaRequestDto reservaRequestDto) {
        return reservaRepository.findById(id)
                .flatMap(existingReservaDto -> {
                    existingReservaDto.setClienteId(reservaRequestDto.getCliente_id());
                    existingReservaDto.setRestauranteId(reservaRequestDto.getRestaurante_id());
                    existingReservaDto.setEmail(reservaRequestDto.getEmail());
                    existingReservaDto.setFecha_destino(reservaRequestDto.getFecha_destino());
                    existingReservaDto.setPersonas(reservaRequestDto.getPersonas());
                    existingReservaDto.setMonto(reservaRequestDto.getMonto());
                    existingReservaDto.setObservacion(reservaRequestDto.getObservacion());
                    existingReservaDto.setSituacion(reservaRequestDto.getSituacion());
                    return reservaRepository.save(existingReservaDto)
                            .then(updateReservaDetalle(reservaRequestDto.getReserva_detalle()))
                            .then(Mono.just(toReservacionItem(existingReservaDto)));
                });
    }

    public Mono<ReservacionItem> editReservaByRestauranteIdAndReservaId(Integer restauranteId, Integer id, ReservaRequestDto reservaRequestDto) {
        return reservaRepository.findById(id)
                .filter(reservaDto -> reservaDto.getRestauranteId().equals(restauranteId))
                .flatMap(existingReservaDto -> {
                    existingReservaDto.setClienteId(reservaRequestDto.getCliente_id());
                    existingReservaDto.setEmail(reservaRequestDto.getEmail());
                    existingReservaDto.setFecha_destino(reservaRequestDto.getFecha_destino());
                    existingReservaDto.setPersonas(reservaRequestDto.getPersonas());
                    existingReservaDto.setMonto(reservaRequestDto.getMonto());
                    existingReservaDto.setObservacion(reservaRequestDto.getObservacion());
                    existingReservaDto.setSituacion(reservaRequestDto.getSituacion());
                    return reservaRepository.save(existingReservaDto)
                            .then(updateReservaDetalle(reservaRequestDto.getReserva_detalle()))
                            .then(Mono.just(toReservacionItem(existingReservaDto)));
                });
    }


    private ReservacionItem toReservacionItem(ReservaDto reservaDto) {
        ReservacionItem reservacionItem = new ReservacionItem();
        reservacionItem.setId(reservaDto.getId());
        reservacionItem.setCliente_id(reservaDto.getClienteId());
        reservacionItem.setRestaurante_id(reservaDto.getRestauranteId());
        reservacionItem.setEmail(reservaDto.getEmail());
        reservacionItem.setFecha_registro(reservaDto.getFecha_registro());
        reservacionItem.setFecha_destino(reservaDto.getFecha_destino());
        reservacionItem.setPersonas(reservaDto.getPersonas());
        reservacionItem.setMonto(reservaDto.getMonto());
        reservacionItem.setObservacion(reservaDto.getObservacion());
        reservacionItem.setSituacion(reservaDto.getSituacion());
        reservacionItem.setEstado(reservaDto.getEstado());
        return reservacionItem;
    }

    private Mono<ReservacionItem.ReservaDetalleItem> toReservaDetalleItem(ReservaDetalleDto reservaDetalleDto) {
        ReservacionItem.ReservaDetalleItem reservaDetalleItem = new ReservacionItem.ReservaDetalleItem();
        reservaDetalleItem.setId(reservaDetalleDto.getId());
        reservaDetalleItem.setId_reserva(reservaDetalleDto.getIdReserva());
        reservaDetalleItem.setId_carta(reservaDetalleDto.getId_carta());
        reservaDetalleItem.setSubtotal(reservaDetalleDto.getSubtotal());
        reservaDetalleItem.setCantidad(reservaDetalleDto.getCantidad());
        reservaDetalleItem.setEstado(reservaDetalleDto.getEstado());

        return platoCartaRepository.findById(reservaDetalleDto.getId_carta())
                .map(platoCartaDto -> {
                    ReservacionItem.ReservaDetalleItem.PlatoCartaItem platoCartaItem = new ReservacionItem.ReservaDetalleItem.PlatoCartaItem();
                    platoCartaItem.setId(platoCartaDto.getId());
                    platoCartaItem.setNombre(platoCartaDto.getNombre());
                    platoCartaItem.setPrecio(platoCartaDto.getPrecio());
                    platoCartaItem.setStock(platoCartaDto.getStock());
                    platoCartaItem.setEstado(platoCartaDto.getEstado());
                    reservaDetalleItem.setPlato_carta(platoCartaItem);
                    return reservaDetalleItem;
                });
    }

    public Mono<ReservaDto> desactivarReserva(Integer id) {
        return reservaRepository.findById(id)
                .flatMap(reservaDto -> {
                    reservaDto.setEstado("I");
                    return reservaRepository.save(reservaDto);
                });
    }

    public Mono<ReservaDto> restaurarReserva(Integer id) {
        return reservaRepository.findById(id)
                .flatMap(reservaDto -> {
                    reservaDto.setEstado("A");
                    return reservaRepository.save(reservaDto);
                });
    }

}
