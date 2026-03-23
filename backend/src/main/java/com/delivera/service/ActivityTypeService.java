package com.delivera.service;

import com.delivera.model.ActivityType;
import com.delivera.repository.ActivityTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ActivityTypeService {

    @Autowired private ActivityTypeRepository activityTypeRepository;

    public List<ActivityType> getAll() {
        return activityTypeRepository.findAllByOrderBySortOrderAsc();
    }
}
