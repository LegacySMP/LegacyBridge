package me.allinkdev.betabridge;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.exceptions.RateLimitedException;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Set;

public final class Utility {
    private static final Logger LOGGER = LoggerFactory.getLogger("BetaBridge Utility");
    private static final String SPECIAL_CHARACTERS = "([*_`|/\\\\])";
    private static final String TO_DESTROY = "§.|§";
    private static final Set<Message.MentionType> ALLOWED_MENTIONS = Collections.emptySet();

    private Utility() {
        throw new UnsupportedOperationException("Utility class; cannot be instantiated.");
    }

    public static String format(final String original, final boolean escapeSpecial) {
        return original.replaceAll(SPECIAL_CHARACTERS, escapeSpecial ? "\\\\$1" : "$1")
                .replaceAll(TO_DESTROY, "");
    }

    public static String bold(final String message) {
        return "**" + message + "**";
    }

    public static <T> void completeAction(final RestAction<T> action) {
        try {
            action.complete(true);
        } catch (RateLimitedException e) {
            LOGGER.error("Ratelimited!", e);
        }
    }

    public static MessageCreateData constructMessage(final String content, final boolean escapeSpecial) {
        final String formatted = format(content, escapeSpecial);

        return new MessageCreateBuilder()
                .setContent(formatted)
                .setAllowedMentions(ALLOWED_MENTIONS)
                .build();
    }

    public static void sendMessage(final String content, @NotNull final TextChannel channel, final boolean escapeSpecial) {
        final MessageCreateData data = constructMessage(content, escapeSpecial);
        final MessageCreateAction createAction = channel.sendMessage(data);

        completeAction(createAction);
    }
}
