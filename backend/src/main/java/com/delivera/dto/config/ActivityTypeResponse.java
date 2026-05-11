package com.delivera.dto.config;

import com.delivera.model.ActivityType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ActivityTypeResponse {

    private String code;
    private String labelEs;
    private String labelEn;
    private short sortOrder;

    public static ActivityTypeResponse from(ActivityType at) {
        ActivityTypeResponse dto = new ActivityTypeResponse();
        dto.code = at.getCode();
        dto.labelEs = at.getLabelEs();
        dto.labelEn = at.getLabelEn();
        dto.sortOrder = at.getSortOrder();
        return dto;
    }
}
