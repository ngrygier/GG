package pl.gg.crm.common.error;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record ApiError(
        Instant timestamp,
        int status,
        String message,
        List<FieldError> fieldErrors,
        Object details
) {

    public record FieldError(String field, String message) {
    }

    public static ApiError of(int status, String message) {
        return new ApiError(Instant.now(), status, message, List.of(), null);
    }
}
