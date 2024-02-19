package com.exampleJPA2.JPA2demo;

import com.exampleJPA2.JPA2demo.controllers.MoviesController;
import com.exampleJPA2.JPA2demo.exceptions.ResourceNotFoundException;
import com.exampleJPA2.JPA2demo.models.Movie;
import com.exampleJPA2.JPA2demo.repository.MovieRepository;
import com.exampleJPA2.JPA2demo.repository.UserRepository;
import com.exampleJPA2.JPA2demo.security.jwt.UserDetailsImpl;

//status lib
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;



import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJsonTesters;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.security.Principal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

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

    //@WithMockUser(username = "user1", password = "pwd", roles = "USER") es para testear este endpoint con Roles ya que tenemos security dep
    // https://stackoverflow.com/questions/15203485/spring-test-security-how-to-mock-authentication
    @Test
    @WithMockUser(username = "user1", password = "pwd", roles = "USER")
    public void canRetrieveByIdWhenExists() throws Exception {

        // given
        given(movieRepository.findById(2L))
                .willReturn(Optional.of(new Movie("Bing", "Juan testing", "España", 3)));

        // when
        MockHttpServletResponse response = mockMvc.perform(
                        get("/movies/2")
                                .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentAsString()).isEqualTo(
                jsonMovie.write(new Movie("Bing", "Juan testing", "España", 3)).getJson()
        );
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
}
