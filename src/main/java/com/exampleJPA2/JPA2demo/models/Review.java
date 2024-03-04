package com.exampleJPA2.JPA2demo.models;
// lombok
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;


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
    @JsonIgnore // esto ha arreglado mi problema de la recursividad al agregar en mi reivews array la pelicula
    @JoinColumn(name = "movie_id")
    //Especifica el nombre de la columna que actúa como clave foránea en la tabla movies
    private Movie movie;

    private  String author;
    @NotEmpty(message = "description can not be empty")
    private String description;
    //adding Date
    @DateTimeFormat(pattern = "dd-MM-yyyy")
    private String date;



}
