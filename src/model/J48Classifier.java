package model;

import app.Application;
import config.Config;
import utils.EventHandler;
import utils.Logger;
import weka.classifiers.Evaluation;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;
import weka.classifiers.trees.J48;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;

import org.json.JSONObject;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class J48Classifier implements EventHandler {

    private static J48 tree = null;
    private static Evaluation eval = null;
    private int lastPosition = 0;
    private int M = 0;
    private Double lastEquity = 0.0;

    private static final String MODEL_PATH_TEMPLATE = "data/j48_%d.model";
    private static final String[] REMOVE_OPTIONS = {"-R","1,2,3,5,13,38,39,49"};

    public J48Classifier() {
        try {
            Logger.log("Initializing J48 Classifier...");
            loadModel(Config.M);
            Logger.log("Initialized J48 Classifier");
        } catch (Exception e) {
            logError(e);
        }
    }

    private void loadModel(int M) throws Exception {
        if (this.M == M) return;

        Logger.log("Loading M" + M + " model...");
        Application.instance.setIsReady(false);
        tree = deserializeModel(M);

        Instances data = loadAndFilterData();
        eval = new Evaluation(data);
        eval.crossValidateModel(tree, data, 10, new Random(1));
        printCrossValidationResults();

        this.M = M;
        Logger.log("Loaded M" + M + " model");
        Application.instance.setIsReady(true);
    }

    private J48 deserializeModel(int M) throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(String.format(MODEL_PATH_TEMPLATE, M)))) {
            return (J48) ois.readObject();
        }
    }

    private Instances loadAndFilterData() throws Exception {
        DataSource source = new DataSource(Config.ARFF_FILE);
        Instances data = source.getDataSet();

        if (data.classIndex() == -1) {
            data.setClassIndex(data.numAttributes() - 1);
        }

        Remove remove = new Remove();
        remove.setOptions(REMOVE_OPTIONS);
        remove.setInputFormat(data);
        return Filter.useFilter(data, remove);
    }

    private void printCrossValidationResults() {
        System.out.println(eval.toSummaryString("\n10-Fold Cross-Validation Results\n======\n", false));
    }

    public void train(int M) {
        try {
            Logger.log("Training M" + M + " model...");
            tree = new J48();
            Instances data = loadAndFilterData();
            setJ48Options(M);
            tree.buildClassifier(data);
            serializeModel(M);
            Logger.log("Trained M" + M + " model");
        } catch (Exception e) {
            logError(e);
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
                throw new IllegalArgumentException("Invalid M value: " + M);
        }
        tree.setOptions(options);
    }

    private void serializeModel(int M) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(String.format(MODEL_PATH_TEMPLATE, M)))) {
            oos.writeObject(tree);
        }
    }

    public void change(int M) {
        try {
            Logger.log("Changing to M" + M + " model...");
            Application.instance.setIsReady(false);
            loadModel(M);
            Logger.log("Changed to M" + M + " model");
            Application.instance.setIsReady(true);
        } catch (Exception e) {
            logError(e);
        }
    }

    public void predict(double[] features, String time, Double epoch, Double equityLast) {
        try {
            Instances data = new Instances("InboundInstance", createAttributes(), 0);
            data.add(new DenseInstance(1.0, features));

            if (data.classIndex() == -1) {
                data.setClassIndex(data.numAttributes() - 1);
            }

            Remove remove = new Remove();
            remove.setOptions(new String[]{"-R", "1,2,3,5,14,38,39,49"});
            remove.setInputFormat(data);
            Instances filteredData = Filter.useFilter(data, remove);

            double prediction = tree.classifyInstance(filteredData.instance(0));
            handlePrediction(prediction, epoch, equityLast);
        } catch (Exception e) {
            logError(e);
        }
    }

    private ArrayList<Attribute> createAttributes() {
        return new ArrayList<>(Arrays.asList(/* Define attributes here */));
    }

    private void handlePrediction(double prediction, Double epoch, Double equityLast) {
        int ep = epoch.intValue() / 10000 * 10;

        if (lastPosition == 0 && prediction == 1) {
            lastPosition = 1;
            logAndSendTradeActions("COVER", ep, equityLast);
        } else if (lastPosition == 1 && prediction == 0) {
            lastPosition = 0;
            logAndSendTradeActions("SELL", ep, equityLast);
        }
        lastEquity = equityLast;
    }

    private void logAndSendTradeActions(String action, int ep, Double equityLast) {
        Logger.log2(String.format("%s %d SPY %s", action, ep, equityLast));
        Application.instance.client.send(String.format("%s %d SPY %s", action, ep, equityLast));
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
            predict(values, jsonObject.getString("time"), jsonObject.getDouble("epoch"), jsonObject.getDouble("equityLast"));

        } catch (Exception e) {
            logError(e);
        }
    }

    private void logError(Exception e) {
        e.printStackTrace();
        Logger.log("Error: " + e.getMessage());
    }
}
