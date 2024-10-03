package utils;
import config.Config;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Logger {
    private static final String LOG_FILE = Config.LOG_FILE;

    public static void log(String logString) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(LOG_FILE, true))) {
            LocalDateTime currentTime = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String formattedTime = currentTime.format(formatter);
            System.out.println(formattedTime + " " + logString);
            writer.printf("%s, %s%n", formattedTime, logString);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}