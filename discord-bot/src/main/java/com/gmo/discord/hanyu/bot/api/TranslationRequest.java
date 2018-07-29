package com.gmo.discord.hanyu.bot.api;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.MoreObjects;

/**
 * @author tedelen
 */
public class TranslationRequest {
    private final String text;
    private final List<String> destinationLanguages;
    private final String sourceLanguage;

    private TranslationRequest(final Builder builder) {
        text = builder.text;
        destinationLanguages = builder.destinationLanguages;
        sourceLanguage = builder.sourceLanguage;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public String getText() {
        return text;
    }

    @JsonIgnore
    public List<String> getDestinationLanguages() {
        return destinationLanguages;
    }

    @JsonIgnore
    public Optional<String> getSourceLanguage() {
        return Optional.ofNullable(sourceLanguage);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TranslationRequest that = (TranslationRequest) o;
        return Objects.equals(text, that.text) &&
                Objects.equals(destinationLanguages, that.destinationLanguages) &&
                Objects.equals(sourceLanguage, that.sourceLanguage);
    }

    @Override
    public int hashCode() {
        return Objects.hash(text, destinationLanguages, sourceLanguage);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("text", text)
                .add("destinationLanguages", destinationLanguages)
                .add("sourceLanguage", sourceLanguage)
                .toString();
    }

    public static final class Builder {
        private String text;
        private List<String> destinationLanguages;
        private String sourceLanguage;

        private Builder() {
            destinationLanguages = new ArrayList<>();
        }

        public Builder withText(final String text) {
            this.text = text;
            return this;
        }

        public Builder addDestinationLanguage(final String destinationLanguage) {
            this.destinationLanguages.add(destinationLanguage);
            return this;
        }

        public Builder withDestinationLanguages(final Collection<String> destinationLanguages) {
            this.destinationLanguages = new ArrayList<>(destinationLanguages);
            return this;
        }

        public Builder withSourceLanguage(final String sourceLanguage) {
            this.sourceLanguage = sourceLanguage;
            return this;
        }

        public TranslationRequest build() {
            return new TranslationRequest(this);
        }
    }
}
