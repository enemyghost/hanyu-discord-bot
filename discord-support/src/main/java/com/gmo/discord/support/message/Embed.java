package com.gmo.discord.support.message;

import com.google.common.base.Strings;
import discord4j.discordjson.json.EmbedData;
import discord4j.discordjson.json.ImmutableEmbedData;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.StringJoiner;

/**
 * Metadata for a Discord Embed message
 */
public class Embed {
    private final String title;
    private final Color color;
    private final List<String> content;

    private Embed(final Builder builder) {
        title = builder.title;
        color = builder.color;
        content = List.copyOf(builder.content);
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public Optional<String> getTitle() {
        return Optional.ofNullable(title);
    }

    public Optional<Color> getColor() {
        return Optional.ofNullable(color);
    }

    public List<String> getContent() {
        return content;
    }

    public EmbedData toEmbedData() {
        final ImmutableEmbedData.Builder builder = EmbedData.builder();
        getTitle().ifPresent(builder::title);
        getColor().map(Color::getDiscordValue).ifPresent(builder::color);
        final String join = String.join("\n", getContent());
        if (!Strings.isNullOrEmpty(join)) {
            builder.description(join);
        }
        return builder.build();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final Embed embed = (Embed) o;
        return Objects.equals(title, embed.title) &&
                Objects.equals(color, embed.color) &&
                Objects.equals(content, embed.content);
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, color, content);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Embed.class.getSimpleName() + "[", "]")
                .add("title='" + title + "'")
                .add("color=" + color)
                .add("content=" + content)
                .toString();
    }

    public static final class Builder {
        private String title;
        private Color color;
        private List<String> content;

        private Builder() {
            content = new ArrayList<>();
        }

        public Builder withTitle(final String val) {
            title = val;
            return this;
        }

        public Builder withColor(final Color val) {
            color = val;
            return this;
        }

        public Builder addContent(final String val) {
            if (val != null) {
                content.add(val);
            }
            return this;
        }

        public Builder withContent(final List<String> val) {
            if (val != null) {
                content = new ArrayList<>(val);
            }
            return this;
        }

        public Embed build() {
            return new Embed(this);
        }
    }
}
