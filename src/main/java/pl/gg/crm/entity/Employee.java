package pl.gg.crm.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import pl.gg.crm.enums.EmployeeRole;

import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "employees", uniqueConstraints = {
        @UniqueConstraint(name = "employees_user_id_key", columnNames = {"user_id"})
})
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "employees_id_gen")
    @SequenceGenerator(name = "employees_id_gen", sequenceName = "employees_employee_id_seq", allocationSize = 1)
    @Column(name = "employee_id", nullable = false)
    private Integer id;

    @NotNull
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.RESTRICT)
    @JoinColumn(name = "user_id", nullable = false)
    private pl.gg.crm.entity.User user;

    @Size(max = 50)
    @NotNull
    @Column(name = "first_name", nullable = false, length = 50)
    private String firstName;

    @Size(max = 50)
    @NotNull
    @Column(name = "last_name", nullable = false, length = 50)
    private String lastName;

    @Size(max = 20)
    @NotNull
    @Column(name = "phone_number", nullable = false, length = 20)
    private String phoneNumber;

    @NotNull
    @Column(name = "hire_date", nullable = false)
    private LocalDate hireDate;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "position", nullable = false)
    private EmployeeRole position;


}