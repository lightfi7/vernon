package model;

import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;
import weka.classifiers.trees.J48;
import weka.classifiers.Evaluation;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;
import weka.core.converters.CSVSaver;
import java.io.File;
import java.util.ArrayList;
import java.util.Random;

public class J48Classifier {

    public static J48 tree;
    public static Instances dataset;

    static {
        try {
            DataSource source = new DataSource("data/weka_model.arff");
            dataset = source.getDataSet();
            dataset.setClassIndex(dataset.numAttributes() - 1);
            tree = new J48();
            tree.setOptions(new String[] {"-C", "0.25", "-M", "2"});
            tree.buildClassifier(dataset);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void predict(double[] features) {
        try {

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

/*
    public static void main(String[] args) throws Exception {
        // Load the ARFF file
        DataSource source = new DataSource("data/vernon.arff");
        Instances data = source.getDataSet();

        // Set the class index (assumed to be the last column)
        if (data.classIndex() == -1) {
            data.setClassIndex(data.numAttributes() - 1);
        }

        System.out.println(data.get(1).numValues());

        // Remove specific columns (1st, 2nd, 3rd, 5th, 38th, 39th, 49th)
        Remove remove = new Remove();
        final String[] removeOptions = new String[]{"-R", "1,2,3,5,38,39,49"};
        remove.setOptions(removeOptions);

        remove.setInputFormat(data);

        Instances filteredData = Filter.useFilter(data, remove);

        // Train the J48 model with specified parameters (-c 0.25 -m 2)
        String[] options = new String[]{"-C", "0.25", "-M", "2"};
        tree.setOptions(options);

        // Perform 10-fold cross-validation for evaluation
        Evaluation eval = new Evaluation(filteredData);
        eval.crossValidateModel(tree, filteredData, 10, new Random(1));

        // Print cross-validation results
        System.out.println(eval.toSummaryString("\n10-Fold Cross-Validation Results\n======\n", false));

        // Now build the classifier on the entire dataset
        tree.buildClassifier(filteredData);

        // Create a new dataset with predicted labels
        Instances outputData = new Instances(data);
        outputData.insertAttributeAt(new weka.core.Attribute("PredictedClass"), outputData.numAttributes());

        // Classify each instance and add the prediction to the outputData
        for (int i = 0; i < filteredData.numInstances(); i++) {
            double prediction = tree.classifyInstance(filteredData.instance(i));
            outputData.instance(i).setValue(outputData.numAttributes() - 1, prediction);
        }

        // Save the output with predictions to a new CSV file
        CSVSaver saver = new CSVSaver();
        saver.setInstances(outputData);
        saver.setFile(new File("output.csv"));
        saver.writeBatch();

        System.out.println("Final model built on the entire dataset and output saved to CSV.");
    }
*/


}