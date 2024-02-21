package com.exampleJPA2.JPA2demo.repository;

import com.exampleJPA2.JPA2demo.models.Movie;

import org.springframework.data.jpa.repository.JpaRepository;

/*
cuando usamos JpaRepository no hace falta que extendamos esta interfaz a PaginationAndSorting
ya que por defecto se extiende, todo lo contrario que si usaramos CrudRepository
CrudRepository<CashCard, Long>, PagingAndSortingRepository<CashCard, Long>
 */


public interface MovieRepository extends JpaRepository<Movie, Long> {

    boolean  existsByTitle(String title);
    //metodo para hacer update

    Movie findByIdAndOwner(Long id, String owner);

    boolean existsByIdAndOwner(Long id, String owner);
}
