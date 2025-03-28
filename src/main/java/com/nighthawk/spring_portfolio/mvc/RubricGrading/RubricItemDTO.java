package com.nighthawk.spring_portfolio.mvc.RubricGrading;

import jakarta.persistence.Entity;
import lombok.Data;

@Data
@Entity
public class RubricItemDTO {
    public String topic;
    public Double weightage;
    public Double points;


    public RubricItemDTO(String topic, Double weightage, Double points){
        this.topic=topic;
        this.weightage=weightage;
        this.points=points;
    }
}