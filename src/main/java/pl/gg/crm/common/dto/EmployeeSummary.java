package pl.gg.crm.common.dto;

import pl.gg.crm.entity.Employee;

public record EmployeeSummary(
        Integer id,
        String firstName,
        String lastName,
        String phoneNumber
) {

    public static EmployeeSummary from(Employee employee) {
        return new EmployeeSummary(
                employee.getId(),
                employee.getFirstName(),
                employee.getLastName(),
                employee.getPhoneNumber());
    }
}
