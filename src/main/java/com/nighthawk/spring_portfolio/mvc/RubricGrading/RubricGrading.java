package com.nighthawk.spring_portfolio.mvc.RubricGrading;

import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Entity
public class RubricGrading {
    @NotNull
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private Double RubricOverallGrade;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "rubric_grading_id")
    private List<RubricItemDTO> rubricItems;

    public RubricGrading(List<RubricItemDTO> rubricItems) { 
        this.rubricItems = rubricItems;
    }


    public RubricGrading(String topic, Double weightage, Double points){
        this.rubricItems.add(new RubricItemDTO(topic,weightage,points));
    }


    public double getOverallGrade(){
        if(RubricOverallGrade!=null){
            return this.RubricOverallGrade;
        }
        double grade=0;
        for(int i=0;i<rubricItems.size();i++){
            grade+=rubricItems.get(i).getWeightage()*rubricItems.get(i).getPoints();
        }
        return grade;
    }

    // method to edit a topic 
    public void setTopic(String newtopic, int index){
        rubricItems.get(index).setTopic(newtopic);
    }

    public void setGrade(Double newgrade, int index){
        rubricItems.get(index).setPoints(newgrade);
    }

    public void setWeight(Double neweight, int index){
        rubricItems.get(index).setWeightage(neweight);
    }



    // method to set the overall grade(override)
    public void setOverallGrade(double Grade){
        this.RubricOverallGrade=Grade;
    }




}

