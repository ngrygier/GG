
package pl.gg.crm.enums;

import java.util.Set;

public enum EmployeeRole {

    OWNER(Set.of(
            Permission.values()
    )),

    OFFICE_WORKER(Set.of(
            Permission.EMPLOYEE_READ,
            Permission.CLIENT_READ,
            Permission.CLIENT_CREATE,
            Permission.CLIENT_UPDATE,
            Permission.CLIENT_DELETE,
            Permission.PROJECT_READ,
            Permission.PROJECT_CREATE,
            Permission.PROJECT_UPDATE,
            Permission.TASK_READ,
            Permission.OFFER_READ,
            Permission.OFFER_CREATE,
            Permission.OFFER_UPDATE,
            Permission.CONTRACT_READ
    )),

    PRODUCTION_WORKER(Set.of(
            Permission.PROJECT_READ,
            Permission.TASK_READ,
            Permission.TASK_UPDATE
    ));

    private final Set<Permission> permissions;

    EmployeeRole(Set<Permission> permissions) {
        this.permissions = permissions;
    }

    public Set<Permission> getPermissions() {
        return permissions;
    }
}
