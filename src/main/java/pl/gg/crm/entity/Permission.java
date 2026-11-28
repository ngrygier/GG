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
@Table(name = "permissions", uniqueConstraints = {
        @UniqueConstraint(name = "permissions_permission_name_key", columnNames = {"permission_name"})
})
public class Permission {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "permissions_id_gen")
    @SequenceGenerator(name = "permissions_id_gen", sequenceName = "permissions_permission_id_seq", allocationSize = 1)
    @Column(name = "permission_id", nullable = false)
    private Integer id;

    @Size(max = 100)
    @NotNull
    @Column(name = "permission_name", nullable = false, length = 100)
    private String permissionName;

    @OneToMany(mappedBy = "permission")
    private Set<RolePermission> rolePermissions = new HashSet<>();

}