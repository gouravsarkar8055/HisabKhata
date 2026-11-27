package com.user.api_response;

import java.time.LocalDateTime;
import java.util.Map;
<<<<<<< HEAD

import org.springframework.http.HttpStatus;

=======
>>>>>>> 45fd25982da46d435d6a1f4c455026ae04e43c17
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;
    private Map<String, String> errors;
    private LocalDateTime timestamp;
<<<<<<< HEAD
    private HttpStatus httpStatus;
=======
>>>>>>> 45fd25982da46d435d6a1f4c455026ae04e43c17
}
