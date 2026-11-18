package pl.gg.crm.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "contracts", uniqueConstraints = {
        @UniqueConstraint(name = "contracts_project_id_key", columnNames = {"project_id"}),
        @UniqueConstraint(name = "contracts_contract_number_key", columnNames = {"contract_number"})
})
public class Contract {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "contracts_id_gen")
    @SequenceGenerator(name = "contracts_id_gen", sequenceName = "contracts_contract_id_seq", allocationSize = 1)
    @Column(name = "contract_id", nullable = false)
    private Integer id;

    @NotNull
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    private pl.gg.crm.entity.Project project;

    @Size(max = 50)
    @NotNull
    @Column(name = "contract_number", nullable = false, length = 50)
    private String contractNumber;

    @NotNull
    @Column(name = "signed_date", nullable = false)
    private LocalDate signedDate;

    @NotNull
    @Column(name = "contract_value", nullable = false, precision = 12, scale = 2)
    private BigDecimal contractValue;

    @Size(max = 30)
    @NotNull
    @Column(name = "status", nullable = false, length = 30)
    private String status;

    @Size(max = 255)
    @Column(name = "document_path")
    private String documentPath;

}