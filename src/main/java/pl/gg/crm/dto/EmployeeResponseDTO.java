package pl.gg.crm.dto;

import pl.gg.crm.enums.EmployeeRole;

import java.time.LocalDate;

public record EmployeeResponseDTO(
        Integer id,
        Integer userId,
        String firstName,
        String lastName,
        String phoneNumber,
        LocalDate hireDate,
        EmployeeRole position
) {}
