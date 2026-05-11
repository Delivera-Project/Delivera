package com.delivera.controller;

import com.delivera.dto.config.ActivityTypeResponse;
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

    @Mock private ActivityTypeService activityTypeService;
    @InjectMocks private ActivityTypeController controller;

    @Test
    void getAll_mapsAndReturnsAll() {
        ActivityType at = new ActivityType();
        at.setCode("LOGISTICS");
        at.setLabelEs("Logística");
        at.setLabelEn("Logistics");
        when(activityTypeService.getAll()).thenReturn(List.of(at));

        List<ActivityTypeResponse> result = controller.getAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCode()).isEqualTo("LOGISTICS");
        assertThat(result.get(0).getLabelEs()).isEqualTo("Logística");
    }

    @Test
    void getAll_emptyListReturnsEmpty() {
        when(activityTypeService.getAll()).thenReturn(List.of());
        assertThat(controller.getAll()).isEmpty();
    }
}
