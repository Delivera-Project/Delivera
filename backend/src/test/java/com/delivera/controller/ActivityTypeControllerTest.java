package com.delivera.controller;

import com.delivera.model.ActivityType;
import com.delivera.service.ActivityTypeService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ActivityTypeControllerTest {

    @Mock
    private ActivityTypeService activityTypeService;

    @InjectMocks
    private ActivityTypeController activityTypeController;

    @Test
    void getAll_returnsMappedDtos() {
        ActivityType type = new ActivityType();
        type.setCode("RETAIL");
        type.setLabelEs("Comercio");
        type.setLabelEn("Retail");
        type.setSortOrder((short) 1);

        when(activityTypeService.getAll()).thenReturn(List.of(type));

        var result = activityTypeController.getAll();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCode()).isEqualTo("RETAIL");
    }
}
