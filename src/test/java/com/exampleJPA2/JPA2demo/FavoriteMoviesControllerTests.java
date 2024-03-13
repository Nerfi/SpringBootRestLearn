package com.exampleJPA2.JPA2demo;

import com.exampleJPA2.JPA2demo.controllers.FavoriteMoviesController;
import com.exampleJPA2.JPA2demo.models.FavoriteMovie;
import com.exampleJPA2.JPA2demo.models.Movie;
import com.exampleJPA2.JPA2demo.models.User;
import com.exampleJPA2.JPA2demo.repository.FavoriteMoviesRepository;
import com.exampleJPA2.JPA2demo.repository.MovieRepository;
import com.exampleJPA2.JPA2demo.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(FavoriteMoviesController.class)
//https://docs.spring.io/spring-data/commons/docs/current/api/org/springframework/data/web/config/EnableSpringDataWebSupport.html
@EnableSpringDataWebSupport
public class FavoriteMoviesControllerTests {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private MovieRepository movieRepository;
    @MockBean
    private UserRepository userRepository;
    @MockBean
    FavoriteMoviesRepository favMovieRepository;


    private User userTest;
    private Movie movieTest;
    Long userId;
    Long moviesId;

    Long favoriteMovieId;
    FavoriteMovie favoriteMovie;

    List<FavoriteMovie> favMovies;

    @BeforeEach
    void setUp() {
        userTest = new User();
        movieTest = new Movie("test movie 1", "juan", "España", 3);
                //hemos tenido que setear el id de la movie de forma manuel por que teniamos un error 500
        //message":"Cannot invoke \"java.lang.Long.equals(Object)\" because the return value of \"com.exampleJPA2.JPA2demo.models.Movie.getMovie_id()\" is null"
        movieTest.setMovie_id(23L);
        userId = 23L;
        moviesId = 1L;
        favoriteMovieId = 15L;
        favoriteMovie = new FavoriteMovie();

        favoriteMovie.setOwner("admina");

        // Crear y asociar películas favoritas al usuario
        FavoriteMovie favoriteMovie1 = new FavoriteMovie();
        favoriteMovie1.setUser(userTest);
        favoriteMovie1.setMovie(movieTest);


        FavoriteMovie favoriteMovie2 = new FavoriteMovie();
        favoriteMovie2.setUser(userTest);
        favoriteMovie2.setMovie(movieTest);


        FavoriteMovie favoriteMovie3 = new FavoriteMovie();
        favoriteMovie3.setUser(userTest);
        favoriteMovie3.setMovie(movieTest);


        favMovies = Arrays.asList(favoriteMovie1, favoriteMovie2, favoriteMovie3);
        // con este setter setFavoriteMovies creamos la relacion la relacion entre el usuer y sus favorites movies
        // mirar el metodo findAllFavoriteMovies para entender por que las seteamos
        userTest.setFavoriteMovies(favMovies);
    }

    @Test
    @WithMockUser(username = "user1", password = "pwd", roles = "USER")
    public void canCreateFavoriteMovieWhenLogged() throws Exception {
        //vamos a poder guardar siempre y cuando tengamos usuario y movie que guardar
        given(userRepository.findById(userId)).willReturn(Optional.of(userTest));
        given(movieRepository.findById(moviesId)).willReturn(Optional.of(movieTest));

        //when
        mockMvc.perform(
                        post("/favorite-movies/{userId}/{moviesId}", userId, moviesId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .with(csrf())
                //hemos añadido la linea de arriba(with(csrf)) ya que al tener spring security activado
                //al hacer una peticion post necesitamos un token para que la peticion se lleve acabo
                //al no tener csrf activado , si hacemos un post no llevara token por lo cual no podremos
                //hacer el post

                //tampoco hemos añadido .content() ya que en el metodo de nuestro controlador
                /// no esperamos un @RequestBody

                )
                .andExpect(status().isCreated());

        //verificar que las llamadas han ocurrido solo una vez


        verify(userRepository, times(1)).findById(userId);
        verify(movieRepository, times(1)).findById(moviesId);

        //argument captor
        ArgumentCaptor<FavoriteMovie> argumentCaptor = ArgumentCaptor.forClass(FavoriteMovie.class);
        //hemos usado ArgumentCaptor por el siguiente error:
        //en esta linea   verify(favMovieRepository, times(1)).save(any(FavoriteMovie.class));
        // Inferred type 'S' for type parameter 'S' is not within its bound; should extend 'com.exampleJPA2.JPA2demo.models.FavoriteMovie'
        // ya que aveces mockito tiene problemas para inferir el tipo del objeto en el cual estamos haciendo verify()
        verify(favMovieRepository, times(1)).save(argumentCaptor.capture());

        FavoriteMovie capturedFavoriteMovie = argumentCaptor.getValue();

        assertEquals(userTest, capturedFavoriteMovie.getUser());
        assertEquals(movieTest, capturedFavoriteMovie.getMovie());
        //verify(favMovieRepository, times(1)).save(any(FavoriteMovie.class) );

    }

    @Test
    public void cannotCreateFavoriteMovieIfNotLoggedIn() throws Exception {
        mockMvc.perform(
                post("/favorite-movies/{userId}/{moviesId}", userId, moviesId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
        ).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "user1", password = "pwd", roles = "USER")
    public void canRetrieveFavoritesMoviesOfGivenUser() throws Exception {
        given(userRepository.findById(userId)).willReturn(Optional.of(userTest));


        ResultActions result = mockMvc.perform(
                get("/favorite-movies/all/{userId}", userId)
                        .accept(MediaType.APPLICATION_JSON)

        );


        result.andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.size()").value(favMovies.size()))
                .andDo(print());


    }

    @Test
    @WithMockUser(username = "admina", password = "pwd", roles = "USER")
    public void canDeleteAFavoriteMovieIfIsTheRightUser() throws Exception {
        given(favMovieRepository.findById(favoriteMovieId)).willReturn(Optional.of(favoriteMovie));

        mockMvc.perform(
                delete("/favorite-movies/{favoriteMovieId}", favoriteMovieId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .with(csrf())


        ).andExpect(status().isNoContent());

    }



}
