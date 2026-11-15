package pl.gg.crm.security.access;

/**
 * Port do bazy danych dla kontroli dostępu do konkretnych rekordów - jedyne miejsce, które
 * {@link ResourceAccessService} potrzebuje od warstwy persystencji.
 *
 * <p>TODO(DB): zaimplementować na podstawie encji/repozytoriów (np. jako {@code @Component} z zapytaniami
 * {@code exists...}) i usunąć {@link NotImplementedResourceAssignmentLookup}.
 *
 * <p>{@code employeeId} to id pracownika z claimu {@code sub} JWT. Każda metoda odpowiada wyłącznie na pytanie
 * o przypisanie - uprawnienia i "widok wszystkiego" sprawdza już {@link ResourceAccessService}.
 * W razie wątpliwości (brak rekordu, brak przypisania) zwracać {@code false}.
 */
public interface ResourceAssignmentLookup {

    /** Czy pracownik jest przypisany do projektu. TODO(DB): relacja pracownik-projekt nie jest jeszcze opisana. */
    boolean isAssignedToProject(Long employeeId, Long projectId);

    /** Czy zadanie jest przypisane do pracownika (zadanie ma dokładnie jednego pracownika, np. {@code existsByIdAndAssigneeId}). */
    boolean isTaskAssignedTo(Long employeeId, Long taskId);

    /** Czy klient należy do projektu, do którego przypisany jest pracownik (projekt ma jednego klienta). */
    boolean isClientInAssignedProject(Long employeeId, Long clientId);

    /** Czy projektant jest powiązany z projektem, do którego przypisany jest pracownik. TODO(DB): relacja projektant-projekt do ustalenia. */
    boolean isDesignerInAssignedProject(Long employeeId, Long designerId);
}
