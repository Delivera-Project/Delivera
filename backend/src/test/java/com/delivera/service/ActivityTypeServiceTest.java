package com.delivera.service;

import com.delivera.model.ActivityType;
import com.delivera.repository.ActivityTypeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ActivityTypeServiceTest {

    @Mock
    private ActivityTypeRepository activityTypeRepository;
    @InjectMocks
    private ActivityTypeService activityTypeService;

    @Test
    void getAll_returnsSortedList() {
        ActivityType a = new ActivityType();
        ActivityType b = new ActivityType();
        when(activityTypeRepository.findAllByOrderBySortOrderAsc()).thenReturn(List.of(a, b));
        assertThat(activityTypeService.getAll()).hasSize(2);
    }

}
