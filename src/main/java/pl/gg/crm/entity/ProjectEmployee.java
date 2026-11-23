package pl.gg.crm.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Getter
@Setter
@Entity
@Table(name = "project_employees")
public class ProjectEmployee {
    @EmbeddedId
    private ProjectEmployeeId id;

    @MapsId("projectId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @MapsId("employeeId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    public static ProjectEmployee of(Project project, Employee employee) {
        ProjectEmployeeId id = new ProjectEmployeeId();
        id.setProjectId(project.getId());
        id.setEmployeeId(employee.getId());
        ProjectEmployee assignment = new ProjectEmployee();
        assignment.setId(id);
        assignment.setProject(project);
        assignment.setEmployee(employee);
        return assignment;
    }

}
