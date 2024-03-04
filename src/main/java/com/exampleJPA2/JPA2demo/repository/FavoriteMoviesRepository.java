package com.exampleJPA2.JPA2demo.repository;

import com.exampleJPA2.JPA2demo.models.FavoriteMovie;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FavoriteMoviesRepository extends JpaRepository<FavoriteMovie, Long> {

}
