package com.upgrade.codechallenge.service;

import com.google.gson.Gson;
import com.upgrade.codechallenge.model.Reservation;
import com.upgrade.codechallenge.repository.ReservationRepository;
import com.upgrade.codechallenge.util.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

import static java.time.temporal.ChronoUnit.DAYS;

@Service
public class ReservationServiceImpl implements ReservationService {

    @Autowired
    private ReservationRepository reservationRepository;

    @Override
    public Response getReservations(LocalDate arrivalDate, LocalDate departureDate) {
        Gson gson = new Gson();
        if(arrivalDate == null) {
            return new Response("You must specify an arrival date.", null, HttpStatus.BAD_REQUEST);
        }
        if(departureDate == null) {
            departureDate = arrivalDate.plusDays(30);
        } else if(arrivalDate.isAfter(departureDate)) {
            return new Response("Arrival date is greather than departure date.", null, HttpStatus.BAD_REQUEST);
        }
        return new Response(null,
                gson.toJson(this.reservationRepository.findByArrivalDateGreaterThanEqualAndDepartureDateLessThanEqual(arrivalDate,departureDate)),
                HttpStatus.OK);
    }

    @Override
    public Response saveReservation(Reservation reservation) {
        Gson gson = new Gson();
        if(reservation.getArrivalDate() == null) {
            return new Response("You must specify an arrival date.", null, HttpStatus.BAD_REQUEST);
        }
        if(reservation.getDepartureDate() == null) {
            return new Response("You must specify an departure date.", null, HttpStatus.BAD_REQUEST);
        }

        // verificar que la fecha de hoy sea menor a un mes hasta el dia de la reserva (se puede reservar hasta un mes antes)
        // ** TODO: method for validate dates
        LocalDate now = LocalDate.now();
        long daysBetween = DAYS.between(now, reservation.getArrivalDate());
        // if daysBetween < 1
        if(daysBetween < 1 || daysBetween > 30) {
            return new Response("The campsite can be reserved minimum 1 day(s) ahead of arrival and up to 1 month in advance.", null, HttpStatus.BAD_REQUEST);
        }
        // calcular la diferencia de 3 dias (maximo de 3 dias)
        daysBetween = DAYS.between(reservation.getArrivalDate(), reservation.getDepartureDate());
        if(daysBetween > 3) {
            return new Response("The campsite can be reserved for max 3 days.", null, HttpStatus.BAD_REQUEST);
        }
        // END TODO ** //

        this.reservationRepository.save(reservation);
        return new Response(null, gson.toJson(reservation.getId().toString()), HttpStatus.OK);
    }

    @Override
    public void updateReservation(Reservation reservation) {
        // si existe
        this.reservationRepository.save(reservation);
    }

    @Override
    public Response deleteReservation(Long id) {
        // si existe
        Reservation reservation = this.reservationRepository.findReservationById(id);
        if(reservation == null) {
            return new Response("Reservation does not exist", null, HttpStatus.NOT_FOUND);
        }
        this.reservationRepository.delete(reservation);
        return new Response(null, null, HttpStatus.OK);
    }

    @Override
    public void getAvailability(LocalDate arrivalDate, LocalDate departureDate) {

    }


}
