package pl.gg.crm.common.error;

import org.springframework.http.HttpStatus;

public class ConflictException extends ApiException {

    public ConflictException(String message) {
        this(message, null);
    }

    public ConflictException(String message, Object details) {
        super(HttpStatus.CONFLICT, message, null, details);
    }
}
