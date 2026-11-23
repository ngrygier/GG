package pl.gg.crm.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "projects")
public class Project {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "projects_id_gen")
    @SequenceGenerator(name = "projects_id_gen", sequenceName = "projects_project_id_seq", allocationSize = 1)
    @Column(name = "project_id", nullable = false)
    private Integer id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "offer_id", nullable = false)
    private Offer offer;

    @Size(max = 150)
    @NotNull
    @Column(name = "project_name", nullable = false, length = 150)
    private String projectName;

    @Column(name = "order_description", length = Integer.MAX_VALUE)
    private String orderDescription;

    @Size(max = 100)
    @Column(name = "client_source", length = 100)
    private String clientSource;

    @NotNull
    @Enumerated(EnumType.STRING)
    @ColumnDefault("'PENDING'")
    @Column(name = "status", nullable = false, length = 30)
    private ProjectStatus status = ProjectStatus.PENDING;

    @Column(name = "deadline")
    private LocalDate deadline;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ProjectEmployee> assignments = new LinkedHashSet<>();

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Size(max = 100)
    @Column(name = "installation_street", length = 100)
    private String installationStreet;

    @Size(max = 10)
    @Column(name = "installation_building_number", length = 10)
    private String installationBuildingNumber;

    @Size(max = 10)
    @Column(name = "installation_apartment_number", length = 10)
    private String installationApartmentNumber;

    @Size(max = 10)
    @Column(name = "installation_postal_code", length = 10)
    private String installationPostalCode;

    @Size(max = 100)
    @Column(name = "installation_city", length = 100)
    private String installationCity;

}
