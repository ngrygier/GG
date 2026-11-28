
package pl.gg.crm.repository;

import pl.gg.crm.entity.Employee;
import pl.gg.crm.enums.EmployeeRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmployeeRepository
        extends JpaRepository<Employee, Integer>,
        JpaSpecificationExecutor<Employee> {

    // Find by first name
    List<Employee> findByFirstName(String firstName);

    // Find by last name
    List<Employee> findByLastName(String lastName);

    // Search by first name
    List<Employee> findByFirstNameContainingIgnoreCase(String firstName);

    // Search by last name
    List<Employee> findByLastNameContainingIgnoreCase(String lastName);

    // Find employees by role
    List<Employee> findDistinctByPosition(EmployeeRole position);

    // Check if employees with a given role exist
    boolean existsByPosition(EmployeeRole position);
}
