package utils;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class Logger {
    private static final String LOG_FILE = "data/log.csv";

    public static void log(String data, String decision) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(LOG_FILE, true))) {
            writer.printf("%s,%s%n", data, decision);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
