package com.gmo.discord.matchups.bot.command;

import discord4j.core.object.entity.User;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

public class DiscordMessage {
    private final String text;
    private final byte[] content;
    private final boolean replacePrevious;
    private final User directRecipient;

    private DiscordMessage(final Builder builder) {
        text = builder.text.toString();
        replacePrevious = builder.replacePrevious;
        directRecipient = builder.directRecipient;
        content = builder.content;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public Optional<String> getText() {
        return Optional.ofNullable(text);
    }

    public Optional<User> getDirectRecipient() {
        return Optional.ofNullable(directRecipient);
    }

    public Optional<byte[]> getContent() {
        return Optional.ofNullable(content);
    }

    public boolean isReplacePrevious() {
        return replacePrevious;
    }

    public static Builder newBuilder(final DiscordMessage copy) {
        return DiscordMessage.newBuilder()
                .withText(copy.text)
                .withReplacePrevious(copy.replacePrevious);
    }

    public Collection<DiscordMessage> singleton() {
        return Collections.singleton(this);
    }

    public static final class Builder {
        private StringBuilder text;
        private boolean replacePrevious;
        private User directRecipient;
        private byte[] content;

        private Builder() {
            replacePrevious = false;
            text = new StringBuilder();
        }

        public Builder withText(final String text) {
            this.text = new StringBuilder(text);
            return this;
        }

        public Builder appendText(final String text) {
            this.text.append(text);
            return this;
        }

        public Builder appendNewLine() {
            this.text.append("\n");
            return this;
        }

        public Builder withReplacePrevious(final boolean replacePrevious) {
            this.replacePrevious = replacePrevious;
            return this;
        }

        public Builder withDirectRecipient(final User user) {
            this.directRecipient = user;
            return this;
        }

        public Builder withContent(final byte[] content) {
            this.content = content;
            return this;
        }

        public DiscordMessage build() {
            return new DiscordMessage(this);
        }
    }
}
