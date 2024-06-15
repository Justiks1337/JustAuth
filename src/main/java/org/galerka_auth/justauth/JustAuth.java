package org.galerka_auth.justauth;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.galerka_auth.telegram.TelegramBot;

import java.io.File;


@NoArgsConstructor
public final class JustAuth extends JavaPlugin {

    @Getter()
    private static JustAuth instance;

    @Override
    public void onEnable() {
        instance = this;
        createPluginFolder();
        getServer().getPluginManager().registerEvents(new AuthMeHandler(), this);
        getServer().getPluginManager().registerEvents(new AuthChecker(), this);
        loadConfig();
        TelegramBot.telegramBotStarter();
    }

    @Override
    public void onDisable() {
        getLogger().info("plugin successful stopped.");
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

    private void loadConfig() {
        File file = new File(JustAuth.getInstance().getDataFolder() + File.separator + "config.yml");
        if (!file.exists()) {
            JustAuth.getInstance().getDataFolder().mkdirs();
            JustAuth.getInstance().saveResource("config.yml", true);
        }
        YamlConfiguration.loadConfiguration(file);
    }


}
