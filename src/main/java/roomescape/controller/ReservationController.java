package roomescape.controller;


import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import roomescape.auth.AuthenticationPrincipal;
import roomescape.auth.LoginMember;
import roomescape.domain.Reservation;
import roomescape.dto.ReservationRequest;
import roomescape.dto.ReservationResponse;
import roomescape.dto.ReservationResponses;
import roomescape.dto.ReservationUpdateRequest;
import roomescape.service.ReservationService;

@Validated
@RestController
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping("/reservations")
    public ReservationResponses read(
            @RequestParam(required = false) String name,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size
    ) {
        return ReservationResponses.from(reservationService.find(name, page, size));
    }

    @PostMapping("/reservations")
    @ResponseStatus(HttpStatus.CREATED)
    public ReservationResponse create(
            @AuthenticationPrincipal LoginMember loginMember,
            @Valid @RequestBody ReservationRequest reservationRequest
    ) {
        Reservation reservation = reservationService.save(
                loginMember,
                reservationRequest.date(),
                reservationRequest.timeId(),
                reservationRequest.themeId()
        );
        return ReservationResponse.from(reservation);
    }

    @DeleteMapping("/admin/reservations/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @AuthenticationPrincipal LoginMember loginMember,
            @PathVariable Long id) {
        reservationService.deleteByManager(loginMember.id(), id);
    }

    @DeleteMapping("/reservations/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancel(
            @AuthenticationPrincipal LoginMember loginMember,
            @PathVariable Long id
    ) {
        reservationService.cancelById(loginMember.id(), id);
    }

    @PatchMapping("/reservations/{id}")
    public ReservationResponse update(
            @AuthenticationPrincipal LoginMember loginMember,
            @PathVariable long id,
            @Valid @RequestBody ReservationUpdateRequest request
    ) {
        Reservation reservation = reservationService.updateDateAndTime(
                loginMember.id(),
                id,
                request.date(),
                request.timeId()
        );
        return ReservationResponse.from(reservation);
    }

    @GetMapping("/reservations/mine")
    public ReservationResponses readMine(
            @AuthenticationPrincipal LoginMember loginMember,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size
    ) {
        return ReservationResponses.from(
                reservationService.findMine(loginMember.id(), page, size)
        );
    }

    @GetMapping("/admin/reservations")
    public ReservationResponses readByManager(
            @AuthenticationPrincipal LoginMember loginMember,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size
    ) {
        return ReservationResponses.from(
                reservationService.findByManager(loginMember.id(), page, size)
        );
    }

}
