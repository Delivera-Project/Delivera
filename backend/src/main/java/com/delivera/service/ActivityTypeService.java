package com.delivera.service;

import com.delivera.model.ActivityType;
import com.delivera.repository.ActivityTypeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ActivityTypeService {

    private final ActivityTypeRepository activityTypeRepository;

    public ActivityTypeService(ActivityTypeRepository activityTypeRepository) {
        this.activityTypeRepository = activityTypeRepository;
    }

    @Transactional(readOnly = true)
    public List<ActivityType> getAll() {
        return activityTypeRepository.findAllByOrderBySortOrderAsc();
    }
}
