package com.upgrade.codechallenge.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.google.gson.annotations.JsonAdapter;
import com.upgrade.codechallenge.util.ReservationGsonAdapter;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.Objects;

@Entity
@JsonAdapter(ReservationGsonAdapter.class)
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name="arrival_date", unique=true)
    private LocalDate arrivalDate;
    @Column(name="departure_date")
    private LocalDate departureDate;
    private String name;
    private String email;
    @Column(name="resource_id")
    private String resourceId;

    public Reservation() {}

    public Reservation(LocalDate arrivalDate, LocalDate departureDate, String name, String email, String resourceId) {
        this.arrivalDate = arrivalDate;
        this.departureDate = departureDate;
        this.name = name;
        this.email = email;
        this.resourceId = resourceId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @JsonFormat(pattern = "yyyy-MM-dd")
    public LocalDate getArrivalDate() {
        return arrivalDate;
    }

    @JsonFormat(pattern = "yyyy-MM-dd")
    public void setArrivalDate(LocalDate arrivalDate) {
        this.arrivalDate = arrivalDate;
    }

    @JsonFormat(pattern = "yyyy-MM-dd")
    public LocalDate getDepartureDate() {
        return departureDate;
    }

    @JsonFormat(pattern = "yyyy-MM-dd")
    public void setDepartureDate(LocalDate departureDate) {
        this.departureDate = departureDate;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    @Override
    public String toString() {
        StringBuilder ret = new StringBuilder();
        ret.append("Reservation - reservationId = ").append(this.id)
            .append(" - arrivalDate = ").append(this.arrivalDate.toString())
            .append(" - departureDate = ").append(this.departureDate.toString())
            .append(" - name = ").append(this.name)
            .append(" - email = ").append(this.email);
        return ret.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Reservation that = (Reservation) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, arrivalDate, departureDate, name, email, resourceId);
    }
}
