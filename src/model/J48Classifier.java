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
    private final ArrayList<Attribute> attributes = new ArrayList<>();
    private int lastPosition = 0;
    private int M = 0;
    private Double lastEquity = 0.0;

    public J48Classifier() {
        try {
            Logger.log("Initializing J48 Classifier...", true);
            initialize();
            Logger.log("Initialized J48 Classifier", true);
        } catch (Exception e) {
            Logger.log("Error during initialization: " + e.getMessage(), true);
        }
    }

    private void initialize() throws Exception {
        Instances data = loadData(Config.ARFF_FILE);
        if (data.classIndex() == -1) {
            data.setClassIndex(data.numAttributes() - 1);
        }

        setAttributes(data);

        if (Config.M > 0) {
            loadModel(Config.M);
        }
    }

    private Instances loadData(String filePath) throws Exception {
        DataSource source = new DataSource(filePath);
        return source.getDataSet();
    }

    private Instances applyFilter(Instances data) throws Exception {
        Remove remove = new Remove();
        remove.setOptions(new String[]{"-R", "1,2,3,5,13,38,39,49"});
        remove.setInputFormat(data);
        return Filter.useFilter(data, remove);
    }

    private void setAttributes(Instances data) {
        attributes.clear();
        for (int i = 0; i < data.numAttributes(); i++) {
            attributes.add(data.attribute(i));
        }
    }

    public synchronized void loadModel(int M) throws Exception {
        if (this.M == M) return;

        Logger.log("Loading M" + M + " model...", true);
        Application.instance.setIsReady(false);

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream("data/j48_" + M + ".model"))) {
            tree = (J48) ois.readObject();
            this.M = M;
            Logger.log("Loaded M" + M + " model", true);
        } catch (FileNotFoundException e) {
            Logger.log("Model file not found: " + e.getMessage(), true);
            trainModel(M);
        } finally {
            Application.instance.setIsReady(true);
        }
    }

    public void trainModel(int M) {
        try {
            Logger.log("Training M" + M + " model...", true);
            tree = new J48();
            Instances data = loadData(Config.ARFF_FILE);
            if (data.classIndex() == -1) {
                data.setClassIndex(data.numAttributes() - 1);
            }

            Instances filteredData = applyFilter(data);
            setJ48Options(M);

            Evaluation eval = new Evaluation(filteredData);
            eval.crossValidateModel(tree, filteredData, 10, new Random(1));

            tree.buildClassifier(filteredData);
            saveModel(M);
            Logger.log("Trained M" + M + " model", true);
        } catch (Exception e) {
            Logger.log("Training Error: " + e.getMessage(), true);
        }
    }

    private void setJ48Options(int M) throws Exception {
        String[] options = new String[]{"-C", "0.25", "-M", String.valueOf(M)};
        tree.setOptions(options);
    }

    private void saveModel(int M) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("data/j48_" + M + ".model"))) {
            oos.writeObject(tree);
        }
    }

    public void changeModel(int M) {
        try {
            Logger.log("Changing to M" + M + " model...", true);
            loadModel(M);
        } catch (Exception e) {
            Logger.log("Error changing model: " + e.getMessage(), true);
        }
    }

    public void predict(double[] features, String time, Double epoch, Double equityLast) {
        try {
            Instances data = new Instances("InboundData", attributes, 0);
            if (data.classIndex() == -1) {
                data.setClassIndex(data.numAttributes() - 1);
            }

            data.add(new DenseInstance(1.0, features));

            Instances filteredData = applyFilter(data);

            double prediction = tree.classifyInstance(filteredData.instance(0));
            Logger.log("Prediction: " + prediction, true);

            executeTradeLogic(prediction, epoch, equityLast);
        } catch (Exception e) {
            Logger.log("Prediction Error: " + e.getMessage(), true);
        }
    }

    private void executeTradeLogic(double prediction, Double epoch, Double equityLast) {
        long ep = (long) (epoch / 10000) * 10; // Scale epoch
        if (lastPosition == 0 && prediction == 1) {
            lastPosition = 1;
            logAndSendTrade("COVER", ep, equityLast);
            logAndSendTrade("BUY", ep + 1, equityLast);
        } else if (lastPosition == 1 && prediction == 0) {
            lastPosition = 0;
            logAndSendTrade("SELL", ep, equityLast);
            logAndSendTrade("SHORT", ep + 1, equityLast);
        }
        lastEquity = equityLast;
    }

    private void logAndSendTrade(String action, long ep, Double equity) {
        String logMessage = String.format("%d SPY %s %s %s", ep, action,  lastEquity.toString(), equity.toString());
        Logger.mlog(logMessage, true);
        Application.instance.client.send(logMessage);
    }

    @Override
    public void onDataReceived(String jsonString) {
        try {
            Logger.log("Inbound data: " + jsonString, true);
            if (!Application.instance.isReady) {
                Logger.log("Application isn't ready", true);
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
            Logger.log("Features: " + features.toString(), false);
            predict(features.stream().mapToDouble(Double::doubleValue).toArray(),
                    jsonObject.getString("time"),
                    jsonObject.getDouble("epoch"),
                    jsonObject.getDouble("equityLast"));
        } catch (Exception e) {
            Logger.log("Error processing data: " + e.getMessage(), true);
        }
    }
}
