package com.gmo.discord.hanyu.bot.api;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.base.MoreObjects;

/**
 * @author tedelen
 */
@JsonDeserialize(builder = TranslationRequestPayload.Builder.class)
public class TranslationRequestPayload {
    private final String text;

    private TranslationRequestPayload(final Builder builder) {
        text = builder.text;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public String getText() {
        return text;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TranslationRequestPayload that = (TranslationRequestPayload) o;
        return Objects.equals(text, that.text);
    }

    @Override
    public int hashCode() {
        return Objects.hash(text);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("text", text)
                .toString();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static final class Builder {
        private String text;

        private Builder() {
        }

        public Builder withText(String text) {
            this.text = text;
            return this;
        }

        public TranslationRequestPayload build() {
            return new TranslationRequestPayload(this);
        }
    }
}
