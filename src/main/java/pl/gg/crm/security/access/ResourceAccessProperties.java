package pl.gg.crm.security.access;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Nazwy uprawnień, które zwalniają z ograniczenia "tylko rekordy przypisane do mnie" ({@code crm.security.access.*}).
 *
 * <p>Dzięki temu OWNER (pełny dostęp) czy OFFICE_WORKER (wszyscy klienci) nie są zahardkodowani -
 * wystarczy w bazie przypisać im odpowiednie uprawnienie. Pusta wartość = nikt nie ma globalnego widoku (fail-closed).
 *
 * <p>TODO(DB): {@code TASK_VIEW_ALL} jest na wstępnej liście uprawnień. {@code PROJECT_VIEW_ALL}, {@code CLIENT_VIEW_ALL}
 * i {@code DESIGNER_VIEW_ALL} to PROPOZYCJE - do potwierdzenia i dodania do bazy (albo zmiany nazw tutaj).
 */
@ConfigurationProperties("crm.security.access")
public record ResourceAccessProperties(
        @DefaultValue("PROJECT_VIEW_ALL") String projectViewAllAuthority,
        @DefaultValue("TASK_VIEW_ALL") String taskViewAllAuthority,
        @DefaultValue("CLIENT_VIEW_ALL") String clientViewAllAuthority,
        @DefaultValue("DESIGNER_VIEW_ALL") String designerViewAllAuthority
) {
}
