package com.nighthawk.spring_portfolio.mvc.synergy.SynergyRubricGrade;

import com.nighthawk.spring_portfolio.mvc.assignments.Assignment;
import com.nighthawk.spring_portfolio.mvc.person.Person;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "synergy_rubric_grade_request")
public class SynergyRubricGradeRequest extends SynergyRubricGradeAPIController {
    
    @NotNull
    @ElementCollection(fetch = FetchType.EAGER)
    private Map<String, SynergyRubricGrade.RubricCriterion> rubricScores;

    public SynergyRubricGradeRequest(Assignment assignment, Person student, Person grader, String explanation, Map<String, SynergyRubricGrade.RubricCriterion> rubricScores) {
        super(assignment, student, grader, explanation, null);
        this.rubricScores = rubricScores;
    }
    
    public void accept() {
        this.setStatus((short) 1);
    }

    public void reject() {
        this.setStatus((short) 2);
    }

    public boolean isAccepted() {
        return this.getStatus() == 1;
    }
}
