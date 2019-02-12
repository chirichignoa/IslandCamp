package com.upgrade.codechallenge.service;

import com.upgrade.codechallenge.model.Reservation;
import com.upgrade.codechallenge.util.Response;

import java.time.LocalDate;

public interface ReservationService {

    Response getReservations(LocalDate arrivalDate, LocalDate departureDate);

    Response getReservation(String id);

    Response saveReservation(Reservation reservation);

    Response updateReservation(String id, Reservation reservation);

    Response deleteReservation(String id);

    Response modifyReservation(String id, Reservation reservation);

}
