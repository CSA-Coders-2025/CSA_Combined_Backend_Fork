package com.nighthawk.spring_portfolio.hacks.tablesaw;

import tech.tablesaw.api.Table;
import tech.tablesaw.api.BooleanColumn;
import tech.tablesaw.api.NumericColumn;
import tech.tablesaw.plotly.components.Figure;
import tech.tablesaw.plotly.api.Histogram;
import tech.tablesaw.plotly.api.VerticalBarPlot;
import tech.tablesaw.plotly.components.Layout;

import java.io.File;
import java.io.InputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class TitanicAnalysis {
    public static void main(String[] args) throws Exception {
        // Load Titanic dataset from the classpath into a Tablesaw Table
        InputStream inputStream = TitanicAnalysis.class.getResourceAsStream("/data/titanic.csv");
        if (inputStream == null) {
            throw new IllegalArgumentException("File not found: titanic.csv");
        }
        Table titanic = Table.read().csv(inputStream);

        // Add "alone" column based on "SibSp (Siblings/Spouses)" and "Parch (Parents/Children)"
        NumericColumn<?> sibSpColumn = titanic.numberColumn("SibSp");
        NumericColumn<?> parchColumn = titanic.numberColumn("Parch");
        BooleanColumn aloneColumn = BooleanColumn.create("Alone", titanic.rowCount());
        // Add a new column for each row, indicating whether the passenger is alone
        for (int i = 0; i < titanic.rowCount(); i++) {
            // If both SibSp and Parch are 0, then the passenger is alone otherwise not
            // Number casting is required because Tablesaw does not support primitive types
            boolean isAlone = ((Number) sibSpColumn.get(i)).doubleValue() == 0 && ((Number) parchColumn.get(i)).doubleValue() == 0;
            aloneColumn.set(i, isAlone);
        }
        titanic.addColumns(aloneColumn);

        // Filter the data into distinct tables for analysis
        Table perished = titanic.where(titanic.numberColumn("Survived").isEqualTo(0));
        Table survived = titanic.where(titanic.numberColumn("Survived").isEqualTo(1));
        Table sexCounts = titanic.countBy(titanic.stringColumn("Sex"));
        Table aloneCounts = titanic.countBy(titanic.booleanColumn("Alone"));

        // Ensure output directory exists
        Files.createDirectories(Paths.get("titanic_plots"));

        // Display structure and first rows
        System.out.println(titanic.structure());
        System.out.println(titanic.first(5));
        
        // Conclusions:
        // 1. Gender analysis: Check for survival based on "Sex" column
        System.out.println("\nSurvival rate by gender:");
        System.out.println("Males survived: " + survived.where(survived.stringColumn("Sex").isEqualTo("male")).rowCount());
        System.out.println("Males perished: " + perished.where(perished.stringColumn("Sex").isEqualTo("male")).rowCount());
        System.out.println("Females survived: " + survived.where(survived.stringColumn("Sex").isEqualTo("female")).rowCount());
        System.out.println("Females perished: " + perished.where(perished.stringColumn("Sex").isEqualTo("female")).rowCount());
        
        // Save plot to file instead of showing it
        Figure genderPlot = VerticalBarPlot.create("Count of Passengers by Gender", sexCounts, "Sex", "Count");
        saveToFile(genderPlot, "titanic_plots/gender_counts.html", titanic, survived, perished);
        System.out.println("Gender plot saved to: titanic_plots/gender_counts.html");

        // 2. Fare analysis: Check survival based on "Fare" mean and median
        System.out.println("\nSurvival rate based on Fare Analysis:");
        System.out.println("Mean Fare for Survivors: " + survived.numberColumn("Fare").mean());
        System.out.println("Median Fare for Survivors: " + survived.numberColumn("Fare").median());
        System.out.println("Mean Fare for Non-Survivors: " + perished.numberColumn("Fare").mean());
        System.out.println("Median Fare for Non-Survivors: " + perished.numberColumn("Fare").median()); 
        
        Figure farePlot = Histogram.create("Fare Distribution", titanic.numberColumn("Fare"));
        saveToFile(farePlot, "titanic_plots/fare_histogram.html", titanic, survived, perished);
        System.out.println("Fare plot saved to: titanic_plots/fare_histogram.html");

        // 3. Being alone analysis: Check survival based on "Alone" column
        System.out.println("\nSurvival Rate Based on 'Alone' Status:");
        System.out.println("Survived Alone: " + survived.where(survived.booleanColumn("Alone").isTrue()).rowCount());
        System.out.println("Perished Alone: " + perished.where(perished.booleanColumn("Alone").isTrue()).rowCount());
        System.out.println("Survived with Family: " + survived.where(survived.booleanColumn("Alone").isFalse()).rowCount());
        System.out.println("Perished with Family: " + perished.where(perished.booleanColumn("Alone").isFalse()).rowCount());
        
        Figure alonePlot = VerticalBarPlot.create("Count of Passengers by Alone Status", aloneCounts, "Alone", "Count");
        saveToFile(alonePlot, "titanic_plots/alone_counts.html", titanic, survived, perished);
        System.out.println("Alone status plot saved to: titanic_plots/alone_counts.html");

        // 4. Age analysis: Check survival based on "Age" mean and median
        System.out.println("\nSurvival rate based on Age Analysis:");
        System.out.println("Mean Age for Survivors: " + survived.numberColumn("Age").mean());
        System.out.println("Median Age for Survivors: " + survived.numberColumn("Age").median());
        System.out.println("Mean Age for Non-Survivors: " + perished.numberColumn("Age").mean());
        System.out.println("Median Age for Non-Survivors: " + perished.numberColumn("Age").median());
        
        Figure agePlot = Histogram.create("Age Distribution", titanic.numberColumn("Age"));
        saveToFile(agePlot, "titanic_plots/age_histogram.html", titanic, survived, perished);
        System.out.println("Age plot saved to: titanic_plots/age_histogram.html");
    }
    
    private static void saveToFile(Figure figure, String filename, Table titanic, Table survived, Table perished) {
        String templatesDir = "src/main/resources/templates/";
        new File(templatesDir + "titanic_plots").mkdirs();
        
        try (FileWriter fileWriter = new FileWriter(templatesDir + filename)) {
            // Get the title from the filename instead of the figure
            String title = filename.substring(filename.lastIndexOf("/") + 1)
                                .replace(".html", "")
                                .replace("_", " ")
                                .toUpperCase();
            
            // Create statistics based on the file type
            StringBuilder stats = new StringBuilder();
            if (title.contains("GENDER")) {
                stats.append("<div class=\"row mt-4\">");
                stats.append("  <div class=\"col-md-6\">");
                stats.append("    <h5>Survival Statistics by Gender</h5>");
                stats.append("    <table class=\"table table-bordered\">");
                stats.append("      <thead><tr><th>Gender</th><th>Survived</th><th>Perished</th><th>Survival Rate</th></tr></thead>");
                stats.append("      <tbody>");
                
                int malesSurvived = survived.where(survived.stringColumn("Sex").isEqualTo("male")).rowCount();
                int malesPerished = perished.where(perished.stringColumn("Sex").isEqualTo("male")).rowCount();
                int femalesSurvived = survived.where(survived.stringColumn("Sex").isEqualTo("female")).rowCount();
                int femalesPerished = perished.where(perished.stringColumn("Sex").isEqualTo("female")).rowCount();
                
                double maleSurvivalRate = (double) malesSurvived / (malesSurvived + malesPerished) * 100;
                double femaleSurvivalRate = (double) femalesSurvived / (femalesSurvived + femalesPerished) * 100;
                
                stats.append("        <tr><td>Male</td><td>" + malesSurvived + "</td><td>" + malesPerished + "</td><td>" + String.format("%.1f%%", maleSurvivalRate) + "</td></tr>");
                stats.append("        <tr><td>Female</td><td>" + femalesSurvived + "</td><td>" + femalesPerished + "</td><td>" + String.format("%.1f%%", femaleSurvivalRate) + "</td></tr>");
                stats.append("      </tbody>");
                stats.append("    </table>");
                stats.append("  </div>");
                stats.append("  <div class=\"col-md-6\">");
                stats.append("    <h5>Key Finding</h5>");
                stats.append("    <p>Women had a much higher survival rate (" + String.format("%.1f%%", femaleSurvivalRate) + ") compared to men (" + String.format("%.1f%%", maleSurvivalRate) + ").</p>");
                stats.append("    <p>This supports the \"women and children first\" protocol that was followed during the Titanic's evacuation.</p>");
                stats.append("  </div>");
                stats.append("</div>");
            } else if (title.contains("FARE")) {
                stats.append("<div class=\"row mt-4\">");
                stats.append("  <div class=\"col-md-6\">");
                stats.append("    <h5>Fare Statistics</h5>");
                stats.append("    <table class=\"table table-bordered\">");
                stats.append("      <thead><tr><th>Group</th><th>Mean Fare</th><th>Median Fare</th></tr></thead>");
                stats.append("      <tbody>");
                
                double survivorMeanFare = survived.numberColumn("Fare").mean();
                double survivorMedianFare = survived.numberColumn("Fare").median();
                double perishedMeanFare = perished.numberColumn("Fare").mean();
                double perishedMedianFare = perished.numberColumn("Fare").median();
                
                stats.append("        <tr><td>Survivors</td><td>$" + String.format("%.2f", survivorMeanFare) + "</td><td>$" + String.format("%.2f", survivorMedianFare) + "</td></tr>");
                stats.append("        <tr><td>Non-Survivors</td><td>$" + String.format("%.2f", perishedMeanFare) + "</td><td>$" + String.format("%.2f", perishedMedianFare) + "</td></tr>");
                stats.append("      </tbody>");
                stats.append("    </table>");
                stats.append("  </div>");
                stats.append("  <div class=\"col-md-6\">");
                stats.append("    <h5>Key Finding</h5>");
                stats.append("    <p>Passengers who paid higher fares had better chances of survival.</p>");
                stats.append("    <p>The mean fare for survivors ($" + String.format("%.2f", survivorMeanFare) + ") was significantly higher than for non-survivors ($" + String.format("%.2f", perishedMeanFare) + ").</p>");
                stats.append("    <p>This suggests that first-class passengers had priority access to lifeboats.</p>");
                stats.append("  </div>");
                stats.append("</div>");
            } else if (title.contains("ALONE")) {
                stats.append("<div class=\"row mt-4\">");
                stats.append("  <div class=\"col-md-6\">");
                stats.append("    <h5>Traveling Status Statistics</h5>");
                stats.append("    <table class=\"table table-bordered\">");
                stats.append("      <thead><tr><th>Status</th><th>Survived</th><th>Perished</th><th>Survival Rate</th></tr></thead>");
                stats.append("      <tbody>");
                
                int aloneAndSurvived = survived.where(survived.booleanColumn("Alone").isTrue()).rowCount();
                int aloneAndPerished = perished.where(perished.booleanColumn("Alone").isTrue()).rowCount();
                int familyAndSurvived = survived.where(survived.booleanColumn("Alone").isFalse()).rowCount();
                int familyAndPerished = perished.where(perished.booleanColumn("Alone").isFalse()).rowCount();
                
                double aloneSurvivalRate = (double) aloneAndSurvived / (aloneAndSurvived + aloneAndPerished) * 100;
                double familySurvivalRate = (double) familyAndSurvived / (familyAndSurvived + familyAndPerished) * 100;
                
                stats.append("        <tr><td>Traveling Alone</td><td>" + aloneAndSurvived + "</td><td>" + aloneAndPerished + "</td><td>" + String.format("%.1f%%", aloneSurvivalRate) + "</td></tr>");
                stats.append("        <tr><td>With Family</td><td>" + familyAndSurvived + "</td><td>" + familyAndPerished + "</td><td>" + String.format("%.1f%%", familySurvivalRate) + "</td></tr>");
                stats.append("      </tbody>");
                stats.append("    </table>");
                stats.append("  </div>");
                stats.append("  <div class=\"col-md-6\">");
                stats.append("    <h5>Key Finding</h5>");
                stats.append("    <p>Passengers traveling with family had a " + String.format("%.1f%%", familySurvivalRate) + " survival rate compared to " + String.format("%.1f%%", aloneSurvivalRate) + " for those traveling alone.</p>");
                stats.append("    <p>This suggests that families may have been kept together during evacuation, or that they helped each other survive.</p>");
                stats.append("  </div>");
                stats.append("</div>");
            } else if (title.contains("AGE")) {
                stats.append("<div class=\"row mt-4\">");
                stats.append("  <div class=\"col-md-6\">");
                stats.append("    <h5>Age Statistics</h5>");
                stats.append("    <table class=\"table table-bordered\">");
                stats.append("      <thead><tr><th>Group</th><th>Mean Age</th><th>Median Age</th></tr></thead>");
                stats.append("      <tbody>");
                
                double survivorMeanAge = survived.numberColumn("Age").mean();
                double survivorMedianAge = survived.numberColumn("Age").median();
                double perishedMeanAge = perished.numberColumn("Age").mean();
                double perishedMedianAge = perished.numberColumn("Age").median();
                
                stats.append("        <tr><td>Survivors</td><td>" + String.format("%.1f", survivorMeanAge) + "</td><td>" + String.format("%.1f", survivorMedianAge) + "</td></tr>");
                stats.append("        <tr><td>Non-Survivors</td><td>" + String.format("%.1f", perishedMeanAge) + "</td><td>" + String.format("%.1f", perishedMedianAge) + "</td></tr>");
                stats.append("      </tbody>");
                stats.append("    </table>");
                stats.append("  </div>");
                stats.append("  <div class=\"col-md-6\">");
                stats.append("    <h5>Key Finding</h5>");
                stats.append("    <p>The average age of survivors (" + String.format("%.1f", survivorMeanAge) + ") was lower than non-survivors (" + String.format("%.1f", perishedMeanAge) + ").</p>");
                stats.append("    <p>This supports the priority given to children during evacuation.</p>");
                stats.append("  </div>");
                stats.append("</div>");
            }
            
            // Create HTML template with the statistics included
            StringBuilder html = new StringBuilder();
            html.append("<!DOCTYPE html>\n");
            html.append("<html xmlns:layout=\"http://www.w3.org/1999/xhtml\" xmlns:th=\"http://www.thymeleaf.org\"\n");
            html.append("    layout:decorate=\"~{layouts/base}\" lang=\"en\">\n\n");
            html.append("<th:block layout:fragment=\"title\" th:remove=\"tag\">Titanic Analysis</th:block>\n\n");
            html.append("<th:block layout:fragment=\"body\" th:remove=\"tag\">\n");
            html.append("  <div class=\"container\">\n");
            html.append("    <h2>Titanic Data Analysis</h2>\n");
            html.append("    <a href=\"/titanic\" class=\"btn btn-secondary mb-3\">← Back to Dashboard</a>\n");
            html.append("    <div class=\"card\">\n");
            html.append("      <div class=\"card-header bg-info text-white\">\n");
            html.append("        <h4>" + title + "</h4>\n");
            html.append("      </div>\n");
            html.append("      <div class=\"card-body\">\n");
            html.append("        " + stats.toString() + "\n");
            html.append("      </div>\n");
            html.append("    </div>\n");
            html.append("  </div>\n");
            html.append("</th:block>\n");
            html.append("</html>");
            
            fileWriter.write(html.toString());
            System.out.println("Plot saved to templates/" + filename);
        } catch (IOException e) {
            System.err.println("Error saving plot to " + filename + ": " + e.getMessage());
        }
    }
}