package com.nighthawk.spring_portfolio.mvc.synergy.SynergyRubricGrade;

import java.util.Map;

import com.nighthawk.spring_portfolio.mvc.synergy.SynergyGrade.SynergyGrade;

import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Table;

@Entity
@Table(name = "synergy_rubric_grade")
public class SynergyRubricGrade extends SynergyGrade {
    
    @ElementCollection(fetch = FetchType.EAGER)
    private Map<String, RubricCriterion> rubricScores; // Stores rubric criteria
    
    public SynergyRubricGrade() {}
    
    public SynergyRubricGrade(Map<String, RubricCriterion> rubricScores) {
        this.rubricScores = rubricScores;
    }
    
    public Map<String, RubricCriterion> getRubricScores() {
        return rubricScores;
    }
    
    public void setRubricScores(Map<String, RubricCriterion> rubricScores) {
        this.rubricScores = rubricScores;
    }
    
    public double getTotalScore() {
        return rubricScores.values().stream()
            .mapToDouble(criterion -> criterion.getWeightage() * criterion.getPoints())
            .sum();
    }
    
    public static class RubricCriterion {
        private String topic;
        private double weightage;
        private double points; // Range from 0 to 1

        public RubricCriterion() {}

        public RubricCriterion(String topic, double weightage, double points) {
            this.topic = topic;
            this.weightage = weightage;
            this.points = points;
        }

        public String getTopic() {
            return topic;
        }

        public void setTopic(String topic) {
            this.topic = topic;
        }

        public double getWeightage() {
            return weightage;
        }

        public void setWeightage(double weightage) {
            this.weightage = weightage;
        }

        public double getPoints() {
            return points;
        }

        public void setPoints(double points) {
            this.points = points;
        }
    }
}