package edu.java.bot.configuration;

import edu.java.bot.exception.ApiErrorResponse;
import java.util.Arrays;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.Errors;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RequiredArgsConstructor
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final String VALIDATION_FAILURE_CODE = "VALIDATION_FAILURE";

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiErrorResponse handleValidationException(MethodArgumentNotValidException ex) {
        return new ApiErrorResponse(
            getResponseDescription(HttpStatus.BAD_REQUEST),
            VALIDATION_FAILURE_CODE,
            ex.getClass().getSimpleName(),
            fieldsValidationMessage(ex.getBindingResult()),
            Arrays.stream(ex.getStackTrace())
                .map(StackTraceElement::toString)
                .toList()
        );
    }

    private String getResponseDescription(HttpStatus status) {
        return String.format("%s %s", status.value(), status.name());
    }

    private String fieldsValidationMessage(Errors errors) {
        return errors.getFieldErrors()
            .stream()
            .map(error -> error.getField() + ": " + error.getDefaultMessage())
            .collect(Collectors.joining("; "));
    }
}
