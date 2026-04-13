package com.delivera.dto.activity;

import java.time.LocalDate;

public record OrdersByDayEntry(LocalDate date, long count) {}
