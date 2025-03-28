package com.nighthawk.spring_portfolio.mvc.RubricGrading;

import jakarta.persistence.Embeddable;
import lombok.Data;
@Embeddable
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