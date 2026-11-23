package pl.gg.crm.employee;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.gg.crm.entity.Employee;

public interface EmployeeRepository extends JpaRepository<Employee, Integer> {
}
