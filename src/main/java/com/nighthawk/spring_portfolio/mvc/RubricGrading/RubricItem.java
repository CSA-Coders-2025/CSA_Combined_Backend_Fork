package com.nighthawk.spring_portfolio.mvc.RubricGrading;

import lombok.Data;

@Data
public class RubricItem {
    public String topic;
    public Double weightage;
    public Double points;


    public RubricItem(String topic, Double weightage, Double points){
        this.topic=topic;
        this.weightage=weightage;
        this.points=points;
    }
}