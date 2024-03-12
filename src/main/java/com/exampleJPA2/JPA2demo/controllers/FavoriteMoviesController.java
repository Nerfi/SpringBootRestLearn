package com.exampleJPA2.JPA2demo.controllers;

import com.exampleJPA2.JPA2demo.exceptions.MovieAlreadyExists;
import com.exampleJPA2.JPA2demo.exceptions.ResourceNotFoundException;
import com.exampleJPA2.JPA2demo.models.FavoriteMovie;
import com.exampleJPA2.JPA2demo.models.Movie;
import com.exampleJPA2.JPA2demo.models.User;
import com.exampleJPA2.JPA2demo.repository.FavoriteMoviesRepository;
import com.exampleJPA2.JPA2demo.repository.MovieRepository;
import com.exampleJPA2.JPA2demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.security.Principal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("favorite-movies")
public class FavoriteMoviesController {
    // need to create the repos I guess
    @Autowired
    FavoriteMoviesRepository favMovieRepository;
    // test
    @Autowired
    UserRepository userRepository;
    @Autowired
    MovieRepository movieRepository;

    @GetMapping("/all/{userId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<Movie>> findAllFavoriteMovies(@PathVariable Long userId) {
        Optional<User> userOptional = userRepository.findById(userId);

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            List<FavoriteMovie> favoriteMovies = user.getFavoriteMovies();

            // Mapear las películas favoritas a una lista de objetos Movie
            List<Movie> movies = favoriteMovies.stream()
                    .map(FavoriteMovie::getMovie)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(movies);
        } else {
            return ResponseEntity.notFound().build();
        }
    }


    @PostMapping("/{userId}/{movieId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> addFavoriteMovie(
            @PathVariable Long userId,
            @PathVariable Long movieId,
            UriComponentsBuilder ucb

    ) {
        Optional<User> userOptional = userRepository.findById(userId);
        Optional<Movie> movieOptional = movieRepository.findById(movieId);

        if (userOptional.isPresent() && movieOptional.isPresent()) {
            User user = userOptional.get();
            Movie movie = movieOptional.get();

            if (user.getFavoriteMovies().stream().anyMatch(singleMovie -> singleMovie.getMovie().getMovie_id().equals(movieId))) {
                throw new MovieAlreadyExists("Error: Movie already added");
            }

            FavoriteMovie favoriteMovie = new FavoriteMovie();
            favoriteMovie.setUser(user);
            favoriteMovie.setMovie(movie);
            favoriteMovie.setOwner(user.getUsername());

            favMovieRepository.save(favoriteMovie);

            //create URI to send to front-end in response
            URI favoriteMovieLocation = ucb
                    .path("/favorite-movies/{id}")
                    .buildAndExpand(favoriteMovie.getId())
                    .toUri();

            return ResponseEntity.created(favoriteMovieLocation).build();

        } else {
            return ResponseEntity.notFound().build();
        }
    }


    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR')")
    public ResponseEntity<Void> deleteFavoriteMovie(@PathVariable Long id, Principal principal) {
        //solo podemos eliminar las peliculas que el usuario ha agregado para si mimso
        String prin = principal.getName();

        Optional<FavoriteMovie> movieToDelete = favMovieRepository.findById(id);
        //check that the username and owner are the same in order to be able to delete this
        if (!movieToDelete.get().getOwner().equals(prin)) {

            throw new ResourceNotFoundException("Can not delete favorite movie! " + id);
        }

        favMovieRepository.deleteById(id);
        return ResponseEntity.noContent().build();


    }
}
