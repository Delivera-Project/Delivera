package com.delivera.controller;

import com.delivera.dto.config.ActivityTypeResponse;
import com.delivera.service.ActivityTypeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/activity-types")
@Tag(name = "Tipos de actividad", description = "Catálogo de tipos de actividad empresarial")
public class ActivityTypeController {

    private final ActivityTypeService activityTypeService;

    public ActivityTypeController(ActivityTypeService activityTypeService) {
        this.activityTypeService = activityTypeService;
    }

    @Operation(summary = "Listar tipos de actividad")
    @GetMapping
    public List<ActivityTypeResponse> getAll() {
        return activityTypeService.getAll().stream().map(ActivityTypeResponse::from).toList();
    }
}
