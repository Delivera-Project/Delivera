package com.delivera.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "activity_types")
public class ActivityType {

    @Id
    @Column(length = 50)
    private String code;

    @Column(name = "label_es", nullable = false, length = 100)
    private String labelEs;

    @Column(name = "label_en", nullable = false, length = 100)
    private String labelEn;

    @Column(name = "sort_order", nullable = false)
    private short sortOrder;
}
