package org.ved.crm.returns;

import jakarta.persistence.*;
import lombok.*;
import org.ved.crm.chemist.Chemist;
import org.ved.crm.common.audit.BaseAuditEntity;
import org.ved.crm.stockist.Stockist;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "returns")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Return extends BaseAuditEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chemist_id")
    private Chemist chemist;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stockist_id")
    private Stockist stockist;

    @Column(name = "return_number" , nullable = false)
    private String returnNumber;

    @Column(name = "return_date" , nullable = false)
    private LocalDate returnDate;

    @Column(name = "reason", nullable = false, columnDefinition = "TEXT")
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private ReturnStatus status = ReturnStatus.PENDING;

    @OneToMany(mappedBy = "returnDoc" , cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReturnItem> returnItems = new ArrayList<>();
}
