package pl.gg.crm.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.Hibernate;

import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
@Embeddable
public class ProjectEmployeeId implements Serializable {
    private static final long serialVersionUID = 8094426644762209588L;
    @NotNull
    @Column(name = "project_id", nullable = false)
    private Integer projectId;

    @NotNull
    @Column(name = "employee_id", nullable = false)
    private Integer employeeId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        ProjectEmployeeId entity = (ProjectEmployeeId) o;
        return Objects.equals(this.employeeId, entity.employeeId) &&
                Objects.equals(this.projectId, entity.projectId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(employeeId, projectId);
    }

}