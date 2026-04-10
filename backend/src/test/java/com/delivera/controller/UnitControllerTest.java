package com.delivera.controller;

import com.delivera.dto.unit.B2BUnitResponse;
import com.delivera.dto.unit.UnitRequest;
import com.delivera.model.UnitType;
import com.delivera.service.UnitService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UnitControllerTest {

    @Mock
    private UnitService unitService;
    @InjectMocks
    private UnitController unitController;

    @Test
    void listExternal_returns200WithList() {
        B2BUnitResponse unit = new B2BUnitResponse(UUID.randomUUID(), "Ext Warehouse", "WAREHOUSE", UUID.randomUUID(), "External Co");
        when(unitService.getExternalUnits()).thenReturn(List.of(unit));

        var response = unitController.listExternal();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().get(0).name()).isEqualTo("Ext Warehouse");
    }

    @Test
    void list_returns200WithUnitList() {
        when(unitService.getByCompany()).thenReturn(List.of());

        var response = unitController.list();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEmpty();
    }

    @Test
    void create_returns201() {
        UnitRequest req = new UnitRequest("Warehouse A", UnitType.WAREHOUSE, "1 Main St", null, null);
        when(unitService.create(req)).thenReturn(null);

        var response = unitController.create(req);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }
}
