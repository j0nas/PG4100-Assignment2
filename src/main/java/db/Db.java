package db;

import config.Config;
import models.Book;
import org.apache.logging.log4j.LogManager;

import java.sql.ResultSet;
import java.util.ArrayList;

public class Db {
    protected static ArrayList<Book> fetchAndParseBooks() {
        ArrayList<Book> listToFill = new ArrayList<>();
        try (ConnectToDB dbConnection = new ConnectToDB(Config.DB_HOST, Config.DB_NAME, Config.DB_USER, Config.DB_PASS)) {
            ResultSet result = dbConnection
                    .getConnection()
                    .prepareStatement("SELECT * FROM " + Config.DB_TABLE_NAME)
                    .executeQuery();

            String[] authorSplit;
            String firstName;
            String lastName;
            while (result.next()) {
                authorSplit = result.getString(result.findColumn("author")).split(",");
                firstName = authorSplit[1].trim();
                lastName = authorSplit[0] != null ? authorSplit[0].trim() : "";
                listToFill.add(new Book(
                        result.getInt(result.findColumn("id")),
                        firstName,
                        lastName,
                        result.getString(result.findColumn("title")),
                        result.getString(result.findColumn("ISBN")),
                        result.getInt(result.findColumn("pages")),
                        result.getInt(result.findColumn("released"))
                ));
            }
        } catch (Exception e) {
            LogManager.getLogger(Config.LOG_SERVER).error("Error upon connecting to database: " + e.getMessage());
        }

        return listToFill;
    }
}
