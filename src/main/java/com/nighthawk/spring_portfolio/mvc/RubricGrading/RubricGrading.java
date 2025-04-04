package com.nighthawk.spring_portfolio.mvc.RubricGrading;

import java.util.List;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Entity
public class RubricGrading {
    @NotNull
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "rubric_grading_id")
    private Long id;

    private Double RubricOverallGrade;

    @OneToMany(mappedBy = "rubricgrade", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RubricItem> rubricItems;

    public double getOverallGrade() {
        return this.RubricOverallGrade != null ? this.RubricOverallGrade : 0.0;
    }

    public void setTopic(String newtopic, int index){
        rubricItems.get(index).setTopic(newtopic);
    }

    public void setGrade(Double newgrade, int index){
        rubricItems.get(index).setPoints(newgrade);
    }

    public void setWeight(Double newweight, int index){
        rubricItems.get(index).setWeightage(newweight);
    }

    public void setOverallGrade(double Grade){
        this.RubricOverallGrade = Grade;
    }
}
