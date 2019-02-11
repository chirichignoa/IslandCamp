package com.upgrade.codechallenge.model;

import com.fasterxml.jackson.annotation.JsonFormat;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.time.LocalDate;
import java.util.Objects;

@Entity
public class Reservation {

    @Id
    @GeneratedValue
    private Long id;
    private LocalDate arrivalDate;
    private LocalDate departureDate;
    private String name;
    private String email;

    protected Reservation() {}

    public Reservation(LocalDate arrivalDate, LocalDate departureDate, String name, String email) {
        this.arrivalDate = arrivalDate;
        this.departureDate = departureDate;
        this.name = name;
        this.email = email;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
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
        return id.equals(that.id) &&
                arrivalDate.equals(that.arrivalDate) &&
                departureDate.equals(that.departureDate) &&
                name.equals(that.name) &&
                email.equals(that.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, arrivalDate, departureDate, name, email);
    }
}
