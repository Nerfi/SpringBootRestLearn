package com.exampleJPA2.JPA2demo.models;
// lombok
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name ="reviews")
public class Review {
    //@Id: This annotation specifies the primary key of the entity.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne
    @JoinColumn(name = "movie_id")
    //Especifica el nombre de la columna que actúa como clave foránea en la tabla movies
    private Movie movie;


    @NotEmpty(message = "we need an author")
    private String author;
    @NotEmpty(message = "description can not be empty")
    private String description;




}
