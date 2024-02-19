package com.exampleJPA2.JPA2demo.controllers;

import jakarta.validation.Valid;
import com.exampleJPA2.JPA2demo.exceptions.MovieAlreadyExists;
import com.exampleJPA2.JPA2demo.exceptions.ResourceNotFoundException;
import com.exampleJPA2.JPA2demo.models.Movie;
import com.exampleJPA2.JPA2demo.models.User;
import com.exampleJPA2.JPA2demo.repository.MovieRepository;
import com.exampleJPA2.JPA2demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;


import java.net.URI;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("movies")

public class MoviesController {

    @Autowired
    MovieRepository movieRepository;
    @Autowired
    UserRepository userRepository;


    @GetMapping("/all")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<Movie>> findAll() {
        // de momento no tenemos nada aqui
        List<Movie> movies = movieRepository.findAll();
        if (movies.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(movies);
    }


    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Movie> getMovieById(@PathVariable Long id){


        Movie singleMovie = movieRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Not found Movie with id = " + id));

        return ResponseEntity.ok(singleMovie);
    }

    @PostMapping("/add")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR')")
    public ResponseEntity<?> createMovie(  @Valid @RequestBody Movie movie, Principal principal, UriComponentsBuilder ucb) {

        try {
            Optional<User> optionalUser = userRepository.findByUsername(principal.getName());

            //check if we already have a movie with such title
            if(movieRepository.existsByTitle(movie.getTitle())) {

                throw  new MovieAlreadyExists("Error: movie already exits");
            }

            if(optionalUser.isPresent()) {
                User user = optionalUser.get(); // obtenemos el usuario
                // creamos una nueva movie
                // in case of error check the null in movie id
                Movie movietoSave = new Movie(null,movie.getTitle(), movie.getAuthor(), movie.getCountry(), movie.getRating(),user.getUsername(), user);
                //obtenemos el nombre del usuario que esta creando esta resource y lo añadimos a la movie
                movietoSave.setOwner(user.getUsername());

                //guardamos
                movieRepository.save(movietoSave);

                // Actualizar la lista de películas del usuario
                user.getMoviesList().add(movietoSave);
                userRepository.save(user);

                URI newMovieLocation = ucb
                        .path("/movies/{id}")
                        .buildAndExpand(movietoSave.getMovie_id())
                        .toUri();

                return  ResponseEntity.created(newMovieLocation).build();

            }

            return new  ResponseEntity<Error>(HttpStatus.CONFLICT);

        } catch (MovieAlreadyExists ex) {
            //return  ResponseEntity.badRequest().body(ex.getMessage());
             throw  new MovieAlreadyExists("Error: movie already exits");
        }


    }

    //update
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR')")
    public ResponseEntity<Void> updateMovie(@PathVariable Long id, @RequestBody Movie movie, Principal principal) {
        Movie movieToUpdated = movieRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Movie not found with id " + id));

        if(movieToUpdated.getOwner().equals(principal.getName())) {
            //actualizamos con los setters de la clase
            movieToUpdated.setTitle(movie.getTitle());
            movieToUpdated.setAuthor(movie.getAuthor());
            movieToUpdated.setCountry(movie.getCountry());
            movieToUpdated.setRating(movie.getRating());

            movieRepository.save(movieToUpdated);

            // si todo va bien devolvemos un not content como status http
            return ResponseEntity.noContent().build();

        }


        return ResponseEntity.notFound().build();
    }


    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR')")
    public ResponseEntity<Void> deleteMovie(@PathVariable Long id, Principal principal) {
        // further reading to create GOOD API REST https://www.vinaysahni.com/best-practices-for-a-pragmatic-restful-api
        //añadimos principal solo para saber si este es nuestra movie y poder eliminarla


        if(!movieRepository.existsByIdAndOwner(id, principal.getName())) {
            throw  new ResourceNotFoundException("Movie not found with id " + id);
        }
        movieRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }



}
