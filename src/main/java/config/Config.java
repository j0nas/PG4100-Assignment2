package config;

public class Config {
    public static final String DB_USER = "root";
    public static final String DB_PASS = "";
    public static final String DB_HOST = "localhost";

    public static final String DB_NAME = "pg4100innlevering2";
    public static final String DB_TABLE_NAME = "bokliste";
    public static final String LOG_CLIENT = "no.wact.jenjon13.ClientLog";
    public static final String LOG_SERVER = "no.wact.jenjon13.ServerLog";
    public static int SERVER_PORT = 3000;

    private Config() {
    }
}
