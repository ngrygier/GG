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

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "offers", uniqueConstraints = {
        @UniqueConstraint(name = "offers_offer_number_key", columnNames = {"offer_number"})
})
public class Offer {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "offers_id_gen")
    @SequenceGenerator(name = "offers_id_gen", sequenceName = "offers_offer_id_seq", allocationSize = 1)
    @Column(name = "offer_id", nullable = false)
    private Integer id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @NotNull
    @Column(name = "first_contact_date", nullable = false)
    private LocalDate firstContactDate;

    @NotNull
    @Column(name = "order_description", nullable = false, length = Integer.MAX_VALUE)
    private String orderDescription;

    @NotNull
    @Column(name = "submission_date", nullable = false)
    private LocalDate submissionDate;

    @Column(name = "deadline")
    private LocalDate deadline;

    @NotNull
    @ColumnDefault("false")
    @Column(name = "accepted", nullable = false)
    private Boolean accepted = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.RESTRICT)
    @JoinColumn(name = "created_by")
    private Employee createdBy;

    @Size(max = 50)
    @Column(name = "offer_number", length = 50)
    private String offerNumber;

    @Column(name = "valid_until")
    private LocalDate validUntil;

    @Column(name = "total_amount", precision = 12, scale = 2)
    private BigDecimal totalAmount;

}
