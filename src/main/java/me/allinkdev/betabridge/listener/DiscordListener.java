package me.allinkdev.betabridge.listener;

import lombok.RequiredArgsConstructor;
import me.allinkdev.betabridge.Main;
import me.allinkdev.betabridge.Utility;
import me.allinkdev.betabridge.message.Component;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.minecraft.server.MinecraftServer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class DiscordListener extends ListenerAdapter {
    private static final String ONLINE_PLAYERS = "Online players (%d/%d): %s";
    private static final Logger LOGGER = LoggerFactory.getLogger("BetaBridge Discord Listener");
    private final Main plugin;

    private String getOnlinePlayers() {
        final Player[] onlinePlayers = Bukkit.getOnlinePlayers();
        final int maxPlayers = Bukkit.getMaxPlayers();

        return String.format(ONLINE_PLAYERS, onlinePlayers.length, maxPlayers, Arrays.stream(onlinePlayers)
                .map(Player::getName)
                .collect(Collectors.joining(", "))
        );
    }

    @Override
    public void onMessageReceived(final @NotNull MessageReceivedEvent event) {
        if (plugin.isNotActive()) {
            LOGGER.error("Received message while the plugin wasn't active!");
            return;
        }

        final TextChannel pluginChannel = plugin.getChannel();

        if (pluginChannel == null) {
            LOGGER.error("Received a message while the plugin channel is null!");
            return;
        }

        final MessageChannelUnion eventChannel = event.getChannel();
        final String pluginChannelId = pluginChannel.getId();
        final String eventChannelId = eventChannel.getId();

        if (!pluginChannelId.equals(eventChannelId)) {
            return;
        }

        final Message message = event.getMessage();
        final User author = event.getAuthor();

        if (author.isBot()) {
            return;
        }

        if (author.isSystem()) {
            return;
        }

        final String name = author.getName();
        final String formattedName = Utility.format(name, false);
        final String content = message.getContentStripped();

        if (content.equalsIgnoreCase("!list")) {
            final String onlinePlayers = getOnlinePlayers();
            final String formattedOnlinePlayers = Utility.format(onlinePlayers, true);
            final String boldOnlinePlayers = Utility.bold(formattedOnlinePlayers);

            Utility.sendMessage(boldOnlinePlayers, pluginChannel, false);
            return;
        }

        final String contentFormatted = Utility.format(content, false);

        if (contentFormatted.isEmpty()) {
            return;
        }

        final String[] lines = contentFormatted.split("\n");

        final Component component = Component.text("<")
                .append(Component.text(formattedName, ChatColor.DARK_AQUA))
                .append(Component.text("> "))
                .append(Component.text(lines[0]));

        final String text = component.toString();

        Bukkit.broadcastMessage(text);

        final Server server = Bukkit.getServer();
        final CraftServer craftServer = (CraftServer) server;
        final MinecraftServer minecraftServer = craftServer.getServer();

        minecraftServer.console.sendMessage(text);

        for (int i = 1; i < lines.length; i++) {
            final String line = lines[i];

            Bukkit.broadcastMessage(line);
            minecraftServer.console.sendMessage(line);
        }
    }
}
