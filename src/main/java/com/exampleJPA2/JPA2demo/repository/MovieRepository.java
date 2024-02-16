package com.exampleJPA2.JPA2demo.repository;

import com.exampleJPA2.JPA2demo.models.Movie;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MovieRepository extends JpaRepository<Movie, Long> {

    boolean  existsByTitle(String title);
    //metodo para hacer update

    Movie findByIdAndOwner(Long id, String owner);

    boolean existsByIdAndOwner(Long id, String owner);
}
