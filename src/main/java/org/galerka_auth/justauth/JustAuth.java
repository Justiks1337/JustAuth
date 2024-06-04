package org.galerka_auth.justauth;

import java.io.File;
import java.util.logging.Logger;

import org.bukkit.plugin.java.JavaPlugin;

import org.galerka_auth.database_management.DatabaseConnection;
import org.galerka_auth.telegram.TelegramBot;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public final class JustAuth extends JavaPlugin {

    private static JustAuth instance;
    private static Logger logger;
    private TelegramBot telegramBotInstance;

    @Override
    public void onEnable() {
        instance = this;
        createPluginFolder();
        DatabaseConnection.getInstance();
        getServer().getPluginManager().registerEvents(new AuthMeHandler(this), this);
        getServer().getPluginManager().registerEvents(new AuthChecker(), this);
        telegramBotStarter();

    }

    @Override
    public void onDisable() {
        logger.info("plugin successful stopped.");
    }

    private void createPluginFolder() {
        File pluginFolder = getDataFolder();

        if (!pluginFolder.exists()) {
            if (pluginFolder.mkdirs()) {
                getLogger().info("Папка плагина успешно создана.");
            } else {
                getLogger().severe("Не удалось создать папку плагина.");
            }
        }
    }

    public static JustAuth getInstance(){
        return instance;
    }

    public static Logger getLog() {
        if (logger == null) {
            logger = JustAuth.getInstance().getLogger();
        }
        return logger;
    }

    public void telegramBotStarter () {
        Thread thread =  new Thread(() -> {
            TelegramBotsLongPollingApplication botsApplication = new TelegramBotsLongPollingApplication();

            telegramBotInstance = TelegramBot.getInstance();

            try {
                String botToken = "7406912208:AAH3RaHsedJaN9ILCwennRLWIh4XPVlzkqI";
                botsApplication.registerBot(botToken, telegramBotInstance);
                getLogger().info("Check bot status...");
                if (!botsApplication.isRunning()) {
                    throw new TelegramApiException();
                }

            } catch (TelegramApiException e) {
                this.getLogger().warning("Bot start is failed");
                throw new RuntimeException(e);
            }
        });

        thread.start();
        this.getLogger().info("Telegram bot successful started");
    }
}