package com.user.global_exception_handler;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

<<<<<<< HEAD
import org.springframework.http.HttpStatus;
=======
>>>>>>> 45fd25982da46d435d6a1f4c455026ae04e43c17
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.user.api_response.ApiResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<?>> handleValidationErrors(MethodArgumentNotValidException ex) {

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> {
            errors.put(error.getField(), error.getDefaultMessage());
        });

        ApiResponse<?> response = ApiResponse.builder()
                .success(false)
                .message("Validation failed")
                .errors(errors)
                .timestamp(LocalDateTime.now())
<<<<<<< HEAD
                .httpStatus(HttpStatus.BAD_REQUEST)
=======
>>>>>>> 45fd25982da46d435d6a1f4c455026ae04e43c17
                .build();

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<?>> handleIllegalArgument(IllegalArgumentException ex) {

        ApiResponse<?> response = ApiResponse.builder()
                .success(false)
                .message(ex.getMessage())
                .errors(null)
                .timestamp(LocalDateTime.now())
<<<<<<< HEAD
                .httpStatus(HttpStatus.BAD_REQUEST)
=======
>>>>>>> 45fd25982da46d435d6a1f4c455026ae04e43c17
                .build();

        return ResponseEntity.badRequest().body(response);
    }
<<<<<<< HEAD
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleGenericException(Exception ex){
    	ApiResponse<?> response = ApiResponse.builder()
                .success(false)
                .message(ex.getMessage())
                .errors(null)
                .timestamp(LocalDateTime.now())
                .httpStatus(HttpStatus.INTERNAL_SERVER_ERROR)
                .build();

        return ResponseEntity.internalServerError().body(response);
    }
=======
>>>>>>> 45fd25982da46d435d6a1f4c455026ae04e43c17
}

