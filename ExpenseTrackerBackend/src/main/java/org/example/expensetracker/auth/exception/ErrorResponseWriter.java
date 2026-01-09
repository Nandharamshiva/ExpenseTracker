package org.example.expensetracker.auth.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.io.IOException;

public final class ErrorResponseWriter {

    private ErrorResponseWriter() {
    }

    public static void write(HttpServletRequest request,
                             HttpServletResponse response,
                             HttpStatus status,
                             String message,
                             ObjectMapper objectMapper) throws IOException {

        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        ApiError body = ApiError.builder()
                .status(status.value())
                .error(status.name())
                .message(message)
                .build();

        objectMapper.writeValue(response.getWriter(), body);
    }
}

