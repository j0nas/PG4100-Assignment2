package db;

import main.Book;
import main.Config;
import util.Log;

import java.sql.ResultSet;
import java.util.ArrayList;

public class Db {
    public static ArrayList<Book> fetchAndParseBooks() {
        ArrayList<Book> listToFill = new ArrayList<>();
        try (ConnectToDB dbConnection = new ConnectToDB(Config.DB_HOST, Config.DB_NAME, Config.DB_USER, Config.DB_PASS)) {
            ResultSet result = dbConnection
                    .getConnection()
                    .prepareStatement("SELECT * FROM " + Config.DB_TABLE_NAME)
                    .executeQuery();

            while (result.next()) {
                listToFill.add(new Book(
                        result.getInt(result.findColumn("id")),
                        result.getString(result.findColumn("author")),
                        result.getString(result.findColumn("title")),
                        result.getString(result.findColumn("ISBN")),
                        result.getInt(result.findColumn("pages")),
                        result.getInt(result.findColumn("released"))
                ));
            }
        } catch (Exception e) {
            Log.e("Error upon connecting to database: " + e.getMessage());
        }

        return listToFill;
    }
}
