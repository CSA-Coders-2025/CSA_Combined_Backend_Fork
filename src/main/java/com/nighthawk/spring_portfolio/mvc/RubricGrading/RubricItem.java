package com.nighthawk.spring_portfolio.mvc.RubricGrading;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RubricItem {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String topic;
    private Double weightage;
    private Double points;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rubric_grading_id")
    @JsonBackReference
    private RubricGrading rubricgrade;
}
