package com.upgrade.codechallenge.util;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import com.upgrade.codechallenge.model.Reservation;

import java.io.IOException;
import java.time.LocalDate;

public class ReservationGsonAdapter extends TypeAdapter<Reservation> {
    @Override
    public void write(JsonWriter jsonWriter, Reservation reservation) throws IOException {
        jsonWriter.beginObject();
        jsonWriter.name("id").value(reservation.getResourceId());
        jsonWriter.name("arrival_date").value(reservation.getArrivalDate().toString());
        jsonWriter.name("departure_date").value(reservation.getDepartureDate().toString());
        jsonWriter.name("name").value(reservation.getName());
        jsonWriter.name("email").value(reservation.getEmail());
        jsonWriter.endObject();
    }

    @Override
    public Reservation read(JsonReader jsonReader) throws IOException {
        final Reservation reservation = new Reservation();
        jsonReader.beginObject();
        while (jsonReader.hasNext()) {
            switch (jsonReader.nextName()) {
                case "id":
                    reservation.setResourceId(jsonReader.nextString());
                    break;
                case "arrival_date":
                    reservation.setArrivalDate(LocalDate.parse(jsonReader.nextString()));
                    break;
                case "departure_date":
                    reservation.setDepartureDate(LocalDate.parse(jsonReader.nextString()));
                    break;
                case "name":
                    reservation.setName(jsonReader.nextString());
                    break;
                case "email":
                    reservation.setEmail(jsonReader.nextString());
                    break;
            }
        }
        jsonReader.endObject();
        return reservation;
    }
}
