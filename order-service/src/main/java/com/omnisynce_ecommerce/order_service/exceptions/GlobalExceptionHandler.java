package com.omnisynce_ecommerce.order_service.exceptions;

import com.omnisynce_ecommerce.order_service.dtos.ErrorResponseDTO;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleResourceNotFoundException(ResourceNotFoundException ex, HttpServletRequest request) {
        ErrorResponseDTO error =  new ErrorResponseDTO(LocalDateTime.now(),ex.getMessage(),request.getRequestURI(),HttpStatus.NOT_FOUND.value(), HttpStatus.NOT_FOUND.getReasonPhrase());
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponseDTO> handleBadRequestException(BadRequestException ex, HttpServletRequest request) {
        ErrorResponseDTO error =  new ErrorResponseDTO(LocalDateTime.now(),ex.getMessage(),request.getRequestURI(),HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase());
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ServiceUnavailableException.class)
    public ResponseEntity<ErrorResponseDTO> handleServiceUnavailableException(ServiceUnavailableException ex, HttpServletRequest request) {
        ErrorResponseDTO error =  new ErrorResponseDTO(LocalDateTime.now(),ex.getMessage(),request.getRequestURI(),HttpStatus.SERVICE_UNAVAILABLE.value(), HttpStatus.SERVICE_UNAVAILABLE.getReasonPhrase());
        return new ResponseEntity<>(error, HttpStatus.SERVICE_UNAVAILABLE);
    }

    @ExceptionHandler(StockNotAvailableException.class)
    public ResponseEntity<ErrorResponseDTO> handleStockNotAvailableException(StockNotAvailableException ex, HttpServletRequest request) {
        ErrorResponseDTO error =  new ErrorResponseDTO(LocalDateTime.now(),ex.getMessage(),request.getRequestURI(),HttpStatus.CONFLICT.value(), HttpStatus.CONFLICT.getReasonPhrase());
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(CallNotPermittedException.class)
    public ResponseEntity<ErrorResponseDTO> handleCallNotPermittedException(CallNotPermittedException ex, HttpServletRequest request) {
        ErrorResponseDTO error =  new ErrorResponseDTO(LocalDateTime.now(),ex.getMessage(),request.getRequestURI(),HttpStatus.SERVICE_UNAVAILABLE.value(), HttpStatus.SERVICE_UNAVAILABLE.getReasonPhrase());
        return new ResponseEntity<>(error, HttpStatus.SERVICE_UNAVAILABLE);
    }


}
