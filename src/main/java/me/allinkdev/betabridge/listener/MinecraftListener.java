package me.allinkdev.betabridge.listener;

import lombok.RequiredArgsConstructor;
import me.allinkdev.betabridge.Main;
import me.allinkdev.betabridge.Utility;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RequiredArgsConstructor
public class MinecraftListener implements Listener {
    private static final ExecutorService MESSAGE_PROCESSOR = Executors.newCachedThreadPool();
    private static final String PLUGIN_NOT_ACTIVE = "Received %s while plugin isn't active!";
    private static final String PLUGIN_CHANNEL_NULL = "Received a %s while plugin channel is null!";
    private static final String STATE_CHANGE_MESSAGE = Utility.bold("%s %s the game.");
    private static final Logger LOGGER = LoggerFactory.getLogger("BetaBridge Minecraft Listener");
    private final Main plugin;

    private Optional<TextChannel> checkActiveAndGetChannel(final Event event) {
        final Class<? extends Event> eventClass = event.getClass();
        final String className = eventClass.getSimpleName();

        if (plugin.isNotActive()) {
            final String message = String.format(PLUGIN_NOT_ACTIVE, className);
            LOGGER.error(message);

            return Optional.empty();
        }

        final TextChannel channel = plugin.getChannel();

        if (channel == null) {
            final String message = String.format(PLUGIN_CHANNEL_NULL, className);
            LOGGER.error(message);

            return Optional.empty();
        }

        return Optional.of(channel);
    }

    @EventHandler(priority = Event.Priority.Monitor)
    public void onPlayerChat(final PlayerChatEvent event) {
        MESSAGE_PROCESSOR.submit(() -> this.processChat(event));
    }

    private void processChat(final PlayerChatEvent event) {
        final Optional<TextChannel> channelOptional = checkActiveAndGetChannel(event);

        if (channelOptional.isEmpty()) {
            return;
        }

        final TextChannel channel = channelOptional.get();
        final Player player = event.getPlayer();
        final String username = player.getName();
        final String message = event.getMessage();
        final String format = event.getFormat();
        final String chatMessage = String.format(format, username, message);

        Utility.sendMessage(chatMessage, channel, true);
    }

    private void processPlayerStateChange(final PlayerEvent event, final String verb) {
        final Optional<TextChannel> channelOptional = checkActiveAndGetChannel(event);

        if (channelOptional.isEmpty()) {
            return;
        }

        final TextChannel channel = channelOptional.get();
        final Player player = event.getPlayer();
        final String username = player.getName();
        final String formattedUsername = Utility.format(username, true);
        final String stateMessage = String.format(STATE_CHANGE_MESSAGE, formattedUsername, verb);

        Utility.sendMessage(stateMessage, channel, false);
    }

    public void onPlayerStateChange(final PlayerEvent event, final String verb) {
        MESSAGE_PROCESSOR.submit(() -> processPlayerStateChange(event, verb));
    }

    @EventHandler(priority = Event.Priority.Monitor)
    public void onPlayerJoin(final PlayerJoinEvent event) {
        onPlayerStateChange(event, "joined");
    }

    @EventHandler(priority = Event.Priority.Monitor)
    public void onPlayerQuit(final PlayerQuitEvent event) {
        onPlayerStateChange(event, "left");
    }
}
