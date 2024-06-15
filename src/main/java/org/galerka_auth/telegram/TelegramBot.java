package org.galerka_auth.telegram;

import fr.xephi.authme.api.v3.AuthMeApi;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.galerka_auth.database_management.DatabaseConnection;
import org.galerka_auth.justauth.AuthMeHandler;
import org.galerka_auth.justauth.AuthPlayer;
import org.galerka_auth.justauth.JustAuth;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.concurrent.Executor;


public class TelegramBot implements LongPollingSingleThreadUpdateConsumer {

    private final Executor mainThread;
    private final TelegramClient telegramClient;
    @Getter(lazy = true)
    private static final TelegramBot instance = new TelegramBot(JustAuth.getInstance().getConfig().getString("telegram_bot_token"));

    @Override
    public void consume(Update update) {

        if (update.hasMessage() && update.getMessage().hasText()) {
            handleCommand(update);
        } else if (update.hasCallbackQuery()) {
            handleCallbackQuery(update);
        }
    }

    public TelegramBot(String token) {
        super();
        this.mainThread = Bukkit.getScheduler().getMainThreadExecutor(JustAuth.getInstance());
        telegramClient = new OkHttpTelegramClient(token);
    }

    private void handleCommand(Update update) {
        Message message = update.getMessage();
        String[] split_command = message.getText().split(" ");
        String command = split_command[0];

        switch (command) {
            case "/start" -> startCommand(message);
            case "/2auth" -> twoAuthCommand(message, split_command);
        }
    }

    private void handleCallbackQuery(Update update) {
        String call_data = update.getCallbackQuery().getData();
        Long chat_id = update.getCallbackQuery().getMessage().getChatId();

        try {
            switch (call_data) {
                case "yes" -> telegramClient.executeAsync(new SendMessage(chat_id.toString(), JustAuth.getInstance().getConfig().getString("ok_button_response"))).thenRunAsync(
                        () -> {
                            Player player = Bukkit.getPlayer(getPlayerName(chat_id));
                            player.getPersistentDataContainer().remove(AuthMeHandler.TAG_KEY);
                            player.sendMessage(JustAuth.getInstance().getConfig().getString("successful_authorization"));
                        }, mainThread);
                case "no" ->
                        telegramClient.executeAsync(new SendMessage(chat_id.toString(), JustAuth.getInstance().getConfig().getString("no_button_response"))).thenRunAsync(
                                () -> {
                                    Player player = Bukkit.getPlayer(getPlayerName(chat_id));
                                    player.kick();
                                }, mainThread);
            }
        } catch (TelegramApiException e) {
            JustAuth.getInstance().getLogger().warning(e.getMessage());
        }
    }


    private void startCommand(Message message) {
        try {
            telegramClient.executeAsync(new SendMessage(message.getChatId().toString(), JustAuth.getInstance().getConfig().getString("start_command")
                    ));
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    private void twoAuthCommand(Message message, String[] args) {
        try {

            if (args.length < 3) {
                telegramClient.executeAsync(new SendMessage(message.getChatId().toString(), JustAuth.getInstance().getConfig().getString("not_enough_arguments")));
            }

            String nickname = args[1];
            String password = args[2];

            if (AuthMeApi.getInstance().checkPassword(nickname, password)) {
                PreparedStatement statement = DatabaseConnection.getInstance().connection.prepareStatement(
                        "INSERT INTO users VALUES (?, ?, ?, ?)");
                statement.setString(1, nickname);
                statement.setLong(2, message.getChat().getId());
                statement.setString(3, "");
                statement.setLong(4, Instant.now().getEpochSecond());
                statement.execute();

                telegramClient.executeAsync(new SendMessage(message.getChatId().toString(), JustAuth.getInstance().getConfig().getString("on_success_adding_to_database")));

            } else {
                telegramClient.executeAsync(new SendMessage(message.getChatId().toString(), JustAuth.getInstance().getConfig().getString("on_failed_adding_to_database")));
            }

        } catch (TelegramApiException | SQLException exception) {
            JustAuth.getInstance().getLogger().warning(exception.getMessage());
        }
    }

    private String getPlayerName(Long chat_id) {
        try (PreparedStatement preparedStatement = DatabaseConnection.getInstance().connection.prepareStatement(
                "SELECT * FROM users WHERE telegram_id = ?")) {

            preparedStatement.setLong(1, chat_id);
            ResultSet resultSet = preparedStatement.executeQuery();
            return resultSet.getString("username");

        } catch (SQLException e) {
            JustAuth.getInstance().getLogger().warning(e.getMessage());
            return " ";
        }
    }

    public void sendAuthMessage(AuthPlayer player) {
        try {
            this.telegramClient.executeAsync(createAuthMessage(player.telegram_id, player.ip));
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    private SendMessage createAuthMessage(Long telegramId, String ip) {
        SendMessage message = new SendMessage(telegramId.toString(), JustAuth.getInstance().getConfig().getString("on_attempt").replace("{ip}", ip));
        InlineKeyboardMarkup.InlineKeyboardMarkupBuilder builder = InlineKeyboardMarkup.builder();
        InlineKeyboardRow row = new InlineKeyboardRow();
        InlineKeyboardButton ok_button = new InlineKeyboardButton(JustAuth.getInstance().getConfig().getString("ok_button_title"));
        ok_button.setCallbackData("yes");
        InlineKeyboardButton no_button = new InlineKeyboardButton(JustAuth.getInstance().getConfig().getString("no_button_title"));
        no_button.setCallbackData("no");
        row.add(ok_button);
        row.add(no_button);
        builder.keyboardRow(row);
        message.setReplyMarkup(builder.build());
        return message;
    }

    public static void telegramBotStarter() {
        Thread thread = new Thread(() -> {
            try {
                TelegramBotsLongPollingApplication botsApplication = new TelegramBotsLongPollingApplication();

                String botToken = JustAuth.getInstance().getConfig().getString("telegram_bot_token");
                 botsApplication.registerBot(botToken, TelegramBot.getInstance());
                JustAuth.getInstance().getLogger().info("Check bot status...");
                if (!botsApplication.isRunning()) {
                    throw new TelegramApiException();
                }

            } catch (TelegramApiException e) {
                JustAuth.getInstance().getLogger().warning("Bot start is failed");
                throw new RuntimeException(e);
            }
        });

        thread.start();
        JustAuth.getInstance().getLogger().info("Telegram bot successful started");
    }

}
