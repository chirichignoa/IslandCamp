package com.upgrade.codechallenge.repository;

import com.upgrade.codechallenge.model.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Transactional
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findByArrivalDateGreaterThanEqualAndDepartureDateLessThanEqual(LocalDate arrivalDate, LocalDate departureDate);

    Reservation findReservationById(Long id);
}