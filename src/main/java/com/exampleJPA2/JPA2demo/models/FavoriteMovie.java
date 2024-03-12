package com.exampleJPA2.JPA2demo.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "favoritemovie")
@AllArgsConstructor
@NoArgsConstructor
// need to create table in sql
public class FavoriteMovie {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // IDNETITY es por mysql
    private Long id;

    @ManyToOne
    // not added json ignore in case but maybe we should
    @JoinColumn(name = "movie_id")
    private Movie movie;

    @ManyToOne
    @JoinColumn(name =" user_id")
    private User user;

    //for testing
    //@NotEmpty(message =  "Title debe estar presente")
    private String owner;

}
