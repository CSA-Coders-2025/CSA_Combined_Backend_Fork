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
        saveToFile(genderPlot, "titanic_plots/gender_counts.html");
        System.out.println("Gender plot saved to: titanic_plots/gender_counts.html");

        // 2. Fare analysis: Check survival based on "Fare" mean and median
        System.out.println("\nSurvival rate based on Fare Analysis:");
        System.out.println("Mean Fare for Survivors: " + survived.numberColumn("Fare").mean());
        System.out.println("Median Fare for Survivors: " + survived.numberColumn("Fare").median());
        System.out.println("Mean Fare for Non-Survivors: " + perished.numberColumn("Fare").mean());
        System.out.println("Median Fare for Non-Survivors: " + perished.numberColumn("Fare").median()); 
        
        Figure farePlot = Histogram.create("Fare Distribution", titanic.numberColumn("Fare"));
        saveToFile(farePlot, "titanic_plots/fare_histogram.html");
        System.out.println("Fare plot saved to: titanic_plots/fare_histogram.html");

        // 3. Being alone analysis: Check survival based on "Alone" column
        System.out.println("\nSurvival Rate Based on 'Alone' Status:");
        System.out.println("Survived Alone: " + survived.where(survived.booleanColumn("Alone").isTrue()).rowCount());
        System.out.println("Perished Alone: " + perished.where(perished.booleanColumn("Alone").isTrue()).rowCount());
        System.out.println("Survived with Family: " + survived.where(survived.booleanColumn("Alone").isFalse()).rowCount());
        System.out.println("Perished with Family: " + perished.where(perished.booleanColumn("Alone").isFalse()).rowCount());
        
        Figure alonePlot = VerticalBarPlot.create("Count of Passengers by Alone Status", aloneCounts, "Alone", "Count");
        saveToFile(alonePlot, "titanic_plots/alone_counts.html");
        System.out.println("Alone status plot saved to: titanic_plots/alone_counts.html");

        // 4. Age analysis: Check survival based on "Age" mean and median
        System.out.println("\nSurvival rate based on Age Analysis:");
        System.out.println("Mean Age for Survivors: " + survived.numberColumn("Age").mean());
        System.out.println("Median Age for Survivors: " + survived.numberColumn("Age").median());
        System.out.println("Mean Age for Non-Survivors: " + perished.numberColumn("Age").mean());
        System.out.println("Median Age for Non-Survivors: " + perished.numberColumn("Age").median());
        
        Figure agePlot = Histogram.create("Age Distribution", titanic.numberColumn("Age"));
        saveToFile(agePlot, "titanic_plots/age_histogram.html");
        System.out.println("Age plot saved to: titanic_plots/age_histogram.html");
    }
    
    // Update the saveToFile method in TitanicAnalysis.java
    private static void saveToFile(Figure figure, String filename) {
        // Path to templates directory
        String templatesDir = "src/main/resources/templates/";
        // Create directory if it doesn't exist
        new File(templatesDir + "titanic_plots").mkdirs();
        
        try (FileWriter fileWriter = new FileWriter(templatesDir + filename)) {
            // Create a complete HTML page with Thymeleaf layout
            StringBuilder html = new StringBuilder();
            html.append("<!DOCTYPE html>\n");
            html.append("<html xmlns:layout=\"http://www.w3.org/1999/xhtml\" xmlns:th=\"http://www.thymeleaf.org\"\n");
            html.append("    layout:decorate=\"~{layouts/base}\" lang=\"en\">\n\n");
            html.append("<th:block layout:fragment=\"title\" th:remove=\"tag\">Titanic Analysis</th:block>\n\n");
            html.append("<th:block layout:fragment=\"body\" th:remove=\"tag\">\n");
            html.append("  <div class=\"container\">\n");
            html.append("    <h2>Titanic Data Analysis</h2>\n");
            html.append("    <div id='plot_div'></div>\n");
            html.append("  </div>\n");
            html.append("</th:block>\n\n");
            html.append("<th:block layout:fragment=\"script\" th:remove=\"tag\">\n");
            html.append("  <script src=\"https://cdn.plot.ly/plotly-latest.min.js\"></script>\n");
            html.append("  <script>\n");
            html.append("    document.addEventListener('DOMContentLoaded', function() {\n");
            html.append("      var target_plot_div = document.getElementById('plot_div');\n");
            html.append("      " + extractJavaScriptFromFigure(figure.asJavascript("plot_div")) + "\n");
            html.append("    });\n");
            html.append("  </script>\n");
            html.append("</th:block>\n");
            html.append("</html>");
            
            fileWriter.write(html.toString());
            System.out.println("Plot saved to templates/titanic_plots/" + filename);
        } catch (IOException e) {
            System.err.println("Error saving plot to " + filename + ": " + e.getMessage());
        }
    }

    // Helper to extract just the JavaScript portion from the Figure
    private static String extractJavaScriptFromFigure(String figureJs) {
        // Find the start of the plotly data section
        int startIndex = figureJs.indexOf("var data");
        if (startIndex == -1) startIndex = figureJs.indexOf("var layout");
        
        // Find the end of the plotly.newPlot call
        int endIndex = figureJs.indexOf("</script>");
        if (endIndex == -1) endIndex = figureJs.length();
        
        // Extract just the JS code without script tags
        if (startIndex >= 0 && endIndex > startIndex) {
            return figureJs.substring(startIndex, endIndex).trim();
        }
        
        // If we can't parse it properly, return a simplified version
        return "console.log('Error extracting plotly code');\n" + 
            "Plotly.newPlot(target_plot_div, [{x:[0,1], y:[0,1], type:'scatter'}], {title:'Error Loading Plot'});";
    }
}