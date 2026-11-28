package pl.gg.crm.service;

import pl.gg.crm.entity.Employee;
import pl.gg.crm.enums.EmployeeRole;
import pl.gg.crm.repository.EmployeeRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

    @Service
    public class EmployeeService {

        private final EmployeeRepository employeeRepository;

        public EmployeeService(EmployeeRepository employeeRepository) {
            this.employeeRepository = employeeRepository;
        }

        public List<Employee> getAllEmployees() {
            return employeeRepository.findAll();
        }

        public Optional<Employee> getEmployeeById(Integer id) {
            return employeeRepository.findById(id);
        }

        public List<Employee> getEmployeesByFirstName(String firstName) {
            return employeeRepository.findByFirstName(firstName);
        }

        public List<Employee> getEmployeesByLastName(String lastName) {
            return employeeRepository.findByLastName(lastName);
        }

        public List<Employee> searchByFirstName(String firstName) {
            return employeeRepository.findByFirstNameContainingIgnoreCase(firstName);
        }

        public List<Employee> searchByLastName(String lastName) {
            return employeeRepository.findByLastNameContainingIgnoreCase(lastName);
        }

        public List<Employee> getEmployeesByRole(EmployeeRole position) {
            return employeeRepository.findDistinctByPosition(position);
        }

        public boolean existsByRole(EmployeeRole position) {
            return employeeRepository.existsByPosition(position);
        }

        public Employee createEmployee(Employee employee) {
            return employeeRepository.save(employee);
        }


        public Employee updateEmployee(Integer id, Employee employee) {
            Employee existingEmployee = employeeRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Employee not found"));

            existingEmployee.setFirstName(employee.getFirstName());
            existingEmployee.setLastName(employee.getLastName());
            existingEmployee.setPhoneNumber(employee.getPhoneNumber());
            existingEmployee.setHireDate(employee.getHireDate());
            existingEmployee.setPosition(employee.getPosition());

            return employeeRepository.save(existingEmployee);
        }


        public void deleteEmployee(Integer id) {
            if (!employeeRepository.existsById(id)) {
                throw new RuntimeException("Employee not found");
            }

            employeeRepository.deleteById(id);
        }
    }


