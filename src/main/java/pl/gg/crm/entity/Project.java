package pl.gg.crm.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "projects")
public class Project {
    @Id
    @Column(name = "project_id", nullable = false)
    private Integer id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "offer_id")
    private Offer offer;

    @Size(max = 150)
    @NotNull
    @Column(name = "project_name", nullable = false, length = 150)
    private String projectName;

    @Column(name = "description", length = Integer.MAX_VALUE)
    private String description;

    @Size(max = 30)
    @NotNull
    @Column(name = "status", nullable = false, length = 30)
    private String status;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Size(max = 100)
    @NotNull
    @Column(name = "installation_street", nullable = false, length = 100)
    private String installationStreet;

    @Size(max = 10)
    @NotNull
    @Column(name = "installation_building_number", nullable = false, length = 10)
    private String installationBuildingNumber;

    @Size(max = 10)
    @Column(name = "installation_apartment_number", length = 10)
    private String installationApartmentNumber;

    @Size(max = 10)
    @NotNull
    @Column(name = "installation_postal_code", nullable = false, length = 10)
    private String installationPostalCode;

    @Size(max = 100)
    @NotNull
    @Column(name = "installation_city", nullable = false, length = 100)
    private String installationCity;

}