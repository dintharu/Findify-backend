package edu.icet.ecom.model.entity;

import edu.icet.ecom.enums.ClaimStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.catalina.User;

import java.time.LocalDateTime;

@Entity
@Table(name = "claims")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Claim {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String description;


    @JoinColumn(name = "claimed_by_user_id")
    private Long claimedByUserId;

    @ManyToOne
    @JoinColumn(name = "lost_item_id")
    private LostItem lostItem;

    @ManyToOne
    @JoinColumn(name = "found_item_id")
    private FoundItems foundItem;

    @Enumerated(EnumType.STRING)
    private ClaimStatus status;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
