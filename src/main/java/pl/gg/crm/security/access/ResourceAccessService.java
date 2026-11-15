package pl.gg.crm.security.access;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;
import pl.gg.crm.security.user.CurrentUserIdResolver;

import java.util.function.BiPredicate;

/**
 * Kontrola dostępu do konkretnych rekordów (uzupełnienie {@code hasAuthority(...)}).
 *
 * <p>Reguła dla każdego zasobu: dostęp jest przyznany, gdy użytkownik jest uwierzytelniony ORAZ
 * (ma uprawnienie "widok wszystkiego" dla danego zasobu LUB {@link ResourceAssignmentLookup} potwierdzi przypisanie).
 * Wszystko inne - brak uwierzytelnienia, brak id, brak identyfikatora użytkownika, brak implementacji - daje odmowę.
 *
 * <p>Ta klasa NIE sprawdza bazowego uprawnienia (np. {@code TASK_VIEW}) - łącz oba warunki:
 * <pre>
 * &#64;PreAuthorize("hasAuthority('TASK_VIEW') and &#64;resourceAccess.canAccessTask(authentication, #taskId)")
 * </pre>
 * W serwisach domenowych można wstrzyknąć tę klasę i wywołać metody bezpośrednio.
 */
@Component("resourceAccess")
@EnableConfigurationProperties(ResourceAccessProperties.class)
@RequiredArgsConstructor
public class ResourceAccessService {

    private final ResourceAssignmentLookup assignmentLookup;
    private final CurrentUserIdResolver userIdResolver;
    private final ResourceAccessProperties properties;

    /** OWNER / OFFICE_WORKER przez uprawnienie globalne, PROJECT_WORKER - tylko przypisane projekty. */
    public boolean canAccessProject(Authentication authentication, Long projectId) {
        return check(authentication, projectId, properties.projectViewAllAuthority(), assignmentLookup::isAssignedToProject);
    }

    /** OWNER przez {@code TASK_VIEW_ALL}, pozostali - tylko zadania przypisane do siebie. */
    public boolean canAccessTask(Authentication authentication, Long taskId) {
        return check(authentication, taskId, properties.taskViewAllAuthority(), assignmentLookup::isTaskAssignedTo);
    }

    /** PROJECT_WORKER widzi klienta tylko przez projekt, do którego jest przypisany. */
    public boolean canAccessClient(Authentication authentication, Long clientId) {
        return check(authentication, clientId, properties.clientViewAllAuthority(), assignmentLookup::isClientInAssignedProject);
    }

    /** PROJECT_WORKER widzi projektanta tylko przez projekt, do którego jest przypisany. */
    public boolean canAccessDesigner(Authentication authentication, Long designerId) {
        return check(authentication, designerId, properties.designerViewAllAuthority(), assignmentLookup::isDesignerInAssignedProject);
    }

    private boolean check(Authentication authentication,
                          Long resourceId,
                          String viewAllAuthority,
                          BiPredicate<Long, Long> assignmentCheck) {
        if (resourceId == null || authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        if (hasAuthority(authentication, viewAllAuthority)) {
            return true;
        }
        return userIdResolver.resolveUserId(authentication)
                .map(employeeId -> assignmentCheck.test(employeeId, resourceId))
                .orElse(false);
    }

    private static boolean hasAuthority(Authentication authentication, String authority) {
        if (authority == null || authority.isBlank()) {
            return false;
        }
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(authority::equals);
    }
}
