package com.nighthawk.spring_portfolio.mvc.synergy.SynergyRubricGrade;

import java.util.Map;

import com.nighthawk.spring_portfolio.mvc.assignments.Assignment;
import com.nighthawk.spring_portfolio.mvc.person.Person;

import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name = "synergy_rubric_grade_request")
public class SynergyRubricGradeAPIController {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotNull
    private Short status; // 0 = pending, 1 = accepted, 2 = rejected
    
    private String explanation;

    @NotNull
    @ElementCollection(fetch = FetchType.EAGER)
    private Map<String, SynergyRubricGrade.RubricCriterion> rubricScores;

    @NotNull
    @ManyToOne
    private Person grader;
    
    @NotNull
    @ManyToOne
    private Person student;

    @NotNull
    @ManyToOne
    private Assignment assignment;

    public SynergyRubricGradeAPIController(Assignment assignment, Person student, Person grader, String explanation, Map<String, SynergyRubricGrade.RubricCriterion> rubricScores) {
        this.rubricScores = rubricScores;
        this.explanation = explanation;
        this.grader = grader;
        this.student = student;
        this.assignment = assignment;
        this.status = 0;
    }

    public void accept() {
        this.status = 1;
    }

    public void reject() {
        this.status = 2;
    }

    public boolean isAccepted() {
        return this.status == 1;
    }
}