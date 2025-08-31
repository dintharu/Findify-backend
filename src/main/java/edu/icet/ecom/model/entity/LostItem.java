package edu.icet.ecom.model.entity;

import edu.icet.ecom.enums.ItemStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "lost_items")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LostItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String description;
    private String location;
    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    private Long reportedByUserId;
    private LocalDateTime lostDate;
    private LocalDateTime reportedDate;
    @Enumerated(EnumType.STRING)
    private ItemStatus status;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String imageUrl;
}
