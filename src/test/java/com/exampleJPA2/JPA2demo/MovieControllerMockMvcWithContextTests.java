package com.exampleJPA2.JPA2demo;

import com.exampleJPA2.JPA2demo.controllers.MoviesController;
import com.exampleJPA2.JPA2demo.exceptions.MovieAlreadyExists;
import com.exampleJPA2.JPA2demo.exceptions.ResourceNotFoundException;
import com.exampleJPA2.JPA2demo.models.Movie;
import com.exampleJPA2.JPA2demo.models.User;
import com.exampleJPA2.JPA2demo.repository.MovieRepository;
import com.exampleJPA2.JPA2demo.repository.UserRepository;
import com.exampleJPA2.JPA2demo.security.jwt.UserDetailsImpl;

//status lib
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJsonTesters;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureJsonTesters
@WebMvcTest(MoviesController.class)
//@Import(SecurityConfig.class)/
public class MovieControllerMockMvcWithContextTests {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MovieRepository movieRepository;
    @MockBean
    private UserRepository userRepository;


    @Autowired
    private JacksonTester<Movie> jsonMovie;

    @Autowired
    private ObjectMapper objectMapper;

    //@WithMockUser(username = "user1", password = "pwd", roles = "USER") es para testear este endpoint con Roles ya que tenemos security dep
    // https://stackoverflow.com/questions/15203485/spring-test-security-how-to-mock-authentication
    @Test
    @WithMockUser(username = "user1", password = "pwd", roles = "USER")
    public void canRetrieveByIdWhenExists() throws Exception {

        // given
        given(movieRepository.findById(2L))
                .willReturn(Optional.of(new Movie("Bing", "Juan testing", "Salvador", 3)));

        // when
        MockHttpServletResponse response = mockMvc.perform(
                        get("/movies/2")
                                .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentAsString()).isEqualTo(
                jsonMovie.write(new Movie("Bing", "Juan testing", "Salvador", 3)).getJson()
        );
    }

