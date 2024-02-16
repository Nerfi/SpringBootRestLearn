package com.exampleJPA2.JPA2demo.exceptions;


import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;


import java.util.*;

@ControllerAdvice
public class GlobalExceptionHanlder {
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorMessageException> resourceNotFoundException(ResourceNotFoundException ex, WebRequest request){

        ErrorMessageException message = new ErrorMessageException(
                HttpStatus.NOT_FOUND.value(),
                new Date(),
                ex.getMessage(),
                request.getDescription(false) // set this to true after ust to check
        );

        return new ResponseEntity<ErrorMessageException>(message, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(MovieAlreadyExists.class)
    public ResponseEntity<ErrorMessageException> movieAlreadyExists(MovieAlreadyExists ex, WebRequest request){
        ErrorMessageException message = new ErrorMessageException(
                HttpStatus.CONFLICT.value(),
                new Date(),
                ex.getMessage(),
                request.getDescription(false)
        );

        return new ResponseEntity<ErrorMessageException>(message, HttpStatus.CONFLICT);
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorMessageException> globalExceptionHandler(Exception ex, WebRequest request) {
        ErrorMessageException message = new ErrorMessageException(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                new Date(),
                ex.getMessage(),
                request.getDescription(false)
        );

        return new ResponseEntity<ErrorMessageException>(message, HttpStatus.INTERNAL_SERVER_ERROR);
    }



    // validation in order to throw error in fields annotated with @notnull @max etc..

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> notValid(MethodArgumentNotValidException ex, HttpServletRequest request ){
        List<String> errors = new ArrayList<>();
        ex.getAllErrors().forEach(err -> errors.add(err.getDefaultMessage()));
        Map<String, List<String>> result = new HashMap<>();
        result.put("errors", errors);
        return  new ResponseEntity<>(result, HttpStatus.BAD_REQUEST);
    }
}