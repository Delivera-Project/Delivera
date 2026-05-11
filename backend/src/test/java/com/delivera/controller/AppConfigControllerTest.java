package com.delivera.controller;

import com.delivera.dto.config.AppConfigResponse;
import com.delivera.service.AppConfigService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppConfigControllerTest {

    @Mock private AppConfigService appConfigService;
    @InjectMocks private AppConfigController controller;

    @Test
    void getConfig_returns200WithConfig() {
        AppConfigResponse cfg = new AppConfigResponse(List.of(), List.of(), List.of(), 5242880L);
        when(appConfigService.getConfig()).thenReturn(cfg);
        var resp = controller.getConfig();
        assertThat(resp.getStatusCode().value()).isEqualTo(200);
        assertThat(resp.getBody()).isSameAs(cfg);
    }
}
