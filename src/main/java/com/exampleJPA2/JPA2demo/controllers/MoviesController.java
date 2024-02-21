package com.exampleJPA2.JPA2demo.controllers;

import com.exampleJPA2.JPA2demo.models.Review;
import jakarta.validation.Valid;
import com.exampleJPA2.JPA2demo.exceptions.MovieAlreadyExists;
import com.exampleJPA2.JPA2demo.exceptions.ResourceNotFoundException;
import com.exampleJPA2.JPA2demo.models.Movie;
import com.exampleJPA2.JPA2demo.models.User;
import com.exampleJPA2.JPA2demo.repository.MovieRepository;
import com.exampleJPA2.JPA2demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;


import java.net.URI;
import java.security.Principal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;

//pagination
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

@RestController
@RequestMapping("movies")

public class MoviesController {

    @Autowired
    MovieRepository movieRepository;
    @Autowired
    UserRepository userRepository;


    @GetMapping("/all")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<Movie>> findAll(
        @PageableDefault(size = 10) Pageable pageable
    ) {

        Page<Movie> movies = movieRepository.findAll(
                PageRequest.of(
                        pageable.getPageNumber(),
                        pageable.getPageSize(),
                        pageable.getSortOr(Sort.by(Sort.Direction.DESC, "title"))
                )
        );

        //List<Movie> movies = movieRepository.findAll();
        if (movies.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

       // return ResponseEntity.ok(movies);
        return ResponseEntity.ok(movies.getContent());
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

    @PostMapping("/add/{movieId}/review")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR')")
    public ResponseEntity<?> addReviewToMovie(@PathVariable Long movieId, @Valid @RequestBody Review review, UriComponentsBuilder ucb, Principal principal) {

        //encontramos la movie si existe, sino lanzamos exception
        Movie singleMovie = movieRepository.findById(movieId)
                .orElseThrow(() -> new ResourceNotFoundException("Not found Movie with id = " + movieId));
        review.setAuthor(principal.getName());
        //adding date of creation
        String dateFormmated = configureDate(new Date());
        review.setDate(dateFormmated);

        singleMovie.addReview(review);
        // Establecer al usuario como author de la review
        singleMovie.setAuthor(principal.getName());

// Guardar la película actualizada en el repositorio
        movieRepository.save(singleMovie);


        //sending back the location of the newly created movie review
        URI newMovieLocation = ucb
                .path("/movies/{id}/review")
                .buildAndExpand(singleMovie.getMovie_id())
                .toUri();

        return ResponseEntity.created(newMovieLocation).build();

    }

    //utility method to format date before saving into review

    public String configureDate(Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        return dateFormat.format(date);
    }



}
