import assignment.Book;
import assignment.Config;
import assignment.ConnectToDB;
import assignment.Server;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DbTest {
    private ConnectToDB dbConnection;

    @Before
    public void testConnectToDB() throws Exception {
        dbConnection = new ConnectToDB(Config.DB_HOST, Config.DB_NAME, Config.DB_USER, Config.DB_PASS);
    }

    @Test
    public void testGetAllRowsFromDb() throws SQLException {
        ResultSet result = dbConnection.getConnection().prepareStatement("SELECT * FROM " + Config.DB_TABLE_NAME).executeQuery();

        ResultSetMetaData resultMeta = result.getMetaData();
        Assert.assertTrue("The number of returned rows should be greater than zero.",
                resultMeta.getColumnCount() > 0);

        while (result.next()) {
            for (int i = 1; i <= resultMeta.getColumnCount(); i++) {
                System.out.printf("%s%s: %s", i > 1 ? ", " : "", resultMeta.getColumnName(i), result.getString(i));
            }

            System.out.println();
        }
    }

    @Test
    public void testCheckAllParsedBooks() {
        List<Book> books = new ArrayList<>();
        Server.fetchAndParseQuizContent(books);
        Assert.assertTrue("The list should now contain at least one Book.", books.size() > 0);

        books.forEach(System.out::println);
        System.out.println("Elements held: " + books.size());
    }

    @After
    public void closeConnection() throws Exception {
        if (dbConnection != null && !dbConnection.getConnection().isClosed()) {
            dbConnection.close();
        }
    }
}
