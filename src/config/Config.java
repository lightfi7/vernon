package config;

public class Config {
    /* APP*/
       public static String APP_NAME = "Vernon";
    /* END APP*/

    /* MODEL*/
    public static String ARFF_FILE = "data/vernon.arff";
    public static String OUTPUT_FILE = "data/output.csv";
    /* END MODEL*/

    /* SOCKET
    * */
    public static String HOST = "localhost";
    public static int PORT = 8777;
    public static String ENDPOINT = "ws:localhost:8778";

    /* END SOCKET*/

    /* LOGGER
    * */
    public static String LOG_FILE = "data/log.ini";

    /* END LOGGER*/
}

