package com.upgrade.codechallenge.service;

import com.upgrade.codechallenge.exception.InternalServerErrorException;
import com.upgrade.codechallenge.exception.OcuppedDateRangeException;
import com.upgrade.codechallenge.model.Reservation;
import com.upgrade.codechallenge.repository.ReservationRepository;
import com.upgrade.codechallenge.util.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpServerErrorException;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static java.time.temporal.ChronoUnit.DAYS;

@Service
public class ReservationServiceImpl implements ReservationService {

    @Autowired
    private ReservationRepository reservationRepository;

    public ReservationServiceImpl(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    @Override
    @Transactional
    public Response saveReservation(Reservation body) {
        if(body.getArrivalDate() == null) {
            return new Response("You must specify an arrival date.", null, HttpStatus.BAD_REQUEST);
        }
        if(body.getDepartureDate() == null) {
            return new Response("You must specify a departure date.", null, HttpStatus.BAD_REQUEST);
        }

        Response x = validateDates(body);
        if (x != null) return x;
        try {
            String resourceId = UUID.randomUUID().toString();
            long daysBetween = DAYS.between(body.getArrivalDate(), body.getDepartureDate());
            for (int i = 0; i < daysBetween; i++) {
                Reservation reservation = new Reservation(body.getArrivalDate().plusDays(i),
                        body.getArrivalDate().plusDays(i + 1),
                        body.getName(),
                        body.getEmail(),
                        resourceId);
                this.reservationRepository.save(reservation);
            }
            return new Response(null, resourceId, HttpStatus.OK);
        } catch (Exception e) {
            throw new OcuppedDateRangeException("The camp is already reserved for that date range.");
        }
    }

    @Override
    public Response getReservations(LocalDate arrivalDate, LocalDate departureDate) {
        if(arrivalDate == null) {
            return new Response("You must specify an arrival date.", null, HttpStatus.BAD_REQUEST);
        }
        if(departureDate == null) {
            departureDate = arrivalDate.plusDays(30);
        } else if(arrivalDate.isAfter(departureDate)) {
            return new Response("Arrival date is greater than departure date.", null, HttpStatus.BAD_REQUEST);
        }
        return new Response(null,
                this.reservationRepository.findByArrivalDateGreaterThanEqualAndDepartureDateLessThanEqual(arrivalDate,departureDate),
                HttpStatus.OK);
    }

    @Override
    public Response getReservation(String id) {
        List<Reservation> reservations = this.reservationRepository.findReservationByResourceId(id);
        if(reservations == null || reservations.size() == 0) {
            return new Response("Reservation does not exist", null, HttpStatus.NOT_FOUND);
        }
        return new Response(null,
                reservations,
                HttpStatus.OK);
    }

    @Override
    @Transactional
    public Response updateReservation(String id, Reservation reservation) {
        List<Reservation> reservations = this.reservationRepository.findReservationByResourceId(id);
        Response response = checkIfReservationExists(reservations);
        if(response != null)
            return response;
        response= validateDates(reservation);
        if (response != null) return response;
        int daysBetween = (int) DAYS.between(reservation.getArrivalDate(), reservation.getDepartureDate());
        if(daysBetween != reservations.size()) {
            return new Response("The new reservation's duration must be the same than the previous", null, HttpStatus.BAD_REQUEST);
        }
        // This lines are for move the reservation to a previous date than the arrival date
        int difference;
        boolean before = reservations.get(0).getDepartureDate().isAfter(reservation.getArrivalDate());
        if(before) {
            difference = (int) DAYS.between(reservation.getArrivalDate(), reservations.get(0).getArrivalDate());
            Collections.reverse(reservations);
        } else {
            difference = (int) DAYS.between(reservations.get(reservations.size() -1).getDepartureDate(), reservation.getDepartureDate());
        }
        // Reverse order for avoid the unique constraint violation in the BD
        // I assume that the modification maintains the same amount of days
        try {
            for (int i = daysBetween - 1; i >= 0; i--) {
                Reservation r = reservations.get(i);
                if(before) {
                    this.reservationRepository.updateReservationDates(r.getArrivalDate().minusDays(difference),
                            r.getDepartureDate().minusDays(difference),
                            r.getId());
                } else {
                    this.reservationRepository.updateReservationDates(r.getArrivalDate().plusDays(difference),
                            r.getDepartureDate().plusDays(difference),
                            r.getId());
                }
            }
            return new Response(null, id, HttpStatus.OK);
        } catch (Exception e) {
            throw new OcuppedDateRangeException("The camp is already reserved for that date range.");
        }
    }

    @Override
    @Transactional
    public Response modifyReservation(String id, Reservation reservation) {
        List<Reservation> reservations = this.reservationRepository.findReservationByResourceId(id);
        Response response = checkIfReservationExists(reservations);
        if(response != null)
            return response;
        response = validateDates(reservation);
        if (response != null) return response;
        int newDaysBetween = (int) DAYS.between(reservation.getArrivalDate(), reservation.getDepartureDate());
        if(newDaysBetween != reservations.size()) {
            return new Response("The new reservation's duration must be the same than the previous", null, HttpStatus.BAD_REQUEST);
        }
        // This lines are for move the reservation to a previous date than the arrival date
        int difference;
        boolean before = reservations.get(0).getDepartureDate().isAfter(reservation.getArrivalDate());
        if(before) {
            difference = (int) DAYS.between(reservation.getArrivalDate(), reservations.get(0).getArrivalDate());
            Collections.reverse(reservations);
        } else {
            difference = (int) DAYS.between(reservations.get(reservations.size() -1).getDepartureDate(), reservation.getDepartureDate());
        }
        // Reverse order for avoid the unique constraint violation in the BD
        // I assume that the modification maintains the same amount of days
        try {
            for (int i = newDaysBetween - 1; i >= 0; i--) {
                Reservation r = reservations.get(i);
                if(before) {
                    this.reservationRepository.modifyReservation(reservation.getName(),
                            reservation.getEmail(),
                            r.getArrivalDate().minusDays(difference),
                            r.getDepartureDate().minusDays(difference),
                            r.getId());
                } else {
                    this.reservationRepository.modifyReservation(reservation.getName(),
                            reservation.getEmail(),
                            r.getArrivalDate().plusDays(difference),
                            r.getDepartureDate().plusDays(difference),
                            r.getId());
                }
            }
            return new Response(null, id, HttpStatus.OK);
        } catch (Exception e ) {
            throw new OcuppedDateRangeException("The camp is already reserved for that date range.");
        }
    }

    @Override
    @Transactional
    public Response deleteReservation(String id) {
        List<Reservation> reservations = this.reservationRepository.findReservationByResourceId(id);
        Response r = checkIfReservationExists(reservations);
        if(r != null)
            return r;
        try {
            for (Reservation reservation : reservations) {
                this.reservationRepository.delete(reservation);
            }
            return new Response(null, null, HttpStatus.OK);
        } catch (Exception e) {
            throw new InternalServerErrorException("There is an internal problem in the server.");
        }
    }

    private Response validateDates(Reservation reservation) {
        if(reservation.getArrivalDate().isAfter(reservation.getDepartureDate())) {
            return new Response("The arrival date is greater than departure date.", null, HttpStatus.BAD_REQUEST);
        }
        LocalDate now = LocalDate.now();
        long daysBetween = DAYS.between(now, reservation.getArrivalDate());
        if(daysBetween < 1 || daysBetween > 30) {
            return new Response("The campsite can be reserved minimum 1 day(s) ahead of arrival and up to 1 month in advance.", null, HttpStatus.BAD_REQUEST);
        }
        daysBetween = DAYS.between(reservation.getArrivalDate(), reservation.getDepartureDate());
        if(daysBetween > 3) {
            return new Response("The campsite can be reserved for max 3 days.", null, HttpStatus.BAD_REQUEST);
        }
        return null;
    }

    private Response checkIfReservationExists(List<Reservation> reservations) {
        if(reservations == null || reservations.size() == 0) {
            return new Response("Reservation does not exist", null, HttpStatus.NOT_FOUND);
        }
        return null;
    }
}
