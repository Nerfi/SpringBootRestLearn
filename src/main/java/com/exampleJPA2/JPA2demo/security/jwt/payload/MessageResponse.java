package com.exampleJPA2.JPA2demo.security.jwt.payload;

public class MessageResponse {
    private String message;

    public MessageResponse(String message){
        this.message = message;
    }

    public String getMessage(){
        return this.message;
    }

    public  void setMessage(String message) {
        this.message = message;
    }
}