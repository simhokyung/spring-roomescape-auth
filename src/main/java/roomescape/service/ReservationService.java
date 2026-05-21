package roomescape.service;

import org.springframework.stereotype.Service;
import roomescape.auth.LoginMember;
import roomescape.dao.ManagerDao;
import roomescape.dao.ReservationDao;
import roomescape.dao.ReservationTimeDao;
import roomescape.dao.ThemeDao;
import roomescape.domain.Manager;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.exception.DuplicateResourceException;
import roomescape.exception.ForbiddenException;
import roomescape.exception.InvalidInputException;
import roomescape.exception.NotFoundException;
import roomescape.exception.PastReservationException;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReservationService {

    private final ReservationDao reservationDao;
    private final ReservationTimeDao reservationTimeDao;
    private final ThemeDao themeDao;
    private final ManagerDao managerDao;
    private final Clock clock;

    public ReservationService(ReservationDao reservationDao, ReservationTimeDao reservationTimeDao, ThemeDao themeDao, ManagerDao managerDao, Clock clock) {
        this.reservationDao = reservationDao;
        this.reservationTimeDao = reservationTimeDao;
        this.themeDao = themeDao;
        this.managerDao = managerDao;
        this.clock = clock;
    }

    public List<Reservation> find(String name, int page, int size) {
        if (name == null) {
            return reservationDao.findAll(page, size);
        }
        if (name.isBlank()) {
            throw new InvalidInputException("예약자 이름은 필수입니다.");
        }
        return reservationDao.findByName(name, page, size);
    }

    public List<Reservation> findMine(Long memberId, int page, int size) {
        return reservationDao.findByMemberId(memberId, page, size);
    }

    public List<Reservation> findByManager(Long memberId, int page, int size) {
        Manager manager = findManagerByMemberId(memberId);

        return reservationDao.findByStoreId(manager.getStoreId(), page, size);
    }
    public void cancelById(long id) {
        Reservation reservation = findReservationById(id);
        validateCancelable(reservation);
        reservationDao.deleteById(id);
    }

    public void cancelById(Long memberId, long id) {
        Reservation reservation = findReservationById(id);
        validateOwner(reservation, memberId, "본인의 예약만 취소할 수 있습니다.");
        validateCancelable(reservation);
        reservationDao.deleteById(id);
    }

    public Reservation save(LoginMember loginMember, LocalDate date, Long timeId, Long themeId) {
        Member member = new Member(
                loginMember.id(),
                loginMember.name(),
                loginMember.email(),
                "password"
        );

        return save(member, date, timeId, themeId);
    }

    public Reservation save(String name, LocalDate date, Long timeId, Long themeId) {
        Member member = new Member(
                null,
                name,
                name + "@reservation.local",
                "password"
        );

        return save(member, date, timeId, themeId);
    }

    public Reservation save(Member member, LocalDate date, Long timeId, Long themeId) {
        ReservationTime time = reservationTimeDao.findById(timeId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 예약 시간입니다."));

        Theme theme = themeDao.findById(themeId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 테마입니다."));

        if (reservationDao.existByDateAndTimeAndThemeId(date, timeId, themeId)) {
            throw new DuplicateResourceException(
                    "DUPLICATE_RESERVATION",
                    "이미 존재하는 예약입니다."
            );
        }

        Reservation reservation = Reservation.create(
                member,
                date,
                time,
                theme,
                LocalDateTime.now(clock)
        );
        return reservationDao.save(reservation);
    }

    public Reservation updateDateAndTime(long id, LocalDate date, Long timeId) {
        Reservation reservation = findReservationById(id);
        return updateDateAndTime(reservation, date, timeId);
    }

    public Reservation updateDateAndTime(Long memberId, long id, LocalDate date, Long timeId) {
        Reservation reservation = findReservationById(id);
        validateOwner(reservation, memberId, "본인의 예약만 변경할 수 있습니다.");
        return updateDateAndTime(reservation, date, timeId);
    }

    public void deleteById(Long id) {
        reservationDao.deleteById(id);
    }

    public void deleteByManager(Long memberId, Long reservationId){
        Manager manager = findManagerByMemberId(memberId);
        Reservation reservation = findReservationById(reservationId);

        validateStoreManager(manager,reservation);

        reservationDao.deleteById(reservationId);
    }

    private Reservation updateDateAndTime(Reservation reservation, LocalDate date, Long timeId) {
        ReservationTime newTime = reservationTimeDao.findById(timeId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 예약 시간입니다."));

        LocalDateTime now = LocalDateTime.now(clock);
        Reservation updatedReservation = reservation.updateDateAndTime(date, newTime, now);

        if (reservationDao.existByDateAndTimeAndThemeId(
                updatedReservation.getDate(),
                updatedReservation.getTimeId(),
                updatedReservation.getThemeId()
        )) {
            throw new DuplicateResourceException(
                    "DUPLICATE_RESERVATION",
                    "이미 존재하는 예약입니다."
            );
        }

        reservationDao.updateDateAndTime(
                updatedReservation.getId(),
                updatedReservation.getDate(),
                updatedReservation.getTimeId()
        );

        return reservationDao.findById(reservation.getId())
                .orElseThrow(() -> new NotFoundException("존재하지 않는 예약입니다."));
    }

    private Reservation findReservationById(long id) {
        return reservationDao.findById(id)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 예약입니다."));
    }

    private void validateOwner(Reservation reservation, Long memberId, String message) {
        if (!reservation.getMemberId().equals(memberId)) {
            throw new ForbiddenException(message);
        }
    }

    private void validateCancelable(Reservation reservation) {
        LocalDateTime now = LocalDateTime.now(clock);

        if (reservation.isPast(now)) {
            throw new PastReservationException("지난 예약은 취소할 수 없습니다.");
        }
    }

    private Manager findManagerByMemberId(Long memberId) {
        return managerDao.findByMemberId(memberId)
                .orElseThrow(() -> new ForbiddenException("매장 매니저만 예약을 관리할 수 있습니다."));
    }

    private void validateStoreManager(Manager manager, Reservation reservation) {
        if (!reservation.getStoreId().equals(manager.getStoreId())) {
            throw new ForbiddenException("자기 매장의 예약만 관리할 수 있습니다.");
        }
    }
}
