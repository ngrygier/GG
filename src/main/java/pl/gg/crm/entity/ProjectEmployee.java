package pl.gg.crm.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "project_employees")
public class ProjectEmployee {
    @SequenceGenerator(name = "project_employees_id_gen", sequenceName = "permissions_permission_id_seq", allocationSize = 1)
    @EmbeddedId
    private ProjectEmployeeId id;

    @MapsId("projectId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    private pl.gg.crm.entity.Project project;

    @MapsId("employeeId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

}