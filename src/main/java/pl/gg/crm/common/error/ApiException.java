package pl.gg.crm.common.error;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.util.List;

@Getter
public abstract class ApiException extends RuntimeException {

    private final HttpStatus status;
    private final transient List<ApiError.FieldError> fieldErrors;
    private final transient Object details;

    protected ApiException(HttpStatus status, String message, List<ApiError.FieldError> fieldErrors, Object details) {
        super(message);
        this.status = status;
        this.fieldErrors = fieldErrors == null ? List.of() : List.copyOf(fieldErrors);
        this.details = details;
    }
}
