package com.gmo.discord.support.message;

import java.util.Collections;
import java.util.Optional;

import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.handle.obj.IUser;

/**
 * @author tedelen
 */
public class DiscordMessage {
    private final String text;
    private final EmbedObject embedObject;
    private final byte[] content;
    private final boolean replacePrevious;
    private final IUser directRecipient;

    private DiscordMessage(final Builder builder) {
        embedObject = builder.embedObject;
        text = embedObject == null ? builder.text : null;
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

    public Optional<EmbedObject> getEmbedObject() {
        return Optional.ofNullable(embedObject);
    }

    public Optional<IUser> getDirectRecipient() {
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
                .withReplacePrevious(copy.replacePrevious)
                .withEmbedObject(copy.embedObject);
    }

    public Iterable<DiscordMessage> singleton() {
        return Collections.singleton(this);
    }

    public static final class Builder {
        private String text;
        private EmbedObject embedObject;
        private boolean replacePrevious;
        private IUser directRecipient;
        private byte[] content;

        private Builder() {
            replacePrevious = false;
        }

        public Builder withText(final String text) {
            this.text = text;
            return this;
        }

        public Builder appendText(final String text) {
            this.text = this.text == null ? text : (this.text + text);
            return this;
        }

        public Builder withEmbedObject(final EmbedObject embedObject) {
            this.embedObject = embedObject;
            return this;
        }

        public Builder withReplacePrevious(final boolean replacePrevious) {
            this.replacePrevious = replacePrevious;
            return this;
        }

        public Builder withDirectRecipient(final IUser user) {
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
