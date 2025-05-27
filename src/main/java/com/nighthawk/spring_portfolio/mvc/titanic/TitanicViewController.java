// TitanicViewController.java
package com.nighthawk.spring_portfolio.mvc.titanic;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class TitanicViewController {

    @GetMapping("/titanic")
    public String titanicDashboard() {
        return "titanic_plots/gender_counts";
    }
    
    @GetMapping("/titanic/gender")
    public String genderAnalysis() {
        return "titanic_plots/gender_counts";
    }
    
    @GetMapping("/titanic/age")
    public String ageAnalysis() {
        return "titanic_plots/age_histogram";
    }
    
    @GetMapping("/titanic/fare")
    public String fareAnalysis() {
        return "titanic_plots/fare_histogram";
    }
    
    @GetMapping("/titanic/alone")
    public String aloneAnalysis() {
        return "titanic_plots/alone_counts";
    }
}