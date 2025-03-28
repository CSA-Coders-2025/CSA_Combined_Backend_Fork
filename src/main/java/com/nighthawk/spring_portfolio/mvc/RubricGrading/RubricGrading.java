package com.nighthawk.spring_portfolio.mvc.RubricGrading;

import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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

    // @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true) // many rubric items(singular topics, weightages) inside one rubric gradinger
    private List<RubricItem> rubricItems;

    public RubricGrading(List<RubricItem> rubricItems) { 
        this.rubricItems = rubricItems;
    }


    public RubricGrading(String topic, Double weightage, Double points){
        this.rubricItems.add(new RubricItem(topic,weightage,points));
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

