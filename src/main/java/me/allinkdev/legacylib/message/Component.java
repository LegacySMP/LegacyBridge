package me.allinkdev.legacylib.message;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.ChatColor;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

@Getter
public class Component {
    private final String content;
    private final List<Component> children = new ArrayList<>();
    @Nullable
    @Setter
    private ChatColor color = ChatColor.WHITE;

    Component(final String content) {
        this.content = content;
    }

    Component(final String content, @Nullable final ChatColor color) {
        this.content = content;
        this.color = color;
    }

    public static Component text(final String message) {
        return new Component(message);
    }

    public static Component text(final String message, final ChatColor chatColor) {
        return new Component(message, chatColor);
    }

    @Override
    public String toString() {
        final StringBuilder messageBuilder = new StringBuilder();
        final String colorString = color == null ? ChatColor.WHITE.toString() : color.toString();
        final String me = colorString + content;
        messageBuilder.append(me);

        for (final Component child : children) {
            messageBuilder.append(child.toString());
        }

        return messageBuilder.toString();
    }

    public Component append(final Component child) {
        this.children.add(child);

        return this;
    }
}
