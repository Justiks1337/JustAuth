package org.galerka_auth.justauth;

import fr.xephi.authme.events.LoginEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.persistence.PersistentDataType;
import org.galerka_auth.database_management.DatabaseConnection;
import org.galerka_auth.telegram.TelegramBot;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;


public class AuthMeHandler implements Listener {

    public static final NamespacedKey TAG_KEY = new NamespacedKey("justauth", "noauth");
    public static final NamespacedKey TIME_TO_KICK_KEY = new NamespacedKey("justauth", "timetokick");
    
    @EventHandler
    public void onPlayerAuth(LoginEvent event) {
        Player player = event.getPlayer();
        AuthPlayer user = new AuthPlayer(player.getName());

        if (user.ip.isEmpty()) {
            try (PreparedStatement preparedStatement = DatabaseConnection.getInstance().connection.prepareStatement("UPDATE users SET ip = ? WHERE username = ?")){
                preparedStatement.setString(1, player.getAddress().toString());
                preparedStatement.setString(2, user.username);
                preparedStatement.execute();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        if (!user.need2auth() && !user.ip.equals(player.getAddress().toString())) {
            return;}

        player.getPersistentDataContainer().set(TAG_KEY, PersistentDataType.STRING, "noauth");
        player.getPersistentDataContainer().set(TIME_TO_KICK_KEY, PersistentDataType.LONG, Instant.now().getEpochSecond());
        sendTitle(player);
        TelegramBot.getInstance().sendAuthMessage(user);

    }

    private void sendTitle(Player player) {
        Title.Times times = Title.Times.times(Duration.ofMillis(10 * 50), Duration.ofMillis(70 * 50), Duration.ofMillis(20 * 50));
        Title authTitle = Title.title(
                Component.text(JustAuth.getInstance().getConfig().getString("title")),
                Component.text(JustAuth.getInstance().getConfig().getString("subtitle")),
                times
        );
        player.showTitle(authTitle);
    }

}
