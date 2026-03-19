package com.delivera.dto.unit;

import com.delivera.model.OperationalUnit;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class UnitResponse {

    private UUID id;
    private String name;
    private String type;
    private String address;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private Instant createdAt;

    public UnitResponse(OperationalUnit unit) {
        this.id = unit.getId();
        this.name = unit.getName();
        this.type = unit.getType().name();
        this.address = unit.getAddress();
        this.latitude = unit.getLatitude();
        this.longitude = unit.getLongitude();
        this.createdAt = unit.getCreatedAt();
    }
}
