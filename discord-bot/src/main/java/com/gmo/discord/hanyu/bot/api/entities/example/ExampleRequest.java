package com.gmo.discord.hanyu.bot.api.entities.example;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.base.MoreObjects;

/**
 * @author tedelen
 */
@JsonDeserialize(builder = ExampleRequest.Builder.class)
public class ExampleRequest {
    private final String destinationLanguage;
    private final String sourceLanguage;
    private final String sourceText;
    private final String destinationTranslation;

    private ExampleRequest(Builder builder) {
        destinationLanguage = builder.destinationLanguage;
        sourceLanguage = builder.sourceLanguage;
        sourceText = builder.sourceText;
        destinationTranslation = builder.destinationTranslation;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    @JsonIgnore
    public String getDestinationLanguage() {
        return destinationLanguage;
    }

    @JsonIgnore
    public String getSourceLanguage() {
        return sourceLanguage;
    }

    @JsonProperty("Text")
    public String getSourceText() {
        return sourceText;
    }

    @JsonProperty("Translation")
    public String getDestinationTranslation() {
        return destinationTranslation;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExampleRequest that = (ExampleRequest) o;
        return Objects.equals(destinationLanguage, that.destinationLanguage) &&
                Objects.equals(sourceLanguage, that.sourceLanguage) &&
                Objects.equals(sourceText, that.sourceText) &&
                Objects.equals(destinationTranslation, that.destinationTranslation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(destinationLanguage, sourceLanguage, sourceText, destinationTranslation);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("destinationLanguage", destinationLanguage)
                .add("sourceLanguage", sourceLanguage)
                .add("sourceText", sourceText)
                .add("destinationTranslation", destinationTranslation)
                .toString();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static final class Builder {
        private String destinationLanguage;
        private String sourceLanguage;
        private String sourceText;
        private String destinationTranslation;

        private Builder() {
        }

        public Builder withDestinationLanguage(String destinationLanguage) {
            this.destinationLanguage = destinationLanguage;
            return this;
        }

        public Builder withSourceLanguage(String sourceLanguage) {
            this.sourceLanguage = sourceLanguage;
            return this;
        }

        @JsonProperty("Text")
        public Builder withSourceText(String sourceText) {
            this.sourceText = sourceText;
            return this;
        }

        @JsonProperty("Translation")
        public Builder withDestinationTranslation(String destinationTranslation) {
            this.destinationTranslation = destinationTranslation;
            return this;
        }

        public ExampleRequest build() {
            return new ExampleRequest(this);
        }
    }
}
