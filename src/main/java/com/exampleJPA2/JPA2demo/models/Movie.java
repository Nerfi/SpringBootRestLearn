package com.exampleJPA2.JPA2demo.models;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

// lombok
import jakarta.validation.constraints.*;

import lombok.*;

import java.util.ArrayList;
import java.util.List;


@Getter
@Setter
@EqualsAndHashCode
@ToString(exclude = "user")
//https://stackoverflow.com/questions/68314072/why-to-use-allargsconstructor-and-noargsconstructor-together-over-an-entity
@AllArgsConstructor
@NoArgsConstructor
/*

La anotación @AllArgsConstructor en Lombok es una anotación que se utiliza para generar automáticamente un constructor que incluye todos los campos de la clase.
En otras palabras, Lombok generará un constructor que acepta todos los campos de la clase como parámetros y los inicializa.
 */

@Entity
//@Entity annotation defines that a class can be mapped to a table
@Table(name = "movies")

//PD:  @NotEmpty anotacion solo debe de ser usada en STRINGS, no en numbers

public class Movie {
    //@Id: This annotation specifies the primary key of the entity.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // IDNETITY es por mysql
    private Long movie_id;
    @NotEmpty(message =  "Title debe estar presente")
    @Size(min = 2, max = 120)
    private String title;
    //maybe this could be an array of authors
    @NotEmpty(message = "Author debe de estar presente")
    @Size(min = 2, max = 120)
    private String author;
    @NotEmpty(message = "Country debe de estar presente")
    @Size(min = 2, max = 120)
    private String country;

    @NotNull(message = "Rating debe estar presente ")
    @Min(0) @Max(value = 5, message = "El valor no puede ser mayor que 5")
    private int rating;

    //@NotEmpty
    @NonNull
    private String owner;

    //https://stackoverflow.com/questions/65930344/springboot-onetomany-infinite-loop-with-lombok
    @JsonIgnore // new addition bidireccional error lombok
    @ManyToOne
    @JoinColumn(name = "user_id")
    //Especifica el nombre de la columna que actúa como clave foránea en la tabla movies
    private User user;

    //reviews
    @OneToMany(mappedBy = "movie", cascade = CascadeType.ALL)
    @JsonIgnoreProperties("movie")
   // we have added final because of this: https://stackoverflow.com/questions/23761242/java-lombok-omitting-one-field-in-allargsconstructor
    private final List<Review> reviews = new ArrayList<>();

    //necesario si marcamos la clase como @Entity y si añadimos @NoArgsConstructor

//    public Movie() {
//
//    }

    // THIS CONSTURCTOR WAS ADDED IN ORDER TO PASS THE TEST IN TEST FOLDER JUST THAT
    public Movie(String title, String author, String country, int rating) {
        this.title = title;
        this.author = author;
        this.country = country;
        this.rating = rating;
    }


    // hemos añadido este metodo ya que JPA parece parece que solo entiende el nombre del campo cuando se llama ID(en minisculas) a secas
    // al haberlo llamado movie_id por las convenciones que tiene no puedo usar metodos del repositorio que sean de buscar mediante el ID, que es el
    // nombre del field in the class
    public Long getId(){
        return  this.movie_id;
    }

    //custom method to add review

    public void addReview(Review review) {
        reviews.add(review);
        review.setMovie(this);
    }

}
