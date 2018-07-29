package com.gmo.discord.hanyu.bot.api;

import java.util.Objects;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

/**
 * @author tedelen
 */
@JsonDeserialize(builder = Translation.Builder.class)
public class Translation {
    private final String text;
    private final String destinationLanguage;
    private final Transliteration transliteration;

    private Translation(final Builder builder) {
        text = builder.text;
        destinationLanguage = builder.destinationLanguage;
        transliteration = builder.transliteration;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public String getText() {
        return text;
    }

    public String getDestinationLanguage() {
        return destinationLanguage;
    }

    public Optional<Transliteration> getTransliteration() {
        return Optional.ofNullable(transliteration);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Translation that = (Translation) o;
        return Objects.equals(text, that.text) &&
                Objects.equals(destinationLanguage, that.destinationLanguage) &&
                Objects.equals(transliteration, that.transliteration);
    }

    @Override
    public int hashCode() {
        return Objects.hash(text, destinationLanguage, transliteration);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("text", text)
                .add("destinationLanguage", destinationLanguage)
                .add("transliteration", transliteration)
                .toString();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static final class Builder {
        private String text;
        private String destinationLanguage;
        private Transliteration transliteration;

        private Builder() {
        }

        public Builder withText(final String text) {
            this.text = text;
            return this;
        }

        @JsonProperty("to")
        public Builder withDestinationLanguage(final String destinationLanguage) {
            this.destinationLanguage = destinationLanguage;
            return this;
        }

        public Builder withTransliteration(final Transliteration transliteration) {
            this.transliteration = transliteration;
            return this;
        }

        public Translation build() {
            Preconditions.checkNotNull(Strings.emptyToNull(text), "Null/empty text");
            Preconditions.checkNotNull(Strings.emptyToNull(destinationLanguage), "Null/empty text");
            return new Translation(this);
        }
    }
}
