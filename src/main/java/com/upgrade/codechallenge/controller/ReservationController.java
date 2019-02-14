package com.upgrade.codechallenge.controller;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.upgrade.codechallenge.exception.OcuppedDateRangeException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.upgrade.codechallenge.model.Reservation;
import com.upgrade.codechallenge.service.ReservationService;
import com.upgrade.codechallenge.util.Response;

import java.time.LocalDate;

@RestController
public class ReservationController {

    private Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .excludeFieldsWithoutExposeAnnotation()
            .serializeNulls()
            .create();

    @Autowired
    private ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    // POST
    @RequestMapping(value = "/reservation", method = RequestMethod.POST,
            produces = "application/json; charset=utf-8",
            consumes = "application/json; charset=utf-8")
    public @ResponseBody
    ResponseEntity<String> reservation(@RequestBody Reservation reservation) {
        Response response;
        try {
            response = this.reservationService.saveReservation(reservation);
        } catch(OcuppedDateRangeException e) {
            response = new Response(e.getMessage(), null, HttpStatus.BAD_REQUEST);
        }
        return ResponseEntity.status(response.getCode())
                .contentType(MediaType.APPLICATION_JSON)
                .body(this.gson.toJson(response));
    }

    // GET
    @RequestMapping(value = "/reservation", method = RequestMethod.GET,
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

    // GET
    @RequestMapping(value = "/reservation/{id}", method = RequestMethod.GET,
            produces = "application/json; charset=utf-8")
    public @ResponseBody
    ResponseEntity<String> getReservation(@PathVariable(value="id", required=false) String resourceId) {
        ResponseEntity<String> idError = this.validateId(resourceId);
        if(idError != null) return idError;
        Response response = this.reservationService.getReservation(resourceId);
        return ResponseEntity.status(response.getCode())
                .contentType(MediaType.APPLICATION_JSON)
                .body(this.gson.toJson(response));
    }

    // PATCH
    @RequestMapping(value = "/reservation/{id}", method = RequestMethod.PATCH,
            produces = "application/json; charset=utf-8",
            consumes = "application/json; charset=utf-8")
    public @ResponseBody
    ResponseEntity<String> updateReservation(@PathVariable String id, @RequestBody Reservation reservation) {
        ResponseEntity<String> idError = this.validateId(id);
        if(idError != null) return idError;
        Response response;
        try {
            response = this.reservationService.updateReservation(id, reservation);
        } catch(OcuppedDateRangeException e) {
            response = new Response(e.getMessage(), null, HttpStatus.BAD_REQUEST);
        }
        return ResponseEntity.status(response.getCode())
                .contentType(MediaType.APPLICATION_JSON)
                .body(gson.toJson(response));
    }

    // DELETE /reservation - params(reservationId)
    @RequestMapping(value = "/reservation/{id}", method = RequestMethod.DELETE,
            produces = "application/json; charset=utf-8")
    public @ResponseBody
    ResponseEntity<String> deleteReservation(@PathVariable String id) {
        ResponseEntity<String> idError = this.validateId(id);
        if(idError != null) return idError;
        Response response;
        try {
            response = this.reservationService.deleteReservation(id);
        } catch (Exception e){
            response = new Response(e.getMessage(), null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return ResponseEntity.status(response.getCode())
                .contentType(MediaType.APPLICATION_JSON)
                .body("");
    }

    private ResponseEntity<String> validateId(String id) {
        if (id == null) {
            Response response = new Response("You must give a valid reservation id", null, HttpStatus.BAD_REQUEST);
            return ResponseEntity.status(response.getCode())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(this.gson.toJson(response));
        }
        return null;
    }
}
