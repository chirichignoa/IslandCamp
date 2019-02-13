package com.upgrade.codechallenge;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.upgrade.codechallenge.exception.InternalServerErrorException;
import com.upgrade.codechallenge.exception.OcuppedDateRangeException;
import com.upgrade.codechallenge.model.Reservation;
import com.upgrade.codechallenge.repository.ReservationRepository;
import com.upgrade.codechallenge.service.ReservationServiceImpl;
import com.upgrade.codechallenge.util.Response;
import org.apache.tomcat.jni.Local;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class ReservationServiceTests {

    private MockMvc mockMvc;

    @Mock
    private ReservationRepository reservationRepository;
    @InjectMocks
    private ReservationServiceImpl reservationService;
    private Gson gson;

    @Before
    public void setup() {
        JacksonTester.initFields(this, new ObjectMapper());
        this.mockMvc = MockMvcBuilders
                .standaloneSetup(reservationService)
                .build();
        this.gson = new GsonBuilder()
                .setPrettyPrinting()
                .excludeFieldsWithoutExposeAnnotation()
                .serializeNulls()
                .create();
    }

    // Save Reservation
    @Test
    public void shouldSaveReservation() throws Exception {
        Reservation r = new Reservation(LocalDate.now().plusDays(2),
                                        LocalDate.now().plusDays(5),
                                        "Agustin Chirichigno",
                                        "chirichignoa@gmail.com");

        // Mock repository's save method to assign an id
        Mockito.when(reservationRepository.save(Mockito.any(Reservation.class)))
                .thenAnswer((Answer<Reservation>) invocation -> {
                    Reservation reservation = (Reservation) invocation.getArguments()[0];
                    reservation.setId(1 + (long) (Math.random() * (10 - 1)));
                    return reservation;
                });

        Response response = this.reservationService.saveReservation(r);
        assertThat(response.getContent().toString().length()).isGreaterThan(0);
        assertThat(response.getError()).isEqualTo(null);
    }

    @Test
    public void shouldNotSaveReservationNotArrivalDateAndDepartureDate() {
        Reservation r = new Reservation(null,
                                        LocalDate.now().plusDays(5),
                                        "Agustin Chirichigno",
                                        "chirichignoa@gmail.com");
        Response correctResponse = new Response("You must specify an arrival date.", null, HttpStatus.BAD_REQUEST);


        Response response = this.reservationService.saveReservation(r);
        assertThat(response.getContent()).isEqualTo(null);
        assertThat(response).isEqualTo(correctResponse);
    }

    @Test
    public void shouldNotSaveReservationNotDepartureDate() {
        Reservation r = new Reservation(LocalDate.now().plusDays(2),
                                        null,
                                        "Agustin Chirichigno",
                                        "chirichignoa@gmail.com");
        Response correctResponse = new Response("You must specify a departure date.", null, HttpStatus.BAD_REQUEST);

        Response response = this.reservationService.saveReservation(r);
        assertThat(response.getContent()).isEqualTo(null);
        assertThat(response).isEqualTo(correctResponse);
    }

    @Test
    public void  shouldNotSaveReservationArrivalDateIsGreaterThanDepartureDate() {
        Reservation r = new Reservation(LocalDate.now().plusDays(10),
                                        LocalDate.now().plusDays(2),
                                        "Agustin Chirichigno",
                                        "chirichignoa@gmail.com");
        Response correctResponse = new Response("The arrival date is greater than departure date.", null, HttpStatus.BAD_REQUEST);

        Response response = this.reservationService.saveReservation(r);
        assertThat(response.getContent()).isEqualTo(null);
        assertThat(response).isEqualTo(correctResponse);
    }

    @Test
    public void  shouldNotSaveReservationMoreThanOneMonthInAdvance() {
        Reservation r = new Reservation(LocalDate.now().plusDays(60),
                LocalDate.now().plusDays(63),
                "Agustin Chirichigno",
                "chirichignoa@gmail.com");
        Response correctResponse = new Response("The campsite can be reserved minimum 1 day(s) ahead of arrival and up to 1 month in advance.", null, HttpStatus.BAD_REQUEST);

        Response response = this.reservationService.saveReservation(r);
        assertThat(response.getContent()).isEqualTo(null);
        assertThat(response).isEqualTo(correctResponse);
    }

    @Test
    public void  shouldNotSaveReservationLessThanOneDayForTheArrival() {
        Reservation r = new Reservation(LocalDate.now(),
                                        LocalDate.now().plusDays(3),
                                        "Agustin Chirichigno",
                                        "chirichignoa@gmail.com");
        Response correctResponse = new Response("The campsite can be reserved minimum 1 day(s) ahead of arrival and up to 1 month in advance.", null, HttpStatus.BAD_REQUEST);

        Response response = this.reservationService.saveReservation(r);
        assertThat(response.getContent()).isEqualTo(null);
        assertThat(response).isEqualTo(correctResponse);
    }

    @Test
    public void shouldNotSaveReservationMoreThanThreeDays() {
        Reservation r = new Reservation(LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(9),
                "Agustin Chirichigno",
                "chirichignoa@gmail.com");
        Response correctResponse = new Response("The campsite can be reserved for max 3 days.", null, HttpStatus.BAD_REQUEST);

        Response response = this.reservationService.saveReservation(r);
        assertThat(response.getContent()).isEqualTo(null);
        assertThat(response).isEqualTo(correctResponse);
    }

    @Test
    public void shouldNotSaveReservationCampReserved() {
        Reservation r = new Reservation(LocalDate.now(),
                LocalDate.now().plusDays(9),
                "Agustin Chirichigno",
                "chirichignoa@gmail.com");

        Mockito.lenient().when(this.reservationRepository.save(Mockito.any(Reservation.class)))
                .thenThrow(new RuntimeException("Some SQL exception"));
        try {
            Response response = this.reservationService.saveReservation(r);
        } catch (RuntimeException e) {
            assertThat(e.getMessage()).isEqualTo("Some SQL exception");
        }
    }

    // Get Reservations
    @Test
    public void shouldGetReservations() {
        String resourceId = UUID.randomUUID().toString();
        List<Reservation> reservationList = new ArrayList<>();
        reservationList.add(new Reservation(LocalDate.now(), LocalDate.now().plusDays(1),	"Agustin Chirichigno", "chirichignoa@gmail.com", resourceId));
        reservationList.add(new Reservation(LocalDate.now(), LocalDate.now().plusDays(2),	"Agustin Chirichigno", "chirichignoa@gmail.com", resourceId));
        reservationList.add(new Reservation(LocalDate.now(), LocalDate.now().plusDays(3),	"Agustin Chirichigno", "chirichignoa@gmail.com", resourceId));
        resourceId = UUID.randomUUID().toString();
        reservationList.add(new Reservation(LocalDate.now().plusDays(3), LocalDate.now().plusDays(4),	"Juan Perez", "jp@gmail.com", resourceId));
        reservationList.add(new Reservation(LocalDate.now().plusDays(4), LocalDate.now().plusDays(5),	"Juan Perez", "jp@gmail.com", resourceId));
        reservationList.add(new Reservation(LocalDate.now().plusDays(5), LocalDate.now().plusDays(6),	"Juan Perez", "jp@gmail.com", resourceId));

        Mockito.when(this.reservationRepository
                .findByArrivalDateGreaterThanEqualAndDepartureDateLessThanEqual(Mockito.any(LocalDate.class), Mockito.any(LocalDate.class)))
                .thenReturn(reservationList);

        Response response = this.reservationService.getReservations(LocalDate.now(), LocalDate.now().plusDays(20));
        assertThat(response.getError()).isEqualTo(null);
        assertThat(response.getContent().toString().length()).isGreaterThan(0);
        assertThat(((List<Reservation>)response.getContent()).size()).isEqualTo(reservationList.size());
    }

    @Test
    public void shouldNotGetReservationsArrivalDateMissing() {
        Response correctResponse = new Response("You must specify an arrival date.", null, HttpStatus.BAD_REQUEST);

        Response response = this.reservationService.getReservations(null, LocalDate.now().plusDays(20));
        assertThat(response.getContent()).isEqualTo(null);
        assertThat(response).isEqualTo(correctResponse);
    }

    @Test
    public void shouldGetReservationsWithDepartureDateMissing() {
        String resourceId = UUID.randomUUID().toString();
        List<Reservation> reservationList = new ArrayList<>();
        reservationList.add(new Reservation(LocalDate.now(), LocalDate.now().plusDays(1),	"Agustin Chirichigno", "chirichignoa@gmail.com", resourceId));
        reservationList.add(new Reservation(LocalDate.now(), LocalDate.now().plusDays(2),	"Agustin Chirichigno", "chirichignoa@gmail.com", resourceId));
        reservationList.add(new Reservation(LocalDate.now(), LocalDate.now().plusDays(3),	"Agustin Chirichigno", "chirichignoa@gmail.com", resourceId));
        resourceId = UUID.randomUUID().toString();
        reservationList.add(new Reservation(LocalDate.now().plusDays(3), LocalDate.now().plusDays(4),	"Juan Perez", "jp@gmail.com", resourceId));
        reservationList.add(new Reservation(LocalDate.now().plusDays(4), LocalDate.now().plusDays(5),	"Juan Perez", "jp@gmail.com", resourceId));
        reservationList.add(new Reservation(LocalDate.now().plusDays(5), LocalDate.now().plusDays(6),	"Juan Perez", "jp@gmail.com", resourceId));

        Mockito.when(this.reservationRepository
                .findByArrivalDateGreaterThanEqualAndDepartureDateLessThanEqual(Mockito.any(LocalDate.class), Mockito.any(LocalDate.class)))
                .thenReturn(reservationList);

        Response response = this.reservationService.getReservations(LocalDate.now(), null);
        assertThat(response.getError()).isEqualTo(null);
        assertThat(response.getContent().toString().length()).isGreaterThan(0);
        assertThat(((List<Reservation>)response.getContent()).size()).isEqualTo(reservationList.size());
    }

    @Test
    public void shouldNotGetReservationsArrivalDateIsGreaterThanDepartureDate() {
        Response correctResponse = new Response("Arrival date is greater than departure date.", null, HttpStatus.BAD_REQUEST);

        Response response = this.reservationService.getReservations(LocalDate.now().plusDays(25), LocalDate.now().plusDays(20));
        assertThat(response.getContent()).isEqualTo(null);
        assertThat(response).isEqualTo(correctResponse);
    }

    // Get Reservation
    @Test
    public void shouldGetReservation() {
        String resourceId = UUID.randomUUID().toString();
        List<Reservation> reservationList = new ArrayList<>();
        reservationList.add(new Reservation(LocalDate.now(), LocalDate.now().plusDays(1),	"Agustin Chirichigno", "chirichignoa@gmail.com", resourceId));
        reservationList.add(new Reservation(LocalDate.now(), LocalDate.now().plusDays(2),	"Agustin Chirichigno", "chirichignoa@gmail.com", resourceId));
        reservationList.add(new Reservation(LocalDate.now(), LocalDate.now().plusDays(3),	"Agustin Chirichigno", "chirichignoa@gmail.com", resourceId));

        // Mock repository's save method to assign an id
        Mockito.when(reservationRepository.findReservationByResourceId(Mockito.anyString()))
                .thenReturn(reservationList);

        Response response = this.reservationService.getReservation(resourceId);
        assertThat(response.getContent().toString().length()).isGreaterThan(0);
        assertThat(response.getError()).isEqualTo(null);
        for(Reservation reservation: (List<Reservation>)response.getContent()) {
            assertThat(reservation.getResourceId()).isEqualTo(resourceId);
        }
    }

    @Test
    public void shouldNotGetReservationResourceIdDoesNotExist() {
        String resourceId = UUID.randomUUID().toString();
        Response correctResponse = new Response("Reservation does not exist", null, HttpStatus.NOT_FOUND);

        // Mock repository's save method to assign an id
        Mockito.when(reservationRepository.findReservationByResourceId(Mockito.anyString()))
                .thenReturn(new ArrayList<>());

        Response response = this.reservationService.getReservation(resourceId);
        assertThat(response.getContent()).isEqualTo(null);
        assertThat(response).isEqualTo(correctResponse);
    }

    // Update Reservation
    @Test
    public void shouldUpdateReservation() {
        String resourceId = UUID.randomUUID().toString();
        List<Reservation> reservationList = new ArrayList<>();
        Reservation r = new Reservation(LocalDate.now().plusDays(3), LocalDate.now().plusDays(4),	"Agustin Chirichigno", "chirichignoa@gmail.com", resourceId);
        r.setId(1L);
        reservationList.add(r);
        r = new Reservation(LocalDate.now().plusDays(4), LocalDate.now().plusDays(5),	"Agustin Chirichigno", "chirichignoa@gmail.com", resourceId);
        r.setId(2L);
        reservationList.add(r);
        r = new Reservation(LocalDate.now().plusDays(5), LocalDate.now().plusDays(6),	"Agustin Chirichigno", "chirichignoa@gmail.com", resourceId);
        r.setId(2L);
        reservationList.add(r);
        Response correctResponse = new Response(null, resourceId, HttpStatus.OK);
        // Mock repository's save method to assign an id
        Mockito.when(this.reservationRepository.findReservationByResourceId(Mockito.anyString()))
                .thenReturn(reservationList);

        Reservation toModify = new Reservation(LocalDate.now().plusDays(8), LocalDate.now().plusDays(11), null, null, resourceId);

        Response response = this.reservationService.updateReservation(resourceId,toModify);
        assertThat(response).isEqualTo(correctResponse);
    }

    @Test
    public void shouldNotUpdateReservationCampReserved() {
        String resourceId = UUID.randomUUID().toString();
        List<Reservation> reservationList = new ArrayList<>();
        Reservation r = new Reservation(LocalDate.now().plusDays(3), LocalDate.now().plusDays(4),	"Agustin Chirichigno", "chirichignoa@gmail.com", resourceId);
        r.setId(1L);
        reservationList.add(r);
        r = new Reservation(LocalDate.now().plusDays(4), LocalDate.now().plusDays(5),	"Agustin Chirichigno", "chirichignoa@gmail.com", resourceId);
        r.setId(2L);
        reservationList.add(r);
        r = new Reservation(LocalDate.now().plusDays(5), LocalDate.now().plusDays(6),	"Agustin Chirichigno", "chirichignoa@gmail.com", resourceId);
        r.setId(2L);
        reservationList.add(r);
        Response correctResponse = new Response(null, resourceId, HttpStatus.OK);
        // Mock repository's save method to assign an id
        Mockito.when(this.reservationRepository.findReservationByResourceId(Mockito.anyString()))
                .thenReturn(reservationList);

        Mockito.doThrow(new OcuppedDateRangeException("The camp is already reserved for that date range."))
                .when(this.reservationRepository)
                .updateReservationDates(Mockito.any(LocalDate.class), Mockito.any(LocalDate.class),Mockito.anyLong());

        Reservation toModify = new Reservation(LocalDate.now().plusDays(8), LocalDate.now().plusDays(11), null, null, resourceId);
        try {
            Response response = this.reservationService.updateReservation(resourceId,toModify);
        } catch (OcuppedDateRangeException e) {
            assertThat(e.getMessage()).isEqualTo("The camp is already reserved for that date range.");
        }
    }

    @Test
    public void shouldNotUpdateReservationDoesNotExists() {
        String resourceId = UUID.randomUUID().toString();
        Response correctResponse = new Response("Reservation does not exist", null, HttpStatus.NOT_FOUND);
        Reservation toModify = new Reservation(LocalDate.now().plusDays(8), LocalDate.now().plusDays(11), null, null, resourceId);
        Response response = this.reservationService.updateReservation(resourceId,toModify);
        assertThat(response).isEqualTo(correctResponse);
    }

    @Test
    public void shouldNotUpdateReservationArrivalDateIsGreaterThaDeparture() {
        String resourceId = UUID.randomUUID().toString();
        List<Reservation> reservationList = new ArrayList<>();
        Reservation r = new Reservation(LocalDate.now().plusDays(3), LocalDate.now().plusDays(4),	"Agustin Chirichigno", "chirichignoa@gmail.com", resourceId);
        r.setId(1L);
        reservationList.add(r);
        r = new Reservation(LocalDate.now().plusDays(4), LocalDate.now().plusDays(5),	"Agustin Chirichigno", "chirichignoa@gmail.com", resourceId);
        r.setId(2L);
        reservationList.add(r);
        r = new Reservation(LocalDate.now().plusDays(5), LocalDate.now().plusDays(6),	"Agustin Chirichigno", "chirichignoa@gmail.com", resourceId);
        r.setId(2L);
        reservationList.add(r);
        Response correctResponse = new Response("The arrival date is greater than departure date.", null, HttpStatus.BAD_REQUEST);
        // Mock repository's save method to assign an id
        Mockito.when(this.reservationRepository.findReservationByResourceId(Mockito.anyString()))
                .thenReturn(reservationList);

        Reservation toModify = new Reservation(LocalDate.now().plusDays(11), LocalDate.now().plusDays(8), null, null, resourceId);
        Response response = this.reservationService.updateReservation(resourceId,toModify);
        assertThat(response.getContent()).isEqualTo(null);
        assertThat(response).isEqualTo(correctResponse);
    }

    @Test
    public void shouldNotUpdateReservationMoreThanOneMonthInAdvance() {
        String resourceId = UUID.randomUUID().toString();
        List<Reservation> reservationList = new ArrayList<>();
        Reservation r = new Reservation(LocalDate.now().plusDays(3), LocalDate.now().plusDays(4),	"Agustin Chirichigno", "chirichignoa@gmail.com", resourceId);
        r.setId(1L);
        reservationList.add(r);
        r = new Reservation(LocalDate.now().plusDays(4), LocalDate.now().plusDays(5),	"Agustin Chirichigno", "chirichignoa@gmail.com", resourceId);
        r.setId(2L);
        reservationList.add(r);
        r = new Reservation(LocalDate.now().plusDays(5), LocalDate.now().plusDays(6),	"Agustin Chirichigno", "chirichignoa@gmail.com", resourceId);
        r.setId(2L);
        reservationList.add(r);
        Response correctResponse = new Response("The campsite can be reserved minimum 1 day(s) ahead of arrival and up to 1 month in advance.", null, HttpStatus.BAD_REQUEST);
        // Mock repository's save method to assign an id
        Mockito.when(this.reservationRepository.findReservationByResourceId(Mockito.anyString()))
                .thenReturn(reservationList);

        Reservation toModify = new Reservation(LocalDate.now().plusDays(60), LocalDate.now().plusDays(68), null, null, resourceId);
        Response response = this.reservationService.updateReservation(resourceId,toModify);
        assertThat(response.getContent()).isEqualTo(null);
        assertThat(response).isEqualTo(correctResponse);
    }

    @Test
    public void shouldNotUpdateReservationLessThanOneDayToArrival() {
        String resourceId = UUID.randomUUID().toString();
        List<Reservation> reservationList = new ArrayList<>();
        Reservation r = new Reservation(LocalDate.now().plusDays(3), LocalDate.now().plusDays(4),	"Agustin Chirichigno", "chirichignoa@gmail.com", resourceId);
        r.setId(1L);
        reservationList.add(r);
        r = new Reservation(LocalDate.now().plusDays(4), LocalDate.now().plusDays(5),	"Agustin Chirichigno", "chirichignoa@gmail.com", resourceId);
        r.setId(2L);
        reservationList.add(r);
        r = new Reservation(LocalDate.now().plusDays(5), LocalDate.now().plusDays(6),	"Agustin Chirichigno", "chirichignoa@gmail.com", resourceId);
        r.setId(2L);
        reservationList.add(r);
        Response correctResponse = new Response("The campsite can be reserved minimum 1 day(s) ahead of arrival and up to 1 month in advance.", null, HttpStatus.BAD_REQUEST);
        // Mock repository's save method to assign an id
        Mockito.when(this.reservationRepository.findReservationByResourceId(Mockito.anyString()))
                .thenReturn(reservationList);

        Reservation toModify = new Reservation(LocalDate.now(), LocalDate.now().plusDays(3), null, null, resourceId);
        Response response = this.reservationService.updateReservation(resourceId,toModify);
        assertThat(response.getContent()).isEqualTo(null);
        assertThat(response).isEqualTo(correctResponse);
    }

    @Test
    public void shouldNotUpdateReservationMoreThanThreeDays() {
        String resourceId = UUID.randomUUID().toString();
        List<Reservation> reservationList = new ArrayList<>();
        Reservation r = new Reservation(LocalDate.now().plusDays(3), LocalDate.now().plusDays(4),	"Agustin Chirichigno", "chirichignoa@gmail.com", resourceId);
        r.setId(1L);
        reservationList.add(r);
        r = new Reservation(LocalDate.now().plusDays(4), LocalDate.now().plusDays(5),	"Agustin Chirichigno", "chirichignoa@gmail.com", resourceId);
        r.setId(2L);
        reservationList.add(r);
        r = new Reservation(LocalDate.now().plusDays(5), LocalDate.now().plusDays(6),	"Agustin Chirichigno", "chirichignoa@gmail.com", resourceId);
        r.setId(2L);
        reservationList.add(r);
        Response correctResponse = new Response("The campsite can be reserved for max 3 days.", null, HttpStatus.BAD_REQUEST);
        // Mock repository's save method to assign an id
        Mockito.when(this.reservationRepository.findReservationByResourceId(Mockito.anyString()))
                .thenReturn(reservationList);

        Reservation toModify = new Reservation(LocalDate.now().plusDays(1), LocalDate.now().plusDays(7), null, null, resourceId);
        Response response = this.reservationService.updateReservation(resourceId,toModify);
        assertThat(response.getContent()).isEqualTo(null);
        assertThat(response).isEqualTo(correctResponse);
    }

    // Modify Reservation
    @Test
    public void shouldModifyReservation() {
        String resourceId = UUID.randomUUID().toString();
        List<Reservation> reservationList = new ArrayList<>();
        Reservation r = new Reservation(LocalDate.now().plusDays(3), LocalDate.now().plusDays(4),	"Agustin Chirichigno", "chirichignoa@gmail.com", resourceId);
        r.setId(1L);
        reservationList.add(r);
        r = new Reservation(LocalDate.now().plusDays(4), LocalDate.now().plusDays(5),	"Agustin Chirichigno", "chirichignoa@gmail.com", resourceId);
        r.setId(2L);
        reservationList.add(r);
        r = new Reservation(LocalDate.now().plusDays(5), LocalDate.now().plusDays(6),	"Agustin Chirichigno", "Agustin Chirichigno", resourceId);
        r.setId(2L);
        reservationList.add(r);
        Response correctResponse = new Response(null, resourceId, HttpStatus.OK);
        // Mock repository's save method to assign an id
        Mockito.when(this.reservationRepository.findReservationByResourceId(Mockito.anyString()))
                .thenReturn(reservationList);

        Reservation toModify = new Reservation(LocalDate.now().plusDays(8),
                                                LocalDate.now().plusDays(11),
                                                "Agustin Chirichigno",
                                                "Agustin Chirichigno",
                                                resourceId);

        Response response = this.reservationService.modifyReservation(resourceId,toModify);
        assertThat(response).isEqualTo(correctResponse);
    }

    @Test
    public void shouldNotModifyReservationCampReserved() {
        String resourceId = UUID.randomUUID().toString();
        List<Reservation> reservationList = new ArrayList<>();
        Reservation r = new Reservation(LocalDate.now().plusDays(3), LocalDate.now().plusDays(4),	"Agustin Chirichigno", "chirichignoa@gmail.com", resourceId);
        r.setId(1L);
        reservationList.add(r);
        r = new Reservation(LocalDate.now().plusDays(4), LocalDate.now().plusDays(5),	"Agustin Chirichigno", "chirichignoa@gmail.com", resourceId);
        r.setId(2L);
        reservationList.add(r);
        r = new Reservation(LocalDate.now().plusDays(5), LocalDate.now().plusDays(6),	"Agustin Chirichigno", "chirichignoa@gmail.com", resourceId);
        r.setId(2L);
        reservationList.add(r);
        Response correctResponse = new Response(null, resourceId, HttpStatus.OK);
        // Mock repository's save method to assign an id
        Mockito.when(this.reservationRepository.findReservationByResourceId(Mockito.anyString()))
                .thenReturn(reservationList);

        Mockito.doThrow(new OcuppedDateRangeException("The camp is already reserved for that date range."))
                .when(this.reservationRepository)
                .modifyReservation(Mockito.anyString(), Mockito.anyString(), Mockito.any(LocalDate.class), Mockito.any(LocalDate.class), Mockito.anyLong());

        Reservation toModify = new Reservation(LocalDate.now().plusDays(8),
                LocalDate.now().plusDays(11),
                "Agustin Chirichigno",
                "Agustin Chirichigno",
                resourceId);
        try {
            Response response = this.reservationService.modifyReservation(resourceId,toModify);
        } catch (OcuppedDateRangeException e) {
            assertThat(e.getMessage()).isEqualTo("The camp is already reserved for that date range.");
        }
    }

    @Test
    public void shouldNotModifyReservationDoesNotExists() {
        String resourceId = UUID.randomUUID().toString();
        Response correctResponse = new Response("Reservation does not exist", null, HttpStatus.NOT_FOUND);
        Reservation toModify = new Reservation(LocalDate.now().plusDays(8),
                LocalDate.now().plusDays(11),
                "Agustin Chirichigno",
                "Agustin Chirichigno",
                resourceId);
        Response response = this.reservationService.modifyReservation(resourceId,toModify);
        assertThat(response).isEqualTo(correctResponse);
    }

    @Test
    public void shouldNotModifyReservationArrivalDateIsGreaterThaDeparture() {
        String resourceId = UUID.randomUUID().toString();
        List<Reservation> reservationList = new ArrayList<>();
        Reservation r = new Reservation(LocalDate.now().plusDays(3), LocalDate.now().plusDays(4),	"Agustin Chirichigno", "chirichignoa@gmail.com", resourceId);
        r.setId(1L);
        reservationList.add(r);
        r = new Reservation(LocalDate.now().plusDays(4), LocalDate.now().plusDays(5),	"Agustin Chirichigno", "chirichignoa@gmail.com", resourceId);
        r.setId(2L);
        reservationList.add(r);
        r = new Reservation(LocalDate.now().plusDays(5), LocalDate.now().plusDays(6),	"Agustin Chirichigno", "chirichignoa@gmail.com", resourceId);
        r.setId(2L);
        reservationList.add(r);
        Response correctResponse = new Response("The arrival date is greater than departure date.", null, HttpStatus.BAD_REQUEST);
        // Mock repository's save method to assign an id
        Mockito.when(this.reservationRepository.findReservationByResourceId(Mockito.anyString()))
                .thenReturn(reservationList);

        Reservation toModify = new Reservation(LocalDate.now().plusDays(11),
                                    LocalDate.now().plusDays(8),
                                    "Agustin Chirichigno",
                                    "Agustin Chirichigno",
                                    resourceId);
        Response response = this.reservationService.modifyReservation(resourceId,toModify);
        assertThat(response.getContent()).isEqualTo(null);
        assertThat(response).isEqualTo(correctResponse);
    }

    @Test
    public void shouldNotModifyReservationMoreThanOneMonthInAdvance() {
        String resourceId = UUID.randomUUID().toString();
        List<Reservation> reservationList = new ArrayList<>();
        Reservation r = new Reservation(LocalDate.now().plusDays(3), LocalDate.now().plusDays(4),	"Agustin Chirichigno", "chirichignoa@gmail.com", resourceId);
        r.setId(1L);
        reservationList.add(r);
        r = new Reservation(LocalDate.now().plusDays(4), LocalDate.now().plusDays(5),	"Agustin Chirichigno", "chirichignoa@gmail.com", resourceId);
        r.setId(2L);
        reservationList.add(r);
        r = new Reservation(LocalDate.now().plusDays(5), LocalDate.now().plusDays(6),	"Agustin Chirichigno", "chirichignoa@gmail.com", resourceId);
        r.setId(2L);
        reservationList.add(r);
        Response correctResponse = new Response("The campsite can be reserved minimum 1 day(s) ahead of arrival and up to 1 month in advance.", null, HttpStatus.BAD_REQUEST);
        // Mock repository's save method to assign an id
        Mockito.when(this.reservationRepository.findReservationByResourceId(Mockito.anyString()))
                .thenReturn(reservationList);

        Reservation toModify = new Reservation(LocalDate.now().plusDays(60),
                LocalDate.now().plusDays(68),
                "Agustin Chirichigno",
                "Agustin Chirichigno",
                resourceId);
        Response response = this.reservationService.modifyReservation(resourceId,toModify);
        assertThat(response.getContent()).isEqualTo(null);
        assertThat(response).isEqualTo(correctResponse);
    }

    @Test
    public void shouldNotModifyReservationLessThanOneDayToArrival() {
        String resourceId = UUID.randomUUID().toString();
        List<Reservation> reservationList = new ArrayList<>();
        Reservation r = new Reservation(LocalDate.now().plusDays(3), LocalDate.now().plusDays(4),	"Agustin Chirichigno", "chirichignoa@gmail.com", resourceId);
        r.setId(1L);
        reservationList.add(r);
        r = new Reservation(LocalDate.now().plusDays(4), LocalDate.now().plusDays(5),	"Agustin Chirichigno", "chirichignoa@gmail.com", resourceId);
        r.setId(2L);
        reservationList.add(r);
        r = new Reservation(LocalDate.now().plusDays(5), LocalDate.now().plusDays(6),	"Agustin Chirichigno", "chirichignoa@gmail.com", resourceId);
        r.setId(2L);
        reservationList.add(r);
        Response correctResponse = new Response("The campsite can be reserved minimum 1 day(s) ahead of arrival and up to 1 month in advance.", null, HttpStatus.BAD_REQUEST);
        // Mock repository's save method to assign an id
        Mockito.when(this.reservationRepository.findReservationByResourceId(Mockito.anyString()))
                .thenReturn(reservationList);

        Reservation toModify = new Reservation(LocalDate.now(),
                LocalDate.now().plusDays(3),
                "Agustin Chirichigno",
                "Agustin Chirichigno",
                resourceId);
        Response response = this.reservationService.modifyReservation(resourceId,toModify);
        assertThat(response.getContent()).isEqualTo(null);
        assertThat(response).isEqualTo(correctResponse);
    }

    @Test
    public void shouldNotModifyReservationMoreThanThreeDays() {
        String resourceId = UUID.randomUUID().toString();
        List<Reservation> reservationList = new ArrayList<>();
        Reservation r = new Reservation(LocalDate.now().plusDays(3), LocalDate.now().plusDays(4),	"Agustin Chirichigno", "chirichignoa@gmail.com", resourceId);
        r.setId(1L);
        reservationList.add(r);
        r = new Reservation(LocalDate.now().plusDays(4), LocalDate.now().plusDays(5),	"Agustin Chirichigno", "chirichignoa@gmail.com", resourceId);
        r.setId(2L);
        reservationList.add(r);
        r = new Reservation(LocalDate.now().plusDays(5), LocalDate.now().plusDays(6),	"Agustin Chirichigno", "chirichignoa@gmail.com", resourceId);
        r.setId(2L);
        reservationList.add(r);
        Response correctResponse = new Response("The campsite can be reserved for max 3 days.", null, HttpStatus.BAD_REQUEST);
        // Mock repository's save method to assign an id
        Mockito.when(this.reservationRepository.findReservationByResourceId(Mockito.anyString()))
                .thenReturn(reservationList);

        Reservation toModify = new Reservation(LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(7),
                "Agustin Chirichigno",
                "Agustin Chirichigno",
                resourceId);
        Response response = this.reservationService.modifyReservation(resourceId,toModify);
        assertThat(response.getContent()).isEqualTo(null);
        assertThat(response).isEqualTo(correctResponse);
    }

    // Delete Reservation
    @Test
    public void shouldDeleteReservation() {
        String resourceId = UUID.randomUUID().toString();
        List<Reservation> reservationList = new ArrayList<>();
        reservationList.add(new Reservation(LocalDate.now(), LocalDate.now().plusDays(1),	"Agustin Chirichigno", "chirichignoa@gmail.com", resourceId));
        reservationList.add(new Reservation(LocalDate.now(), LocalDate.now().plusDays(2),	"Agustin Chirichigno", "chirichignoa@gmail.com", resourceId));
        reservationList.add(new Reservation(LocalDate.now(), LocalDate.now().plusDays(3),	"Agustin Chirichigno", "chirichignoa@gmail.com", resourceId));

        // Mock repository's save method to assign an id
        Mockito.when(reservationRepository.findReservationByResourceId(Mockito.anyString()))
                .thenReturn(reservationList);

        Response response = this.reservationService.deleteReservation(resourceId);
        assertThat(response.getContent()).isEqualTo(null);
        assertThat(response.getError()).isEqualTo(null);
    }

    @Test
    public void shouldNotDeleteReservationResourceIdDoesNotExist() {
        String resourceId = UUID.randomUUID().toString();
        Response correctResponse = new Response("Reservation does not exist", null, HttpStatus.NOT_FOUND);

        // Mock repository's save method to assign an id
        Mockito.when(reservationRepository.findReservationByResourceId(Mockito.anyString()))
                .thenReturn(new ArrayList<>());

        Response response = this.reservationService.deleteReservation(resourceId);
        assertThat(response.getContent()).isEqualTo(null);
        assertThat(response).isEqualTo(correctResponse);
    }

    @Test
    public void shouldNotDeleteReservationInternalServerError() {
        String resourceId = UUID.randomUUID().toString();
        // Mock repository's save method to assign an id
        Mockito.when(reservationRepository.findReservationByResourceId(Mockito.anyString()))
                .thenThrow(new RuntimeException("Some weird exception"));
        try {
            Response response = this.reservationService.deleteReservation(resourceId);
        } catch(RuntimeException e) {
            assertThat(e.getMessage()).isEqualTo("Some weird exception");
        }
    }
}
