package db;

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;

import java.sql.Connection;
import java.sql.SQLException;

public class ConnectToDB implements AutoCloseable {
    private final MysqlDataSource dataSource = new MysqlDataSource();
    private Connection connection;
    private final MysqlDataSource dataSource = new MysqlDataSource();

    public ConnectToDB(final String hostname, final String dbName, final String username, final String password) {
        dataSource.setServerName(hostname);
        dataSource.setUser(username);
        dataSource.setPassword(password);
        dataSource.setDatabaseName(dbName);
    }

    public Connection getConnection() throws SQLException {
        if (this.connection == null || this.connection.isClosed()) {
            this.connection = this.dataSource.getConnection();
        }

        return this.connection;
    }

    @Override
    public void close() throws Exception {
        connection.close();
    }
}
