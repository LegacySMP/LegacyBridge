package me.allinkdev.legacylib;

import lombok.Getter;
import me.allinkdev.legacylib.listener.DiscordListener;
import me.allinkdev.legacylib.listener.MinecraftListener;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pro.nocom.legacysmp.legacylib.LegacyLib;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class Main extends JavaPlugin {
    private static final Logger LOGGER = LoggerFactory.getLogger("BetaBridge");
    private final DiscordListener discordListener = new DiscordListener(this);
    private JDA jda;
    @Nullable
    @Getter
    private TextChannel channel;
    private boolean active = false;
    private File configFile;
    private Path configPath;

    @Override
    public void onDisable() {
        if (jda == null) {
            return;
        }

        if (!active) {
            return;
        }

        jda.removeEventListener(discordListener);
        LOGGER.info("Removed Discord event listeners!");
    }

    @Override
    public void onLoad() {
        final File dataFolder = getDataFolder();

        if (!dataFolder.exists()) {
            final boolean successful = dataFolder.mkdirs();

            if (!successful) {
                LOGGER.error("Failed to create plugin data folder!");
                return;
            }
        }


        final Path dataPath = dataFolder.toPath();

        configPath = dataPath.resolve("config.yml");
        configFile = configPath.toFile();
    }

    @Override
    public void onEnable() {
        if (!configFile.exists()) {
            try {
                copyDefaultConfiguration();
            } catch (IOException e) {
                LOGGER.error("Failed to copy default configuration!", e);
                return;
            }
        }

        final FileConfiguration configuration = getConfig();

        try {
            configuration.load(configFile);
        } catch (Exception e) {
            LOGGER.error("Failed to load configuration!", e);
        }

        try {
            configuration.save(configFile);
        } catch (IOException e) {
            LOGGER.error("Failed to save configuration!", e);
        }

        final String token = configuration.getString("token");
        jda = JDABuilder.createLight(token)
                .enableIntents(GatewayIntent.GUILD_MESSAGES, GatewayIntent.MESSAGE_CONTENT)
                .build();

        try {
            jda.awaitReady();
        } catch (Exception e) {
            LOGGER.error("Could not log into Discord!", e);

            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }

            return;
        }

        final String channelId = configuration.getString("channel");
        channel = jda.getTextChannelById(channelId);

        if (channel == null) {
            LOGGER.error("Could not find configured text channel {}. Have you specified the correct channel, and is it a Text Channel (not a Voice Channel, News Channel or anything else)?", channelId);
            return;
        }

        active = true;

        jda.addEventListener(discordListener);

        final PluginManager pluginManager = Bukkit.getPluginManager();
        final MinecraftListener minecraftListener = new MinecraftListener(this);

        pluginManager.registerEvents(minecraftListener, this);

        final LegacyLib legacyLib = LegacyLib.getInstance();

        if (legacyLib == null) {
            throw new IllegalStateException("Plugin loaded without LegacyLib being active!");
        }

        legacyLib.register(minecraftListener);
    }

    public boolean isNotActive() {
        return !active || !isEnabled();
    }

    private void copyDefaultConfiguration() throws IOException, IllegalStateException {
        final ClassLoader classLoader = getClassLoader();
        final InputStream configurationStream = classLoader.getResourceAsStream("config.yml");

        if (configurationStream == null) {
            throw new IllegalStateException("Default configuration is null!");
        }

        Files.copy(configurationStream, configPath);
    }
}
