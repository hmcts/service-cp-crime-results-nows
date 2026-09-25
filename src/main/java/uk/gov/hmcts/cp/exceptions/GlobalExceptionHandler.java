package uk.gov.hmcts.cp.exceptions;

import io.micrometer.tracing.Tracer;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import uk.gov.hmcts.cp.openapi.model.ErrorResponse;
import uk.gov.hmcts.cp.services.ClockService;

import java.util.Map;

@Slf4j
@AllArgsConstructor
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final String NOT_FOUND = "NOT_FOUND";

    private final Tracer tracer;
    private final ClockService clockService;

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> handleResponseStatusException(final ResponseStatusException e) {
        final String message = e.getReason() != null ? e.getReason() : e.getMessage();
        if (e.getStatusCode().is4xxClientError()) {
            log.warn("GlobalExceptionHandler handleResponseStatusException: {}", message);
        } else {
            log.error("GlobalExceptionHandler handleResponseStatusException", e);
        }
        return ResponseEntity.status(e.getStatusCode())
                .body(buildErrorResponse(message, e.getStatusCode().toString(), null));
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleEntityNotFound(final EntityNotFoundException e) {
        log.warn("GlobalExceptionHandler handleEntityNotFound: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(buildErrorResponse(e.getMessage(), NOT_FOUND, null));
    }

    @ExceptionHandler(IncompleteHearingDetailsException.class)
    public ResponseEntity<ErrorResponse> handleIncompleteHearingDetails(final IncompleteHearingDetailsException e) {
        log.warn("GlobalExceptionHandler handleIncompleteHearingDetails: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(buildErrorResponse(e.getMessage(), "SERVICE_UNAVAILABLE", null));
    }

    @ExceptionHandler(HttpServerErrorException.class)
    public ResponseEntity<ErrorResponse> handleServerException(final HttpServerErrorException e) {
        log.error("GlobalExceptionHandler handleServerException", e);
        return ResponseEntity.status(e.getStatusCode())
                .body(buildErrorResponse(e.getMessage(), e.getStatusCode().toString(), null));
    }

    @ExceptionHandler(HttpClientErrorException.class)
    public ResponseEntity<ErrorResponse> handleClientException(final HttpClientErrorException e) {
        log.warn("GlobalExceptionHandler handleClientException: {}", e.getMessage());
        return ResponseEntity.status(e.getStatusCode())
                .body(buildErrorResponse(e.getMessage(), e.getStatusCode().toString(), null));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatch(final MethodArgumentTypeMismatchException e) {
        final String requiredType = e.getRequiredType() != null ? e.getRequiredType().getSimpleName() : "value";
        final String message = String.format("The supplied %s is not a valid %s", e.getName(), requiredType);
        log.warn("GlobalExceptionHandler handleMethodArgumentTypeMismatch: {}", message);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(buildErrorResponse(message, "BAD_REQUEST", Map.of("parameter", e.getName())));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResourceFound(final NoResourceFoundException e) {
        log.warn("GlobalExceptionHandler handleNoResourceFound: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(buildErrorResponse(e.getMessage(), NOT_FOUND, null));
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoHandlerFound(final NoHandlerFoundException e) {
        log.warn("GlobalExceptionHandler handleNoHandlerFound: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(buildErrorResponse(e.getMessage(), NOT_FOUND, null));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(final Exception e) {
        log.error("GlobalExceptionHandler handleException", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(buildErrorResponse(e.getMessage(), "INTERNAL_SERVER_ERROR", null));
    }

    private ErrorResponse buildErrorResponse(final String message, final String errorCode,
                                              final Map<String, Object> details) {
        return ErrorResponse.builder()
                .error(errorCode)
                .message(message)
                .details(details)
                .timestamp(clockService.now())
                .traceId(tracer.currentSpan() == null ? null : tracer.currentSpan().context().traceId())
                .build();
    }
}