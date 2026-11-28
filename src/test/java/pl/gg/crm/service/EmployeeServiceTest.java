package pl.gg.crm.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.gg.crm.dto.EmployeeRequestDTO;
import pl.gg.crm.dto.EmployeeResponseDTO;
import pl.gg.crm.entity.Employee;
import pl.gg.crm.entity.User;
import pl.gg.crm.enums.EmployeeRole;
import pl.gg.crm.repository.EmployeeRepository;
import pl.gg.crm.repository.UserRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private UserRepository userRepository;

    private EmployeeService employeeService;

    @BeforeEach
    void setUp() {
        employeeService = new EmployeeService(employeeRepository, userRepository);
    }

    @Test
    void createsEmployeeForUserWithoutEmployeeProfile() {
        User user = user(7);
        EmployeeRequestDTO request = request(7);
        when(userRepository.findById(7)).thenReturn(Optional.of(user));
        when(employeeRepository.existsByUser_Id(7)).thenReturn(false);
        when(employeeRepository.save(any(Employee.class))).thenAnswer(invocation -> {
            Employee employee = invocation.getArgument(0);
            employee.setId(11);
            return employee;
        });

        EmployeeResponseDTO result = employeeService.createEmployee(request);

        assertThat(result.id()).isEqualTo(11);
        assertThat(result.userId()).isEqualTo(7);
        assertThat(result.firstName()).isEqualTo("Ada");
        assertThat(result.position()).isEqualTo(EmployeeRole.OFFICE_WORKER);
        ArgumentCaptor<Employee> employee = ArgumentCaptor.forClass(Employee.class);
        verify(employeeRepository).save(employee.capture());
        assertThat(employee.getValue().getUser()).isSameAs(user);
    }

    @Test
    void rejectsEmployeeCreationWhenUserDoesNotExist() {
        when(userRepository.findById(7)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> employeeService.createEmployee(request(7)))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("User not found");
        verify(employeeRepository, never()).save(any());
    }

    @Test
    void rejectsEmployeeCreationWhenUserAlreadyHasEmployeeProfile() {
        when(userRepository.findById(7)).thenReturn(Optional.of(user(7)));
        when(employeeRepository.existsByUser_Id(7)).thenReturn(true);

        assertThatIllegalStateException()
                .isThrownBy(() -> employeeService.createEmployee(request(7)))
                .withMessage("Employee already exists for this user");
        verify(employeeRepository, never()).save(any());
    }

    @Test
    void updatesEmployeeWithoutReplacingUser() {
        User user = user(7);
        Employee employee = employee(11, user);
        EmployeeRequestDTO request = new EmployeeRequestDTO(99, "Grace", "Hopper", "222", LocalDate.of(2020, 2, 2), EmployeeRole.OWNER);
        when(employeeRepository.findById(11)).thenReturn(Optional.of(employee));
        when(employeeRepository.save(employee)).thenReturn(employee);

        EmployeeResponseDTO result = employeeService.updateEmployee(11, request);

        assertThat(result.userId()).isEqualTo(7);
        assertThat(result.firstName()).isEqualTo("Grace");
        assertThat(result.position()).isEqualTo(EmployeeRole.OWNER);
    }

    @Test
    void mapsEmployeeListsToResponseDtos() {
        when(employeeRepository.findAll()).thenReturn(List.of(employee(11, user(7))));

        assertThat(employeeService.getAllEmployees())
                .extracting(EmployeeResponseDTO::id, EmployeeResponseDTO::userId)
                .containsExactly(org.assertj.core.groups.Tuple.tuple(11, 7));
    }

    @Test
    void deletesExistingEmployee() {
        when(employeeRepository.existsById(11)).thenReturn(true);

        employeeService.deleteEmployee(11);

        verify(employeeRepository).deleteById(11);
    }

    private static EmployeeRequestDTO request(Integer userId) {
        return new EmployeeRequestDTO(userId, "Ada", "Lovelace", "111", LocalDate.of(2020, 1, 1), EmployeeRole.OFFICE_WORKER);
    }

    private static User user(Integer id) {
        User user = new User();
        user.setId(id);
        return user;
    }

    private static Employee employee(Integer id, User user) {
        Employee employee = new Employee();
        employee.setId(id);
        employee.setUser(user);
        employee.setFirstName("Ada");
        employee.setLastName("Lovelace");
        employee.setPhoneNumber("111");
        employee.setHireDate(LocalDate.of(2020, 1, 1));
        employee.setPosition(EmployeeRole.OFFICE_WORKER);
        return employee;
    }
}
