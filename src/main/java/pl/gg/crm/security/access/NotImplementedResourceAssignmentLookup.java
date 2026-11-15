package pl.gg.crm.security.access;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * TYMCZASOWA, fail-closed implementacja {@link ResourceAssignmentLookup}: nigdy nie potwierdza przypisania.
 *
 * <p>Skutek: dopóki nie ma integracji z bazą, dostęp do pojedynczych rekordów mają wyłącznie użytkownicy
 * z uprawnieniem "widok wszystkiego" (np. {@code TASK_VIEW_ALL}).
 *
 * <p>TODO(DB): usunąć tę klasę po dodaniu implementacji opartej o repozytoria JPA.
 * Pozostawienie obu implementacji zakończy się błędem startu (dwa beany tego samego typu) - celowo.
 */
@Slf4j
@Component
public class NotImplementedResourceAssignmentLookup implements ResourceAssignmentLookup {

    @Override
    public boolean isAssignedToProject(Long employeeId, Long projectId) {
        return deny("project", projectId);
    }

    @Override
    public boolean isTaskAssignedTo(Long employeeId, Long taskId) {
        return deny("task", taskId);
    }

    @Override
    public boolean isClientInAssignedProject(Long employeeId, Long clientId) {
        return deny("client", clientId);
    }

    @Override
    public boolean isDesignerInAssignedProject(Long employeeId, Long designerId) {
        return deny("designer", designerId);
    }

    private static boolean deny(String resource, Long id) {
        log.warn("Record-level access check for {} {} denied - ResourceAssignmentLookup is not implemented yet", resource, id);
        return false;
    }
}
