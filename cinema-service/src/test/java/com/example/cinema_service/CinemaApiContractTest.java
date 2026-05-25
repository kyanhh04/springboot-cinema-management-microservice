package com.example.cinema_service;

import com.example.cinema_service.controller.CinemaController;
import com.example.cinema_service.controller.MovieController;
import com.example.cinema_service.controller.ScreenController;
import com.example.cinema_service.controller.ShowtimeController;
import com.example.cinema_service.dto.CinemaDTO;
import com.example.cinema_service.dto.MovieDTO;
import com.example.cinema_service.dto.ScreenDTO;
import com.example.cinema_service.dto.SeatDTO;
import com.example.cinema_service.dto.ShowtimeDTO;
import com.example.cinema_service.entity.Cinema;
import com.example.cinema_service.entity.Movie;
import com.example.cinema_service.entity.Screen;
import com.example.cinema_service.entity.Seat;
import com.example.cinema_service.entity.Showtime;
import com.example.cinema_service.service.CinemaService;
import com.example.cinema_service.service.MovieService;
import com.example.cinema_service.service.ScreenService;
import com.example.cinema_service.service.ShowtimeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class CinemaApiContractTest {

    private MockMvc mockMvc;

    @Mock
    private CinemaService cinemaService;
    @Mock
    private MovieService movieService;
    @Mock
    private ScreenService screenService;
    @Mock
    private ShowtimeService showtimeService;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(
                new CinemaController(cinemaService),
                new MovieController(movieService),
                new ScreenController(screenService),
                new ShowtimeController(showtimeService)
        ).build();
    }

    @Test
    void cinemaEndpointsRespectContract() throws Exception {
        CinemaDTO cinema = CinemaDTO.builder().id(1L).name("CGV").city("HCM").build();
        when(cinemaService.getAllCinemas()).thenReturn(List.of(cinema));
        when(cinemaService.getCinemaById(1L)).thenReturn(cinema);
        when(cinemaService.createCinema(any())).thenReturn(cinema);
        when(cinemaService.updateCinema(eq(1L), any())).thenReturn(cinema);
        doNothing().when(cinemaService).deleteCinema(1L);
        when(cinemaService.getScreensByCinema(1L)).thenReturn(List.of(ScreenDTO.builder().id(2L).cinemaId(1L).build()));

        mockMvc.perform(get("/api/cinemas")).andExpect(status().isOk()).andExpect(jsonPath("$[0].name").value("CGV"));
        mockMvc.perform(get("/api/cinemas/1")).andExpect(status().isOk()).andExpect(jsonPath("$.id").value(1));
        mockMvc.perform(post("/api/cinemas").contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"CGV\",\"city\":\"HCM\"}"))
                .andExpect(status().isCreated());
        mockMvc.perform(put("/api/cinemas/1").contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"CGV\",\"city\":\"HCM\"}"))
                .andExpect(status().isOk());
        mockMvc.perform(delete("/api/cinemas/1")).andExpect(status().isNoContent());
        mockMvc.perform(get("/api/cinemas/1/screens")).andExpect(status().isOk()).andExpect(jsonPath("$[0].id").value(2));
    }

    @Test
    void movieEndpointsRespectContract() throws Exception {
        MovieDTO movie = MovieDTO.builder().id(1L).title("Avengers").status(Movie.MovieStatus.NOW_SHOWING).build();
        when(movieService.getAllMovies()).thenReturn(List.of(movie));
        when(movieService.getMovieById(1L)).thenReturn(movie);
        when(movieService.getNowShowingMovies()).thenReturn(List.of(movie));
        when(movieService.getComingSoonMovies()).thenReturn(List.of(movie));
        when(movieService.createMovie(any())).thenReturn(movie);
        when(movieService.updateMovie(eq(1L), any())).thenReturn(movie);
        doNothing().when(movieService).deleteMovie(1L);

        mockMvc.perform(get("/api/movies")).andExpect(status().isOk()).andExpect(jsonPath("$[0].title").value("Avengers"));
        mockMvc.perform(get("/api/movies/1")).andExpect(status().isOk()).andExpect(jsonPath("$.id").value(1));
        mockMvc.perform(get("/api/movies/now-showing")).andExpect(status().isOk());
        mockMvc.perform(get("/api/movies/coming-soon")).andExpect(status().isOk());
        mockMvc.perform(post("/api/movies").contentType(MediaType.APPLICATION_JSON).content("{\"title\":\"Avengers\"}"))
                .andExpect(status().isCreated());
        mockMvc.perform(put("/api/movies/1").contentType(MediaType.APPLICATION_JSON).content("{\"title\":\"Avengers\"}"))
                .andExpect(status().isOk());
        mockMvc.perform(delete("/api/movies/1")).andExpect(status().isNoContent());
    }

    @Test
    void screenAndSeatEndpointsRespectContract() throws Exception {
        ScreenDTO screen = ScreenDTO.builder().id(1L).cinemaId(1L).screenNumber("A").totalSeats(100).build();
        SeatDTO seat = SeatDTO.builder().id(5L).screenId(1L).seatRow("A").seatNumber(1).seatLabel("A1")
                .seatType(Seat.SeatType.REGULAR).status(Seat.SeatStatus.AVAILABLE).build();
        when(screenService.getScreenById(1L)).thenReturn(screen);
        when(screenService.createScreen(any())).thenReturn(screen);
        when(screenService.updateScreen(eq(1L), any())).thenReturn(screen);
        doNothing().when(screenService).deleteScreen(1L);
        when(screenService.getSeatsByScreen(1L)).thenReturn(List.of(seat));
        when(screenService.getSeatById(5L)).thenReturn(seat);
        when(screenService.generateSeatsForScreen(1L, 5, 10)).thenReturn(List.of(seat));

        mockMvc.perform(get("/api/screens/1")).andExpect(status().isOk()).andExpect(jsonPath("$.id").value(1));
        mockMvc.perform(post("/api/screens").contentType(MediaType.APPLICATION_JSON).content("{\"screenNumber\":\"A\",\"totalSeats\":100}"))
                .andExpect(status().isCreated());
        mockMvc.perform(put("/api/screens/1").contentType(MediaType.APPLICATION_JSON).content("{\"screenNumber\":\"A\",\"totalSeats\":100}"))
                .andExpect(status().isOk());
        mockMvc.perform(delete("/api/screens/1")).andExpect(status().isNoContent());
        mockMvc.perform(get("/api/screens/1/seats")).andExpect(status().isOk()).andExpect(jsonPath("$[0].seatLabel").value("A1"));
        mockMvc.perform(get("/api/screens/seats/5")).andExpect(status().isOk()).andExpect(jsonPath("$.seatLabel").value("A1"));
        mockMvc.perform(post("/api/screens/1/generate-seats").param("rows", "5").param("seatsPerRow", "10"))
                .andExpect(status().isCreated());
    }

    @Test
    void showtimeEndpointsRespectContract() throws Exception {
        ShowtimeDTO showtime = ShowtimeDTO.builder().id(1L).movieId(1L).cinemaId(1L)
                .showDate(LocalDate.of(2026, 5, 25)).showTime(LocalTime.of(14, 30))
                .basePrice(BigDecimal.valueOf(90000)).availableSeats(50).build();
        when(showtimeService.getAllShowtimes()).thenReturn(List.of(showtime));
        when(showtimeService.getShowtimeById(1L)).thenReturn(showtime);
        when(showtimeService.getShowtimesByMovie(eq(1L), any())).thenReturn(List.of(showtime));
        when(showtimeService.getShowtimesByMovieAndCinema(eq(1L), eq(1L), any())).thenReturn(List.of(showtime));
        when(showtimeService.getShowtimesByDateRange(any(), any())).thenReturn(List.of(showtime));
        when(showtimeService.createShowtime(any())).thenReturn(showtime);
        when(showtimeService.updateShowtime(eq(1L), any())).thenReturn(showtime);
        doNothing().when(showtimeService).deleteShowtime(1L);
        when(showtimeService.reserveSeats(1L, 2)).thenReturn(true);
        doNothing().when(showtimeService).releaseSeats(1L, 2);

        mockMvc.perform(get("/api/showtimes")).andExpect(status().isOk()).andExpect(jsonPath("$[0].id").value(1));
        mockMvc.perform(get("/api/showtimes/1")).andExpect(status().isOk());
        mockMvc.perform(get("/api/showtimes/movie/1").param("date", "2026-05-25")).andExpect(status().isOk());
        mockMvc.perform(get("/api/showtimes/movie/1/cinema/1").param("date", "2026-05-25")).andExpect(status().isOk());
        mockMvc.perform(get("/api/showtimes/date-range").param("startDate", "2026-05-25").param("endDate", "2026-05-26"))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/showtimes").contentType(MediaType.APPLICATION_JSON).content("{\"movie\":{\"id\":1}}"))
                .andExpect(status().isCreated());
        mockMvc.perform(put("/api/showtimes/1").contentType(MediaType.APPLICATION_JSON).content("{\"movie\":{\"id\":1}}"))
                .andExpect(status().isOk());
        mockMvc.perform(delete("/api/showtimes/1")).andExpect(status().isNoContent());
        mockMvc.perform(post("/api/showtimes/1/reserve").param("seats", "2")).andExpect(status().isOk());
        mockMvc.perform(post("/api/showtimes/1/release").param("seats", "2")).andExpect(status().isOk());
    }
}
