package com.exampleJPA2.JPA2demo.exceptions;

public class MovieAlreadyExists extends RuntimeException{
    private String message;

    public MovieAlreadyExists(String message){
        super(message);
        this.message = message;
    }

    public MovieAlreadyExists(){}
}
