package model;

import app.Application;
import config.Config;
import utils.EventHandler;
import utils.Logger;
import weka.classifiers.Evaluation;
import weka.classifiers.trees.J48;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;

import org.json.JSONObject;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class J48Classifier implements EventHandler {

    private J48 tree;
    private final ArrayList<Attribute> attributes = new ArrayList<Attribute>();
    private int lastPosition = 0;
    private int M = 0;
    private Double lastEquity = 0.0;

    public J48Classifier() {
        try {
            Logger.log("Initializing J48 Classifier...");
            initialize();
            Logger.log("Initialized J48 Classifier");
        } catch (Exception e) {
            Logger.log("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void initialize() throws Exception {
        DataSource source = new DataSource(Config.ARFF_FILE);
        Instances data = source.getDataSet();

        // Set the class index (assumed to be the last column)
        if (data.classIndex() == -1) {
            data.setClassIndex(data.numAttributes() - 1);
        }

        // Remove specific columns
        Remove remove = new Remove();
        remove.setOptions(new String[]{"-R", "1,2,3,5,13,38,39,49"});
        remove.setInputFormat(data);

        Instances filteredData = Filter.useFilter(data, remove);
        setAttributes(data);

        // Initial model load if M is set
        if (Config.M > 0) {
            load(Config.M);
        }
    }

    private void setAttributes(Instances data) {
        for (int i = 0; i < data.numAttributes(); i++) {
            attributes.add(data.attribute(i));
        }
    }

    public void load(int M) throws Exception {
        if (this.M == M) return;

        Logger.log("Loading M" + M + " model...");
        Application.instance.setIsReady(false);
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream("data/j48_" + M + ".model"))) {
            tree = (J48) ois.readObject();
            this.M = M;
            Logger.log("Loaded M" + M + " model");
        } catch (FileNotFoundException e) {
            Logger.log("Model file not found: " + e.getMessage());
            train(M);  // Train a new model if loading fails
        } finally {
            Application.instance.setIsReady(true);
        }
    }

    public void train(int M) {
        try {
            Logger.log("Training M" + M + " model...");
            tree = new J48();
            DataSource source = new DataSource(Config.ARFF_FILE);
            Instances data = source.getDataSet();

            if (data.classIndex() == -1) {
                data.setClassIndex(data.numAttributes() - 1);
            }

            Remove remove = new Remove();
            remove.setOptions(new String[]{"-R", "1,2,3,5,13,38,39,49"});
            remove.setInputFormat(data);

            Instances filteredData = Filter.useFilter(data, remove);
            setJ48Options(M);

            // Perform 10-fold cross-validation
            Evaluation eval = new Evaluation(filteredData);
            eval.crossValidateModel(tree, filteredData, 10, new Random(1));
            Logger.log(eval.toSummaryString("\n10-Fold Cross-Validation Results\n======\n", false));

            // Build classifier on the entire dataset
            tree.buildClassifier(filteredData);
            saveModel(M);
            Logger.log("Trained M" + M + " model");
        } catch (Exception e) {
            Logger.log("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void setJ48Options(int M) throws Exception {
        String[] options;
        switch (M) {
            case 2:
                options = new String[]{"-C", "0.25", "-M", "2"};
                break;
            case 7:
                options = new String[]{"-C", "0.25", "-M", "7"};
                break;
            case 15:
                options = new String[]{"-C", "0.25", "-M", "15"};
                break;
            default:
                options = new String[]{"-C", "0.25", "-M", "2"}; // Default case if none match
                break;
        }
        tree.setOptions(options);
    }

    private void saveModel(int M) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("data/j48_" + M + ".model"))) {
            oos.writeObject(tree);
            oos.flush();
        }
    }

    public void change(int M) {
        try {
            Logger.log("Changing to M" + M + " model...");
            Application.instance.setIsReady(false);
            load(M);
            Logger.log("Changed to M" + M + " model");
            Application.instance.setIsReady(true);
        } catch (Exception e) {
            Logger.log("Error: " + e.getMessage());
        }
    }

    public void predict(double[] features, String time, Double epoch, Double equityLast) {
        try {
            Instances data = new Instances("InboundData", attributes, 0);

            if (data.classIndex() == -1) {
                data.setClassIndex(data.numAttributes() - 1);
            }

            data.add(new DenseInstance(1.0, features));

            Remove remove = new Remove();
            remove.setOptions(new String[]{"-R", "1,2,3,5,13,38,39,49"});
            remove.setInputFormat(data);

            Instances filteredData = Filter.useFilter(data, remove);

            System.out.println(filteredData.instance(0));
            double prediction = tree.classifyInstance(filteredData.instance(0));
            Logger.log("Outbound: " + prediction);

            executeTradeLogic(prediction, epoch, equityLast);
        } catch (Exception e) {
            Logger.log("Error: " + e.getMessage());
        }
    }

    private void executeTradeLogic(double prediction, Double epoch, Double equityLast) {
        int ep = epoch.intValue() / 10000 * 10; // Scale epoch
        if (lastPosition == 0 && prediction == 1) {
            lastPosition = 1;
            logAndSendTrade("COVER", ep, equityLast);
            logAndSendTrade("BUY", ep + 1, equityLast - 10.0);
            lastEquity = equityLast;
        } else if (lastPosition == 1 && prediction == 0) {
            lastPosition = 0;
            logAndSendTrade("SELL", ep, equityLast);
            logAndSendTrade("SHORT", ep + 1, equityLast + 10.0);
            lastEquity = equityLast;
        }
    }

    private void logAndSendTrade(String action, int ep, Double equity) {
        String logMessage = String.format("%s %d SPY %s", action, ep, equity);
        Logger.log2(logMessage);
        Application.instance.client.send(logMessage);
    }

    @Override
    public void onDataReceived(String jsonString) {
        try {
            Logger.log("Inbound data: " + jsonString);

            if (!Application.instance.isReady) {
                Logger.log("Application isn't ready");
                return;
            }

            JSONObject jsonObject = new JSONObject(jsonString);
            Map<String, Object> jsonData = new HashMap<>();
            jsonData.put("time", jsonObject.getString("time"));
            jsonData.put("epoch", jsonObject.getDouble("epoch"));
            jsonData.put("equityLast", jsonObject.getDouble("equityLast"));
            jsonData.put("AAAA", jsonObject.getDouble("AAAA"));
            jsonData.put("BBBB", jsonObject.getDouble("BBBB"));
            jsonData.put("CCCC", jsonObject.getDouble("CCCC"));
            jsonData.put("TTTT", jsonObject.getDouble("TTTT"));

            ArrayList<Double> features = Feature.parse(jsonData);
            double[] values = features.stream().mapToDouble(Double::doubleValue).toArray();
            System.out.println(features);
            predict(values, jsonObject.getString("time"), jsonObject.getDouble("epoch"), jsonObject.getDouble("equityLast"));
        } catch (Exception e) {
            Logger.log("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
