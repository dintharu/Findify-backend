package edu.icet.ecom.repository;

import edu.icet.ecom.model.entity.LostItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LostItemRepository extends JpaRepository<LostItem,Long> {

    List<LostItem> findByReportedByUserId(Long userId);
    List<LostItem> findByNameContainingIgnoreCase(String name);

}