    @Test
    @WithMockUser(username = "user1", password = "pwd", roles = "USER")
    public void canRetrieveAllMovies() throws Exception {
        //create fake movies
        List<Movie> allMovies = new ArrayList<>();
        Movie movie1 = new Movie("test movie 1", "juan", "España", 3);
        Movie movie2 = new Movie("test movie 2", "antonio", "Ecuador", 3);
        Movie movie3 = new Movie("test movie 3", "jose", "Chile", 4);

        allMovies.add(movie1);
        allMovies.add(movie2);
        allMovies.add(movie3);

        //when
        when(movieRepository.findAll()).thenReturn(allMovies);

      mockMvc.perform(
                        get("/movies/all")
                                .contentType(MediaType.APPLICATION_JSON)
                                .with(csrf())
                )

                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(allMovies.size()))
              .andExpect(jsonPath("$[0].title").value("test movie 1"))
              .andExpect(jsonPath("$[1].title").value("test movie 2"))
              .andExpect(jsonPath("$[2].title").value("test movie 3"))
                .andDo(print());



    }

    @Test
    @WithMockUser(username = "user1", password = "pwd", roles = "USER")
    public void cantRetrieveByIdWhenDoesNotExist() throws Exception{
        //given
        given(movieRepository.findById(2000L))
                .willThrow(new ResourceNotFoundException("There is not such movies"));
        //when

        MockHttpServletResponse response = mockMvc.perform(
                        get("/movies/2000")
                                .accept(MediaType.APPLICATION_JSON)
                )

               .andReturn().getResponse();

        //then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
        //assertThat(response.getContentAsString()).isEmpty(); // eliminamos esto ya que al devolver una custom exception , viene con un body: "{"statusCode":404,"timestamp":"2024-02-16T13:11:20.350+00:00","message":"There is not such movies","description":"uri=/movies/2000"}"
        //al venir con un body la linea de arriba no nos sirve ya que espera que la response.getContentAsString().isEmpty(), es decir que no venga body
    }

    @Test
    @WithMockUser(username = "admina", password = "pwd", roles = "USER")
    public void canDeleteMovie() throws Exception {
        // for testing principal
        //https://stackoverflow.com/questions/45561471/mock-principal-for-spring-rest-controller
        Long idToTest = 30L;

        // Crea un Principal simulado
        Principal mockPrincipal = Mockito.mock(Principal.class);


        Mockito.when(movieRepository.existsByIdAndOwner(idToTest, "admina")).thenReturn(true);



        // Usa el Principal simulado en tu solicitud
        MockHttpServletResponse response = mockMvc.perform(
                        delete("/movies/30")
                                .principal(mockPrincipal)
                                .contentType(MediaType.APPLICATION_JSON)
                                .with(csrf())
                )
                .andReturn().getResponse();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.NO_CONTENT.value());


    }


    @Test
    @WithMockUser(username = "admina", password = "pwd", roles = "USER")
    public void canUpdateMovie() throws  Exception {
        long id = 28L;
        Movie movie =  new Movie("Bing", "Juan testing", "España", 3);

        movie.setOwner("admina");
        Movie updatedMovie = new  Movie("Bing 2", "testing", "Brasil", 3);

        when(movieRepository.findById(id)).thenReturn(Optional.of(movie));
        when(movieRepository.save(any(Movie.class))).thenReturn(updatedMovie);


        mockMvc.perform(
                        put("/movies/28").contentType(MediaType.APPLICATION_JSON)
                                .with(csrf())
                                .content("{\"title\": \"Bing\", \"author\":\"Juanako\", \"country\":\"Brasil\", \"rating\": \"3\", \"owner\": \"admina\"}")
                                .contentType(MediaType.APPLICATION_JSON)


                )
                .andExpect(status().isNoContent())
                .andDo(print());
        //aqui no podemos hacer   .andExpect(jsonPath("$.email", is(email)))
        //ya que nuestro controlador no devuelve el objeto actualizado es decir,  return ResponseEntity.ok(movieUpdated);
        //sino que devolvemos un return ResponseEntity.noContent().build(); por ello no podemos hacer las assertions
        //de jsonpath ya que no hay objeto de vuelta


    }

    @Test
    @WithMockUser(username = "admina", password = "pwd", roles = {"USER", "MODERATOR"})
    public void createNewMovie() throws  Exception{

        // Crea un Principal simulado
        Principal mockPrincipal = Mockito.mock(Principal.class);
        when(mockPrincipal.getName()).thenReturn("admina");
        User userMock = new User("admina", "password", "ROLE_USER");
        when(userRepository.findByUsername("admina")).thenReturn(Optional.of(userMock));


        Movie movietoSave = new Movie(123L,"Bing","juan", "españa", 3, mockPrincipal.getName(),userMock);
        //when(movieRepository.save(movietoSave)).thenReturn(movietoSave);

        when(movieRepository.save(any(Movie.class))).thenAnswer(invocation -> {
            Movie savedMovie = invocation.getArgument(0);
           savedMovie.setMovie_id(123L); // Asigna un ID temporal
            return savedMovie;
        });

        MvcResult result = mockMvc.perform(
                post("/movies/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{\"title\": \"Bing\", \"author\":\"Juanako\", \"country\":\"Brasil\", \"rating\": \"3\"}")
                        .with(csrf())

        )
                .andReturn();
        MockHttpServletResponse response = result.getResponse();
        assertEquals(HttpStatus.CREATED.value(), response.getStatus());
        assertEquals("http://localhost/movies/" + movietoSave.getMovie_id(),
                response.getHeader(HttpHeaders.LOCATION));



    }

    @Test
    @WithMockUser(username = "admina", password = "pwd", roles = {"USER", "MODERATOR"})
    public void shouldNotAllowToCreateMovieIfTitleAlreadyExists() throws Exception{
        //THIS TEST PASSED BUT IM NOT SURE IF THIS IS THE RIGHT IMPLENTATION
        Movie movietoSave = new Movie("Bing","juan", "españa", 3);
        when(movieRepository.existsByTitle(movietoSave.getTitle())).thenThrow(new MovieAlreadyExists("Error: movie already exists"));
//        when(movieRepository.existsByTitle(movietoSave.getTitle())).thenReturn(true);


        MockHttpServletResponse response =  mockMvc.perform(
            post("/movies/add")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"title\": \"Bing\", \"author\":\"Juanako\", \"country\":\"Brasil\", \"rating\": \"3\", \"owner\": \"admina\"}")
                    .with(csrf())
    )
            .andReturn().getResponse();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.CONFLICT.value());


    }


    @Test
    public void shouldRejectCreatingReviewsWhenUserIsAnonymous() throws Exception {
        this.mockMvc
                .perform(
                        post("/movies/add")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"title\": \"Bing\", \"author\":\"Juanako\", \"country\":\"Brasil\", \"rating\": \"3\", \"owner\": \"admina\"}")
                                .with(csrf())
                )
                .andExpect(status().isUnauthorized());
    }

    //add review test
    @Test
    public void shouldNotAllowCreationOfReviewIfThereIsNoUSer() throws Exception {

        long movieId = 10L;
        mockMvc.perform(
                post("/movies/add/{movieId}/review" , movieId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"description\":  \"random description\"}")
                        .with(csrf())
        )
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "admina", password = "pwd", roles = {"USER", "MODERATOR"})
    public void shouldAllowCreationOfReviewWhenThereIsAUser() throws Exception {
        Movie movie = new Movie("Bing", "Juan testing", "España", 3);

        when(movieRepository.findById(anyLong())).thenReturn(Optional.of(movie));
        long movieId = 10L;

        mockMvc.perform(
                        post("/movies/add/{movieId}/review", movieId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"description\":  \"random description\"}")
                                .with(csrf())
                )
                .andExpect(status().isCreated());

        //checking if review size has increased
        Movie movieUpdatedWithReview = movieRepository.findById(movieId).orElseThrow();
        assertThat(movieUpdatedWithReview.getReviews()).hasSize(1);


    }

}
