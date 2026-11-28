
package pl.gg.crm.service;

import pl.gg.crm.dto.EmployeeRequestDTO;
import pl.gg.crm.dto.EmployeeResponseDTO;
import pl.gg.crm.entity.Employee;
import pl.gg.crm.entity.User;
import pl.gg.crm.enums.EmployeeRole;
import pl.gg.crm.repository.EmployeeRepository;
import pl.gg.crm.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;

    public EmployeeService(
            EmployeeRepository employeeRepository,
            UserRepository userRepository
    ) {
        this.employeeRepository = employeeRepository;
        this.userRepository = userRepository;
    }

    // Convert Employee entity to EmployeeResponseDTO
    private EmployeeResponseDTO toDTO(Employee employee) {
        return new EmployeeResponseDTO(
                employee.getId(),
                employee.getUser().getId(),
                employee.getFirstName(),
                employee.getLastName(),
                employee.getPhoneNumber(),
                employee.getHireDate(),
                employee.getPosition()
        );
    }

    // Get all employees
    public List<EmployeeResponseDTO> getAllEmployees() {
        return employeeRepository.findAll()
                .stream()
                .map(this::toDTO)
                .toList();
    }

    // Get employee by ID
    public EmployeeResponseDTO getEmployeeById(Integer id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        return toDTO(employee);
    }

    // Find employees by first name
    public List<EmployeeResponseDTO> getEmployeesByFirstName(String firstName) {
        return employeeRepository.findByFirstName(firstName)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    // Find employees by last name
    public List<EmployeeResponseDTO> getEmployeesByLastName(String lastName) {
        return employeeRepository.findByLastName(lastName)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    // Search employees by first name
    public List<EmployeeResponseDTO> searchByFirstName(String firstName) {
        return employeeRepository.findByFirstNameContainingIgnoreCase(firstName)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    // Search employees by last name
    public List<EmployeeResponseDTO> searchByLastName(String lastName) {
        return employeeRepository.findByLastNameContainingIgnoreCase(lastName)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    // Find employees by role
    public List<EmployeeResponseDTO> getEmployeesByRole(EmployeeRole position) {
        return employeeRepository.findDistinctByPosition(position)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    // Check if employees with a given role exist
    public boolean existsByRole(EmployeeRole position) {
        return employeeRepository.existsByPosition(position);
    }

    // Create employee
    public EmployeeResponseDTO createEmployee(EmployeeRequestDTO dto) {

        User user = userRepository.findById(dto.userId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Employee employee = new Employee();

        employee.setUser(user);
        employee.setFirstName(dto.firstName());
        employee.setLastName(dto.lastName());
        employee.setPhoneNumber(dto.phoneNumber());
        employee.setHireDate(dto.hireDate());
        employee.setPosition(dto.position());

        Employee savedEmployee = employeeRepository.save(employee);

        return toDTO(savedEmployee);
    }

    // Update employee
    public EmployeeResponseDTO updateEmployee(Integer id, EmployeeRequestDTO dto) {

        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        employee.setFirstName(dto.firstName());
        employee.setLastName(dto.lastName());
        employee.setPhoneNumber(dto.phoneNumber());
        employee.setHireDate(dto.hireDate());
        employee.setPosition(dto.position());

        Employee updatedEmployee = employeeRepository.save(employee);

        return toDTO(updatedEmployee);
    }

    // Delete employee
    public void deleteEmployee(Integer id) {
        if (!employeeRepository.existsById(id)) {
            throw new RuntimeException("Employee not found");
        }

        employeeRepository.deleteById(id);
    }
}
