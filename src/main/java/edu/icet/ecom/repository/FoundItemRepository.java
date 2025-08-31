package edu.icet.ecom.repository;

import edu.icet.ecom.model.entity.FoundItems;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FoundItemRepository extends JpaRepository<FoundItems,Long> {

    List<FoundItems> findByFoundByUserId(Long userId);
    List<FoundItems> findByNameContainingIgnoreCase(String name);

}
