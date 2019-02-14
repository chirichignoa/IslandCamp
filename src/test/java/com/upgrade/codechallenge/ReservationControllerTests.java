package com.upgrade.codechallenge;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.upgrade.codechallenge.controller.ReservationController;
import com.upgrade.codechallenge.exception.InternalServerErrorException;
import com.upgrade.codechallenge.exception.OccupiedDateRangeException;
import com.upgrade.codechallenge.model.Reservation;
import com.upgrade.codechallenge.service.ReservationService;
import com.upgrade.codechallenge.util.Response;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.nullable;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@RunWith(MockitoJUnitRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class ReservationControllerTests {

	private MockMvc mockMvc;

	@Mock
	private ReservationService reservationService;
	@InjectMocks
	private ReservationController reservationController;
	private Gson gson;

	@Before
	public void setup() {
		JacksonTester.initFields(this, new ObjectMapper());
		this.mockMvc = MockMvcBuilders
				.standaloneSetup(reservationController)
				.build();
		this.gson = new GsonBuilder()
				.setPrettyPrinting()
				.excludeFieldsWithoutExposeAnnotation()
				.serializeNulls()
				.create();
	}

	//POST TESTS
	@Test
	public void shouldCreateReservation() throws Exception {
		Mockito.when(reservationService
				.saveReservation(Mockito.any(Reservation.class)))
				.thenReturn(new Response(null, UUID.randomUUID(), HttpStatus.OK));

		JSONObject reservation = new JSONObject();
		reservation.put("arrivalDate", LocalDate.now().plusDays(3));
		reservation.put("departureDate", LocalDate.now().plusDays(6));
		reservation.put("name", "Agustin Chirichigno");
		reservation.put("email", "chirichignoa@gmail.com");

		MockHttpServletResponse response = this.mockMvc.perform(post("/reservation")
																.contentType(MediaType.APPLICATION_JSON)
																.content(reservation.toString()))
														.andReturn()
														.getResponse();

		// then
		assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
	}

	@Test
	public void shouldNotCreateReservationCampReserved() throws Exception {
		Mockito.when(reservationService
				.saveReservation(Mockito.any(Reservation.class)))
				.thenThrow(new OccupiedDateRangeException("The camp is already reserved for that date range."));

		JSONObject reservation = new JSONObject();
		reservation.put("arrivalDate", LocalDate.now().plusDays(3));
		reservation.put("departureDate", LocalDate.now().plusDays(6));
		reservation.put("name", "Agustin Chirichigno");
		reservation.put("email", "chirichignoa@gmail.com");

		MockHttpServletResponse response = this.mockMvc.perform(post("/reservation")
				.contentType(MediaType.APPLICATION_JSON)
				.content(reservation.toString()))
				.andReturn()
				.getResponse();

		// then
		assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
	}

	@Test
	public void shouldNotCreateReservationMoreThanOneMonthInAdvance() throws Exception {
		Mockito.when(reservationService
				.saveReservation(Mockito.any(Reservation.class)))
				.thenReturn(new Response("The campsite can be reserved minimum 1 day(s) ahead of arrival and up to 1 month in advance.", null, HttpStatus.BAD_REQUEST));

		JSONObject reservation = new JSONObject();
		reservation.put("arrivalDate", LocalDate.now().plusDays(60));
		reservation.put("departureDate", LocalDate.now().plusDays(63));
		reservation.put("name", "Agustin Chirichigno");
		reservation.put("email", "chirichignoa@gmail.com");

		MockHttpServletResponse response = this.mockMvc.perform(post("/reservation")
				.contentType(MediaType.APPLICATION_JSON)
				.content(reservation.toString()))
				.andReturn()
				.getResponse();

		// then
		assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
	}

	@Test
	public void shouldNotCreateReservationLessThanOneDayForTheArrival() throws Exception {
		Mockito.when(reservationService
				.saveReservation(Mockito.any(Reservation.class)))
				.thenReturn(new Response("The campsite can be reserved minimum 1 day(s) ahead of arrival and up to 1 month in advance.", null, HttpStatus.BAD_REQUEST));

		JSONObject reservation = new JSONObject();
		reservation.put("arrivalDate", LocalDate.now());
		reservation.put("departureDate", LocalDate.now().plusDays(3));
		reservation.put("name", "Agustin Chirichigno");
		reservation.put("email", "chirichignoa@gmail.com");

		MockHttpServletResponse response = this.mockMvc.perform(post("/reservation")
				.contentType(MediaType.APPLICATION_JSON)
				.content(reservation.toString()))
				.andReturn()
				.getResponse();

		// then
		assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
	}

	@Test
	public void shouldNotCreateReservationDepartureDateIsBeforeThanArrival() throws Exception {
		Mockito.when(reservationService
				.saveReservation(Mockito.any(Reservation.class)))
				.thenReturn(new Response("The arrival date is greater than departure date.", null, HttpStatus.BAD_REQUEST));

		JSONObject reservation = new JSONObject();
		reservation.put("arrivalDate", LocalDate.now().plusDays(6));
		reservation.put("departureDate", LocalDate.now().plusDays(1));
		reservation.put("name", "Agustin Chirichigno");
		reservation.put("email", "chirichignoa@gmail.com");

		MockHttpServletResponse response = this.mockMvc.perform(post("/reservation")
				.contentType(MediaType.APPLICATION_JSON)
				.content(reservation.toString()))
				.andReturn()
				.getResponse();

		// then
		assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
	}

	@Test
	public void shouldNotCreateReservationGreaterThanThreeDays() throws Exception {
		Mockito.when(reservationService
				.saveReservation(Mockito.any(Reservation.class)))
				.thenReturn(new Response("The campsite can be reserved for max 3 days.", null, HttpStatus.BAD_REQUEST));

		JSONObject reservation = new JSONObject();
		reservation.put("arrivalDate", LocalDate.now().plusDays(2));
		reservation.put("departureDate", LocalDate.now().plusDays(10));
		reservation.put("name", "Agustin Chirichigno");
		reservation.put("email", "chirichignoa@gmail.com");

		MockHttpServletResponse response = this.mockMvc.perform(post("/reservation")
				.contentType(MediaType.APPLICATION_JSON)
				.content(reservation.toString()))
				.andReturn()
				.getResponse();

		// then
		assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
	}

	@Test
	public void shouldNotCreateReservationArrivalDateIsMissing() throws Exception {
		Mockito.when(reservationService
				.saveReservation(Mockito.any(Reservation.class)))
				.thenReturn(new Response("You must specify an arrival date.", null, HttpStatus.BAD_REQUEST));

		JSONObject reservation = new JSONObject();
		reservation.put("departureDate", LocalDate.of(2019,2,22));
		reservation.put("name", "Agustin Chirichigno");
		reservation.put("email", "chirichignoa@gmail.com");

		MockHttpServletResponse response = this.mockMvc.perform(post("/reservation")
				.contentType(MediaType.APPLICATION_JSON)
				.content(reservation.toString()))
				.andReturn()
				.getResponse();

		// then
		assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
	}

	@Test
	public void shouldNotCreateReservationDepartureDateIsMissing() throws Exception {
		Mockito.when(reservationService
				.saveReservation(Mockito.any(Reservation.class)))
				.thenReturn(new Response("You must specify a departure date.", null, HttpStatus.BAD_REQUEST));

		JSONObject reservation = new JSONObject();
		reservation.put("arrivalDate", LocalDate.of(2019,2,19));
		reservation.put("name", "Agustin Chirichigno");
		reservation.put("email", "chirichignoa@gmail.com");

		MockHttpServletResponse response = this.mockMvc.perform(post("/reservation")
				.contentType(MediaType.APPLICATION_JSON)
				.content(reservation.toString()))
				.andReturn()
				.getResponse();

		// then
		assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
	}

	// GET TESTS
	@Test
	public void shouldGetReservations() throws Exception {
		String resourceId = UUID.randomUUID().toString();
		List<Reservation> reservationList = new ArrayList<>();
		reservationList.add(new Reservation(LocalDate.now(), LocalDate.now().plusDays(1),	"Agustin Chirichigno", "chirichignoa@gmail.com", resourceId));
		reservationList.add(new Reservation(LocalDate.now(), LocalDate.now().plusDays(2),	"Agustin Chirichigno", "chirichignoa@gmail.com", resourceId));
		reservationList.add(new Reservation(LocalDate.now(), LocalDate.now().plusDays(3),	"Agustin Chirichigno", "chirichignoa@gmail.com", resourceId));
		resourceId = UUID.randomUUID().toString();
		reservationList.add(new Reservation(LocalDate.now().plusDays(3), LocalDate.now().plusDays(4),	"Juan Perez", "jp@gmail.com", resourceId));
		reservationList.add(new Reservation(LocalDate.now().plusDays(4), LocalDate.now().plusDays(5),	"Juan Perez", "jp@gmail.com", resourceId));
		reservationList.add(new Reservation(LocalDate.now().plusDays(5), LocalDate.now().plusDays(6),	"Juan Perez", "jp@gmail.com", resourceId));

		Mockito.when(reservationService
				.getReservations(Mockito.any(LocalDate.class), Mockito.any(LocalDate.class)))
				.thenReturn(new Response(null, this.gson.toJson(reservationList), HttpStatus.OK));

		MockHttpServletResponse response = this.mockMvc.perform(get("/reservation?arrival=2019-02-10&departure=2019-02-28")).andReturn().getResponse();
		// then
		assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
	}

	@Test
	public void shouldNotGetReservationsArrivalDateIsMissing() throws Exception {
		Mockito.when(reservationService
				.getReservations(nullable(LocalDate.class), Mockito.any(LocalDate.class)))
				.thenReturn(new Response("You must specify an arrival date.", null, HttpStatus.BAD_REQUEST));

		MockHttpServletResponse response = this.mockMvc.perform(get("/reservation?departure=2019-02-28")).andReturn().getResponse();
		// then
		assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
	}

	@Test
	public void shouldNotGetReservationsDepartureDateIsBeforeThanArrival() throws Exception {
		Mockito.when(reservationService
				.getReservations(Mockito.any(LocalDate.class), Mockito.any(LocalDate.class)))
				.thenReturn(new Response("You must specify an arrival date.", null, HttpStatus.BAD_REQUEST));

		MockHttpServletResponse response = this.mockMvc.perform(get("/reservation?arrival=2019-03-10&departure=2019-02-28")).andReturn().getResponse();
		// then
		assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
	}

	@Test
	public void shouldGetReservation() throws Exception {
		String resourceId = UUID.randomUUID().toString();
		List<Reservation> reservationList = new ArrayList<>();
		reservationList.add(new Reservation(LocalDate.now(), LocalDate.now().plusDays(1),	"Agustin Chirichigno", "chirichignoa@gmail.com", resourceId));
		reservationList.add(new Reservation(LocalDate.now(), LocalDate.now().plusDays(2),	"Agustin Chirichigno", "chirichignoa@gmail.com", resourceId));
		reservationList.add(new Reservation(LocalDate.now(), LocalDate.now().plusDays(3),	"Agustin Chirichigno", "chirichignoa@gmail.com", resourceId));
		Mockito.when(reservationService
				.getReservation(Mockito.anyString()))
				.thenReturn(new Response(null, this.gson.toJson(reservationList), HttpStatus.OK));

		MockHttpServletResponse response = this.mockMvc.perform(get("/reservation/" + resourceId)).andReturn().getResponse();
		// then
		assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
	}

	@Test
	public void shouldNotGetReservation() throws Exception {
		String resourceId = UUID.randomUUID().toString();
		List<Reservation> reservationList = new ArrayList<>();
		reservationList.add(new Reservation(LocalDate.now(), LocalDate.now().plusDays(1),	"Agustin Chirichigno", "chirichignoa@gmail.com", resourceId));
		reservationList.add(new Reservation(LocalDate.now(), LocalDate.now().plusDays(2),	"Agustin Chirichigno", "chirichignoa@gmail.com", resourceId));
		reservationList.add(new Reservation(LocalDate.now(), LocalDate.now().plusDays(3),	"Agustin Chirichigno", "chirichignoa@gmail.com", resourceId));
		resourceId = UUID.randomUUID().toString();
		Mockito.when(reservationService
				.getReservation(Mockito.anyString()))
				.thenReturn(new Response("Reservation does not exist", null, HttpStatus.NOT_FOUND));

		MockHttpServletResponse response = this.mockMvc.perform(get("/reservation/" + resourceId)).andReturn().getResponse();
		// then
		assertThat(response.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
	}

	// PATCH TESTS
	@Test
	public void shouldUpdateReservation() throws Exception {
		// Assuming that exists a reservation with resourceId for three days
		String resourceId = UUID.randomUUID().toString();

		Mockito.when(reservationService
				.updateReservation(Mockito.any(String.class), Mockito.any(Reservation.class)))
				.thenReturn(new Response(null, resourceId, HttpStatus.OK));


		JSONObject reservation = new JSONObject();
		reservation.put("arrivalDate", LocalDate.now().plusDays(1));
		reservation.put("departureDate", LocalDate.now().plusDays(4));

		MockHttpServletResponse response = this.mockMvc.perform(patch("/reservation/" + resourceId)
				.contentType(MediaType.APPLICATION_JSON)
				.content(reservation.toString()))
				.andReturn()
				.getResponse();

		// then
		assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
	}

	@Test
	public void shouldNotUpdateReservationDoesNotExists() throws Exception {
		String resourceId = UUID.randomUUID().toString();
		Mockito.when(reservationService
				.updateReservation(Mockito.any(String.class), Mockito.any(Reservation.class)))
				.thenReturn(new Response("Reservation does not exist", null, HttpStatus.NOT_FOUND));

		JSONObject reservation = new JSONObject();
		reservation.put("arrivalDate", LocalDate.now().plusDays(2));
		reservation.put("departureDate", LocalDate.now().plusDays(5));

		MockHttpServletResponse response = this.mockMvc.perform(patch("/reservation/" + resourceId)
				.contentType(MediaType.APPLICATION_JSON)
				.content(reservation.toString()))
				.andReturn()
				.getResponse();

		// then
		assertThat(response.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
	}

	@Test
	public void shouldNotUpdateReservationDifferenceBetweenDateIsLessThanPreviousReservation() throws Exception {
		// Assuming that exists a reservation with resourceId for three days
		String resourceId = UUID.randomUUID().toString();
		Mockito.when(reservationService
				.updateReservation(Mockito.any(String.class), Mockito.any(Reservation.class)))
				.thenReturn(new Response("The new reservation's duration must be the same than the previous", null, HttpStatus.BAD_REQUEST));

		// The new reservation is for two days
		JSONObject reservation = new JSONObject();
		reservation.put("name", "Agustin Chirichigno");
		reservation.put("email", "chirichignoa@gmail.com");
		reservation.put("arrivalDate", LocalDate.now().plusDays(2));
		reservation.put("departureDate", LocalDate.now().plusDays(4));

		MockHttpServletResponse response = this.mockMvc.perform(patch("/reservation/" + resourceId)
				.contentType(MediaType.APPLICATION_JSON)
				.content(reservation.toString()))
				.andReturn()
				.getResponse();

		// then
		assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
	}

	@Test
	public void shouldNotUpdateReservationCampReserved() throws Exception {
		String resourceId = UUID.randomUUID().toString();
		Mockito.when(reservationService
				.updateReservation(Mockito.any(String.class), Mockito.any(Reservation.class)))
				.thenThrow(new OccupiedDateRangeException("The camp is already reserved for that date range."));

		// The new reservation is for two days
		JSONObject reservation = new JSONObject();
		reservation.put("arrivalDate", LocalDate.now().plusDays(2));
		reservation.put("departureDate", LocalDate.now().plusDays(4));

		MockHttpServletResponse response = this.mockMvc.perform(patch("/reservation/" + resourceId)
				.contentType(MediaType.APPLICATION_JSON)
				.content(reservation.toString()))
				.andReturn()
				.getResponse();

		// then
		assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
	}

	// DELETE TESTS
	@Test
	public void shouldDeleteReservation() throws Exception {
		// Assuming that exists a reservation with resourceId for three days
		String resourceId = UUID.randomUUID().toString();

		Mockito.when(reservationService
				.deleteReservation(Mockito.anyString()))
				.thenReturn(new Response(null, null, HttpStatus.OK));

		MockHttpServletResponse response = this.mockMvc.perform(delete("/reservation/" + resourceId))
				.andReturn()
				.getResponse();

		// then
		assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
	}

	@Test
	public void shouldNotDeleteReservationDoesNotExist() throws Exception {
		String resourceId = UUID.randomUUID().toString();

		Mockito.when(reservationService
				.deleteReservation(Mockito.anyString()))
				.thenReturn(new Response("Reservation does not exist", null, HttpStatus.NOT_FOUND));

		MockHttpServletResponse response = this.mockMvc.perform(delete("/reservation/" + resourceId))
				.andReturn()
				.getResponse();

		// then
		assertThat(response.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
	}

	@Test
	public void shouldNotDeleteReservationInternalServerError() throws Exception {
		String resourceId = UUID.randomUUID().toString();

		Mockito.when(reservationService
				.deleteReservation(Mockito.anyString()))
				.thenThrow(new InternalServerErrorException("Some weird exception"));

		MockHttpServletResponse response = this.mockMvc.perform(delete("/reservation/" + resourceId))
				.andReturn()
				.getResponse();

		// then
		assertThat(response.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
	}
}

