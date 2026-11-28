
package pl.gg.crm.repository;

import jakarta.validation.constraints.NotNull;
import pl.gg.crm.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import pl.gg.crm.entity.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository
        extends JpaRepository<Employee, Integer>,
        JpaSpecificationExecutor<Employee> {

    List<Employee> findByFirstName(String firstName);

    List<Employee> findByLastName(String lastName);

    List<Employee> findByFirstNameContainingIgnoreCase(String firstName);

    List<Employee> findByLastNameContainingIgnoreCase(String lastName);

    Optional<Employee> findByUser_UserRoles_Role_RoleName(String roleName);

    List<Employee> findAllByUser_UserRoles_Role_RoleName(String roleName);

}
