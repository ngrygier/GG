
package pl.gg.crm.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import pl.gg.crm.enums.EmployeeRole;

import java.time.LocalDate;

public record EmployeeRequestDTO(

        @NotNull
        Integer userId,

        @NotBlank
        @Size(max = 50)
        String firstName,

        @NotBlank
        @Size(max = 50)
        String lastName,

        @NotBlank
        @Size(max = 20)
        String phoneNumber,

        @NotNull
        LocalDate hireDate,

        @NotNull
        EmployeeRole position

) {}
