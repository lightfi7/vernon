package utils;
import dialog.MainDialog;
import config.Config;
import weka.core.pmml.jaxbbindings.True;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Logger {
    private static final String LOG_FILE = Config.LOG_FILE;
    private static final String M_LOG_FILE = Config.M_LOG_FILE;


    public static void log(String logString, Boolean visible) {
        if(visible)
            MainDialog.instance.addLogMessage(logString);
        try (PrintWriter writer = new PrintWriter(new FileWriter(LOG_FILE, true))) {
            LocalDateTime currentTime = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String formattedTime = currentTime.format(formatter);
            System.out.println(formattedTime + " " + logString);
            writer.printf("%s %s%n", formattedTime, logString);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void mlog(String logString, Boolean visible) {
        MainDialog.instance.addLogMessage(logString);
        try (PrintWriter writer = new PrintWriter(new FileWriter(M_LOG_FILE, true))) {
            LocalDateTime currentTime = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String formattedTime = currentTime.format(formatter);
            System.out.println(formattedTime + " " + logString);
            writer.printf("%s%n",logString);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}