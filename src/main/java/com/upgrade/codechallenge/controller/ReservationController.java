package com.upgrade.codechallenge.controller;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.upgrade.codechallenge.model.Reservation;
import com.upgrade.codechallenge.service.ReservationService;
import com.upgrade.codechallenge.util.Response;
import com.upgrade.codechallenge.util.ResponseJsonSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;

@RestController
public class ReservationController {

    private Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .excludeFieldsWithoutExposeAnnotation()
            .serializeNulls()
            .registerTypeAdapter(Response.class, new ResponseJsonSerializer())
            .create();

    @Autowired
    private ReservationService reservationService;

    // GET /availability params(date_from, date_until => optional)
    @RequestMapping(value = "/reservations", method = RequestMethod.GET,
            produces = "application/json; charset=utf-8")
    public @ResponseBody
    ResponseEntity<String> getReservations(@RequestParam(value="arrival", required=false) String arrival,
                                           @RequestParam(value="departure", required=false) String departure) {

        LocalDate arrivalDate = (arrival != null)? LocalDate.parse(arrival) : null;
        LocalDate departureDate = (departure!=null)? LocalDate.parse(departure) : null;
        Response response = this.reservationService.getReservations(arrivalDate, departureDate);

        return ResponseEntity.status(response.getCode())
                .contentType(MediaType.APPLICATION_JSON)
                .body(this.gson.toJson(response));
    }

    // POST /reservation - params(name, email, arrivalDate, departureDate) - return reservationId
    @RequestMapping(value = "/reservation", method = RequestMethod.POST,
            produces = "application/json; charset=utf-8",
            consumes = "application/json; charset=utf-8")
    public @ResponseBody
    ResponseEntity<String> reservation(@RequestBody Reservation reservation) {
        Response response = this.reservationService.saveReservation(reservation);
        return ResponseEntity.status(response.getCode())
                .contentType(MediaType.APPLICATION_JSON)
                .body(this.gson.toJson(response));
    }

    // PATCH /reservation - params(reservationId, arrivalDate, departureDate)
    @RequestMapping(value = "/reservation/{id}", method = RequestMethod.PATCH)
    public @ResponseBody
    String modifyReservation(@PathVariable String id, @RequestBody Reservation reservation) {
        return null;
    }

    // DELETE /reservation - params(reservationId)
    @RequestMapping(value = "/reservation/{id}", method = RequestMethod.DELETE)
    public @ResponseBody
    ResponseEntity<String> deleteReservation(@PathVariable String id) {
        Long reservationId;
        try {
            reservationId = Long.parseLong(id);
        } catch (NumberFormatException e) {
            reservationId = null;
        }
        if (reservationId == null) {
            Response response = new Response("You must give a valid reservation id", null, HttpStatus.BAD_REQUEST);
            return ResponseEntity.status(response.getCode())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(this.gson.toJson(response));
        }
        Response response = this.reservationService.deleteReservation(reservationId);
        return ResponseEntity.status(response.getCode())
                .contentType(MediaType.APPLICATION_JSON)
                .body("");
    }

    @RequestMapping(value = "/availability", method = RequestMethod.GET)
    public @ResponseBody
    String getAvailability(@RequestParam(value="arrival", required=false) String arrival,
                           @RequestParam(value="departure", required=false) String departure) {
        LocalDate arrivalDate = (arrival != null)? LocalDate.parse(arrival) : null;
        LocalDate departureDate = (departure!=null)? LocalDate.parse(departure) : null;

        return null;
    }
}
