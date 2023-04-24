package me.allinkdev.legacybridge.listener;

import lombok.RequiredArgsConstructor;
import me.allinkdev.legacybridge.Main;
import me.allinkdev.legacybridge.Utility;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;

import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RequiredArgsConstructor
public class MinecraftListener implements Listener {
    private static final ExecutorService PROCESSOR = Executors.newCachedThreadPool();
    private static final String STATE_CHANGE_MESSAGE = Utility.bold("%s %s the game.");
    private final Main plugin;

    private Optional<TextChannel> checkActiveAndGetChannel() {
        if (plugin.isNotActive()) {
            return Optional.empty();
        }

        final TextChannel channel = plugin.getChannel();

        return Optional.ofNullable(channel);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerChat(final AsyncPlayerChatEvent event) {
        final Optional<TextChannel> channelOptional = checkActiveAndGetChannel();

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
        final Optional<TextChannel> channelOptional = checkActiveAndGetChannel();

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
        PROCESSOR.submit(() -> processPlayerStateChange(event, verb));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(final PlayerJoinEvent event) {
        onPlayerStateChange(event, "joined");
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(final PlayerQuitEvent event) {
        onPlayerStateChange(event, "left");
    }
}
