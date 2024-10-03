package model;
import app.Application;
import config.Config;
import utils.EventHandler;
import utils.Logger;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instances;
import weka.core.converters.CSVSaver;
import weka.core.converters.ConverterUtils.DataSource;
import weka.classifiers.trees.J48;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;

public class J48Classifier implements EventHandler {

    public static J48 tree = new J48();
    public static ArrayList<Attribute> attributes = new ArrayList<>();

    public J48Classifier() {

        try {
            Logger.log("Initializing J48 Classifier...");
            // Load the ARFF file
            DataSource source = new DataSource(Config.ARFF_FILE);
            Instances data = source.getDataSet();

            // Set the attributes from data instance

            for (int i = 0; i < data.numAttributes(); i++) {
                attributes.add(new Attribute(data.attribute(i).name()));
            }

            // Set the class index (assumed to be the last column)
            if (data.classIndex() == -1) {
                data.setClassIndex(data.numAttributes() - 1);
            }

//            System.out.println(data.get(1).numValues());

            // Remove specific columns (1st, 2nd, 3rd, 5th, 14th, 38th, 39th, 49th)
            Remove remove = new Remove();
            final String[] removeOptions = new String[]{"-R", "1,2,3,5,14,38,39,49"};
            remove.setOptions(removeOptions);

            remove.setInputFormat(data);

            Instances filteredData = Filter.useFilter(data, remove);

            // Train the J48 model with specified parameters (-c 0.25 -m 2)
            String[] options = new String[]{"-C", "0.25", "-M", "2"};
            tree.setOptions(options);

            // Now build the classifier on the entire dataset
            tree.buildClassifier(filteredData);

            Application.instance.isReady = true;
            Logger.log("Initialized J48 Classifier");

        } catch (Exception e) {
            e.printStackTrace();
            Logger.log("Error: " + e.getMessage());
        }

    }

    public void predict(double[] features) {
        try {
            Instances data = new Instances("InboundInstance", attributes, 0);

            data.add(new DenseInstance(1.0, features));

            if (data.classIndex() == -1) {
                data.setClassIndex(data.numAttributes() - 1);
            }

            System.out.println(data.numClasses());
            // Remove specific columns (1st, 2nd, 3rd, 5th, 14th, 38th, 39th, 49th)
            Remove remove = new Remove();
            final String[] removeOptions = new String[]{"-R", "1,2,3,5,14,38,39,49"};
            remove.setOptions(removeOptions);

            remove.setInputFormat(data);

            Instances filteredData = Filter.useFilter(data, remove);

            // Create a new dataset with predicted labels
            Instances outputData = new Instances(data);
            outputData.insertAttributeAt(new weka.core.Attribute("PredictedClass"), outputData.numAttributes());

            double[] predictions = new double[outputData.numInstances()];

            // Classify each instance and add the prediction to the outputData
            for (int i = 0; i < filteredData.numInstances(); i++) {
                double prediction = tree.classifyInstance(filteredData.instance(i));
                predictions[i] = prediction;
                System.out.println(prediction);
                outputData.instance(i).setValue(outputData.numAttributes() - 1, prediction);
            }

            Logger.log("Outbound: " + Arrays.toString(predictions));

//            Application.instance.client.send(predictions.toString());

            // Save the output with predictions to a new CSV file
            CSVSaver saver = new CSVSaver();
            saver.setInstances(outputData);
            saver.setFile(new File("output.csv"));
            saver.writeBatch();

        } catch (Exception e) {
            Logger.log("Error: " + e.getMessage());
        }
    }

    @Override
    public void onDataReceived(String jsonString) {
        try {
            Logger.log("Inbound data: "+jsonString);

            if(!Application.instance.isReady){
                Logger.log("Application isn't ready");
                return;
            }

            // Parse JSON string to a JSONObject
            JSONObject jsonObject = new JSONObject(jsonString);

            // Prepare the data as a Map
            Map<String, Object> jsonData = new HashMap<String, Object>();
            jsonData.put("time", jsonObject.getString("time"));
            jsonData.put("epoch", jsonObject.getDouble("epoch"));
            jsonData.put("equityLast", jsonObject.getDouble("equityLast"));
            jsonData.put("AAAA", jsonObject.getDouble("AAAA"));
            jsonData.put("BBBB", jsonObject.getDouble("BBBB"));
            jsonData.put("CCCC", jsonObject.getDouble("CCCC"));
            jsonData.put("TTTT", jsonObject.getDouble("TTTT"));

            ArrayList<Double> features = Feature.parse(jsonData);
            System.out.println(features);

            double[] values = new double[features.size()];
            for (int i = 0; i < features.size(); i++) {
                values[i] = features.get(i);
            }

            predict(values);


        }catch (Exception e){
            e.printStackTrace();
            Logger.log("Error: " + e.getMessage());
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