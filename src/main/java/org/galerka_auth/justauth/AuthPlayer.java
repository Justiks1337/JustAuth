package org.galerka_auth.justauth;

import org.galerka_auth.database_management.DatabaseConnection;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;


public class AuthPlayer {

    final static int TIME_OF_LOGIN_WITHOUT_2AUTH = JustAuth.getInstance().getConfig().getInt("time_without_2auth");

    public Long telegram_id;
    public String username;
    public String ip;
    public int lastAuth;
    public boolean inDatabase;


    public AuthPlayer (String player_name) {
        username = player_name;
        inDatabase = getUserInfo();
    }

    public boolean need2auth () {
        return inDatabase && Instant.now().getEpochSecond() < lastAuth + TIME_OF_LOGIN_WITHOUT_2AUTH;
    }

    public boolean getUserInfo() {
        try (PreparedStatement statement =  DatabaseConnection.getInstance().connection.prepareStatement(
                "SELECT * FROM users WHERE username = ?")) {

            statement.setString(1, username);

            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {

                telegram_id = resultSet.getLong("telegram_id");
                ip = resultSet.getString("ip");
                if (ip == null) {
                    ip = "";
                }
                lastAuth = resultSet.getInt("lastAuth");
                return true;
            }
            return false;

        }
        catch (SQLException e) {
            JustAuth.getInstance().getLogger().warning("Database request failed. Error: " + e.getMessage());
            return false;
        }
    }
}
