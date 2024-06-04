package org.galerka_auth.database_management;


import javax.inject.Singleton;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import org.galerka_auth.justauth.JustAuth;


@Singleton
public class DatabaseConnection {
    private static final String URL = "jdbc:sqlite:" +
            JustAuth.getInstance().getDataFolder() +
            File.separator +
            "database.db";
    private static DatabaseConnection INSTANCE;
    public Connection connection;


    private DatabaseConnection () {
        try {
            connection = DriverManager.getConnection(URL);
            JustAuth.getLog().info("Successful connection to database");
        }
        catch (SQLException e) {
            JustAuth.getLog().warning("Connection failed, error: " + e.getMessage());
        }
        createTables();
    }

    public void createTables() {
        try (Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE IF NOT EXISTS users (" +
                    "username TEXT PRIMARY KEY, " +
                    "telegram_id INTEGER, " +
                    "ip TEXT, " +
                    "lastAuth INTEGER)");
            JustAuth.getLog().info("Tables created.");
        }
        catch (SQLException e){
            JustAuth.getLog().warning("failed connection to database, plugin stopped. Error: " + e.getMessage());
            System.exit(0);
        }
    }

    public static DatabaseConnection getInstance() {
        if(INSTANCE == null) {
            INSTANCE = new DatabaseConnection();
        }
        return INSTANCE;
    }

}
