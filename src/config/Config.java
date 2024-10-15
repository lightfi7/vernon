package config;

public class Config {
    /* APP*/
       public static String APP_NAME = "Vernon";
    /* END APP*/

    /* MODEL*/
    public static int M = 2;
    public static String ARFF_FILE = "data/vernon.arff";
    public static String OUTPUT_FILE = "data/output.csv";
    /* END MODEL*/

    /* SOCKET
    * */
    public static String HOST = "localhost";
    public static int PORT = 80;
    public static String ENDPOINT = "http://3.145.21.56:8887";
//    public static String ENDPOINT = "http://127.0.0.1:8887";

    public static String SELL_CMD = "Sell";
    public static String BUY_CMD = "Buy";
    public static String SHORT_CMD = "Short";
    public static String COVER_CMD = "Cover";

    /* END SOCKET*/

    /* LOGGER
    * */
    public static String LOG_FILE = "data/log.ini";

    /* END LOGGER*/
}

