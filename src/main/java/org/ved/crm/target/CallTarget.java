package org.ved.crm.target;


import jakarta.persistence.*;
import lombok.*;
import org.ved.crm.common.audit.BaseAuditEntity;
import org.ved.crm.user.User;

@Entity
@Table(
        name = "call_targets",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_call_targets_rep_month_year",
                columnNames = {"rep_id", "month", "year"}
        )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CallTarget extends BaseAuditEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rep_id",nullable = false)
    private User rep;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_by_id",nullable = false)
    private User assignedBy;

    @Column(name = "month", nullable = false)
    private Integer month;

    @Column(name = "year", nullable = false)
    private Integer year;

    @Column(name = "target_visits", nullable = false)
    private Integer targetVisits;

    @Builder.Default
    @Column(name = "actual_visits", nullable = false)
    private Integer actualVisits = 0;
}
