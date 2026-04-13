package com.delivera.dto.activity;

import java.util.UUID;

public record UnitRankingEntry(UUID unitId, String unitName, String unitType, long orderCount) {}
