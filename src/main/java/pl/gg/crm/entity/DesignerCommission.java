package pl.gg.crm.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "designer_commissions", uniqueConstraints = {
        @UniqueConstraint(name = "uq_designer_client", columnNames = {"designer_id", "client_id"})
})
public class DesignerCommission {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "designer_commissions_id_gen")
    @SequenceGenerator(name = "designer_commissions_id_gen", sequenceName = "designer_commissions_commission_id_seq", allocationSize = 1)
    @Column(name = "commission_id", nullable = false)
    private Integer id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "designer_id", nullable = false)
    private pl.gg.crm.entity.Designer designer;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @NotNull
    @Column(name = "commission_amout", nullable = false, precision = 12, scale = 2)
    private BigDecimal commissionAmout;

    @Size(max = 30)
    @NotNull
    @Column(name = "status", nullable = false, length = 30)
    private String status;

    @NotNull
    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

}