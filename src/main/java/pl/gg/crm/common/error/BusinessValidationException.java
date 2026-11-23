package pl.gg.crm.common.error;

import org.springframework.http.HttpStatus;

import java.util.List;

public class BusinessValidationException extends ApiException {

    public BusinessValidationException(List<ApiError.FieldError> fieldErrors) {
        super(HttpStatus.BAD_REQUEST, "Dane formularza są niepoprawne", fieldErrors, null);
    }

    public static BusinessValidationException ofField(String field, String message) {
        return new BusinessValidationException(List.of(new ApiError.FieldError(field, message)));
    }
}
