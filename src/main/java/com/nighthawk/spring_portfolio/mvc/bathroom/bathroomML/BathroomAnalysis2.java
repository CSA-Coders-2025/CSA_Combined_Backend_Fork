package com.nighthawk.spring_portfolio.mvc.bathroom.bathroomML;

import java.io.File;
import weka.classifiers.trees.J48;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.CSVLoader;
import weka.classifiers.Evaluation;
import java.util.Random;

import static tech.tablesaw.aggregate.AggregateFunctions.mean;
import tech.tablesaw.api.IntColumn;
import tech.tablesaw.api.StringColumn;
import tech.tablesaw.api.Table;
import tech.tablesaw.plotly.Plot;
import tech.tablesaw.plotly.api.Histogram;
import tech.tablesaw.plotly.api.VerticalBarPlot;

public class BathroomAnalysis2 {
    public static void main(String[] args) throws Exception {
        // ✅ Ensure file exists before proceeding
        File csvFile = new File("bathroom_summary.csv");
        if (!csvFile.exists()) {
            System.out.println("❌ CSV file not found! Ensure preprocessing ran first.");
            return;
        }

        // ✅ Load dataset for visualization
        Table bathroom = Table.read().csv(csvFile);

        // ✅ Analysis: Bathroom visits by student
        Table nameCounts = bathroom.countBy(bathroom.stringColumn("Name"));
        System.out.println("📊 Bathroom visits per student:");
        System.out.println(nameCounts);
        Plot.show(VerticalBarPlot.create("Visits per Student", nameCounts, "Name", "Count"));

        // ✅ Analysis: Average duration per student
        Table avgDurationByName = bathroom.summarize("Duration", mean).by("Name");
        System.out.println("📊 Average visit duration per student:");
        System.out.println(avgDurationByName);
        Plot.show(VerticalBarPlot.create("Avg Duration per Student", avgDurationByName, "Name", "Mean [Duration]"));

        // ✅ Analysis: Abnormal visits
        int totalAbnormal = bathroom.where(bathroom.booleanColumn("Abnormal").isTrue()).rowCount();
        int totalNormal = bathroom.rowCount() - totalAbnormal;
        System.out.println("📊 Abnormal vs Normal Visits:");
        System.out.println("Abnormal (>15 min): " + totalAbnormal);
        System.out.println("Normal: " + totalNormal);

        Table abnormalCounts = Table.create("Abnormality")
                .addColumns(
                    StringColumn.create("Type", new String[]{"Normal", "Abnormal"}),
                    IntColumn.create("Count", new int[]{totalNormal, totalAbnormal})
                );
        Plot.show(VerticalBarPlot.create("Abnormal vs Normal Visits", abnormalCounts, "Type", "Count"));

        // ✅ Analysis: Duration Distribution
        Plot.show(Histogram.create("Distribution of Visit Durations", bathroom.numberColumn("Duration")));

        // ✅ Machine Learning: Predicting abnormal visits using J48 Decision Tree
        CSVLoader loader = new CSVLoader();
        loader.setSource(csvFile);
        Instances data = loader.getDataSet();

        // ✅ Convert boolean "Abnormal" to nominal (needed for Weka)
        data.renameAttribute(data.attribute("Abnormal"), "Abnormal (Nominal)");
        data.setClassIndex(data.numAttributes() - 1); // Ensure "Abnormal" is the last column

        System.out.println("✅ Data Loaded. Number of instances: " + data.numInstances());

        // ✅ Train J48 Decision Tree model
        J48 tree = new J48();
        tree.buildClassifier(data);
        
        // ✅ Evaluate model using cross-validation
        Evaluation eval = new Evaluation(data);
        eval.crossValidateModel(tree, data, 10, new Random(1));
        System.out.println("✅ Model Evaluation:");
        System.out.println(eval.toSummaryString());                                                             

        // ✅ Predict abnormality for a new instance
        Instance testInstance = data.firstInstance();
        double prediction = tree.classifyInstance(testInstance);
        System.out.println("✅ Predicted Abnormality: " + data.classAttribute().value((int) prediction));
    }
}
