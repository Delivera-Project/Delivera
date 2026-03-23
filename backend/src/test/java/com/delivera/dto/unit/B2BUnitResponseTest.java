package com.delivera.dto.unit;

import com.delivera.model.Company;
import com.delivera.model.OperationalUnit;
import com.delivera.model.UnitType;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class B2BUnitResponseTest {

    @Test
    void from_mapsAllFields() {
        UUID unitId = UUID.randomUUID();
        UUID companyId = UUID.randomUUID();

        Company company = new Company();
        company.setId(companyId);
        company.setName("External Co");

        OperationalUnit unit = new OperationalUnit();
        unit.setId(unitId);
        unit.setName("Warehouse B");
        unit.setType(UnitType.WAREHOUSE);
        unit.setCompany(company);

        B2BUnitResponse response = B2BUnitResponse.from(unit);

        assertThat(response.id()).isEqualTo(unitId);
        assertThat(response.name()).isEqualTo("Warehouse B");
        assertThat(response.type()).isEqualTo("WAREHOUSE");
        assertThat(response.companyId()).isEqualTo(companyId);
        assertThat(response.companyName()).isEqualTo("External Co");
    }
}
