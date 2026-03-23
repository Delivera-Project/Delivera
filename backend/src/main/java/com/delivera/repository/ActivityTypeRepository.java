package com.delivera.repository;

import com.delivera.model.ActivityType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ActivityTypeRepository extends JpaRepository<ActivityType, String> {
    List<ActivityType> findAllByOrderBySortOrderAsc();
}
