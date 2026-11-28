package pl.gg.crm.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "roles", uniqueConstraints = {
        @UniqueConstraint(name = "roles_role_name_key", columnNames = {"role_name"})
})
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "roles_id_gen")
    @SequenceGenerator(
            name = "roles_id_gen",
            sequenceName = "roles_role_id_seq",
            allocationSize = 1
    )
    @Column(name = "role_id", nullable = false)
    private Integer id;

    @Size(max = 50)
    @NotNull
    @Column(name = "role_name", nullable = false, length = 50)
    private String roleName;



    @OneToMany(mappedBy = "role")
    private Set<RolePermission> rolePermissions = new HashSet<>();

}