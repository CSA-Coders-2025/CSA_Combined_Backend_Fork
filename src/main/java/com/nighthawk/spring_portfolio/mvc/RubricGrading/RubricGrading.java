package com.nighthawk.spring_portfolio.mvc.RubricGrading;

import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RubricGrading {
    @NotNull
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private Double RubricOverallGrade;

    @Column(unique=false)
    @NotNull
    private List<String> topic;

    @Column(unique=false)
    @NotNull
    private List<Double> weightage;

    @Column(unique=false)
    @NotNull
    private List<Double> points; // Range from 0 to 1



    public RubricGrading(List<String>topic, List<Double>weightage, List<Double>points){
        this.topic=topic;
        this.weightage=weightage;
        this.points=points;
    }


    public RubricGrading(String topic, Double weightage, Double points){
        this.topic.add(topic);
        this.weightage.add(weightage);
        this.points.add(points);
        this.RubricOverallGrade=points*weightage;
    }


    public double getOverallGrade(){
        if(RubricOverallGrade!=null){
            return this.RubricOverallGrade;
        }
        double grade=0;
        for(int i=0;i<topic.size();i++){
            grade+=weightage.get(i)*points.get(i);
        }
        return grade;
    }

    // method to edit a topic 
    public void setTopic(String newtopic, int index){
        topic.set(index,newtopic);
    }

    public void setGrade(Double newgrade, int index){
        points.set(index,newgrade);
    }

    public void setWeight(Double neweight, int index){
        weightage.set(index,neweight);
    }



    // method to set the overall grade(override)
    public void setOverallGrade(double Grade){
        this.RubricOverallGrade=Grade;
    }




}
