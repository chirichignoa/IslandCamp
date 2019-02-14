package com.upgrade.codechallenge.repository;

import com.upgrade.codechallenge.model.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findByArrivalDateGreaterThanEqualAndDepartureDateLessThanEqual(LocalDate arrivalDate, LocalDate departureDate);

    List<Reservation> findReservationByResourceId(String resourceId);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query(value = "UPDATE Reservation r SET r.arrivalDate = :arrival_date,  r.departureDate = :departure_date WHERE r.id = :id")
    void updateReservationDates(@Param("arrival_date") LocalDate arrivalDate,
                           @Param("departure_date") LocalDate departureDate,
                           @Param("id") Long id);
}