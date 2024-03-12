package com.exampleJPA2.JPA2demo.repository;

import com.exampleJPA2.JPA2demo.models.FavoriteMovie;
import org.springframework.data.jpa.repository.JpaRepository;



public interface FavoriteMoviesRepository extends JpaRepository<FavoriteMovie, Long> {
    boolean findByIdAndOwner(Long id, String owner);

}
