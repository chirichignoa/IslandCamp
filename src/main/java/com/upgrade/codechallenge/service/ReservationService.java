package com.upgrade.codechallenge.service;

import com.upgrade.codechallenge.model.Reservation;
import com.upgrade.codechallenge.util.Response;

import java.time.LocalDate;
import java.util.List;

public interface ReservationService {

    Response getReservations(LocalDate arrivalDate, LocalDate departureDate);

    Response saveReservation(Reservation reservation);

    void updateReservation(Reservation reservation);

    Response deleteReservation(Long id);

    void getAvailability(LocalDate arrivalDate, LocalDate departureDate);
}
