package pl.gg.crm.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.math.BigDecimal;
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
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.RESTRICT)
    @JoinColumn(name = "created_by", nullable = false)
    private Employee createdBy;

    @Size(max = 50)
    @NotNull
    @Column(name = "offer_number", nullable = false, length = 50)
    private String offerNumber;

    @NotNull
    @Column(name = "offer_date", nullable = false)
    private LocalDate offerDate;

    @NotNull
    @Column(name = "valid_until", nullable = false)
    private LocalDate validUntil;

    @NotNull
    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Size(max = 30)
    @NotNull
    @Column(name = "status", nullable = false, length = 30)
    private String status;

    @Column(name = "description", length = Integer.MAX_VALUE)
    private String description;

}