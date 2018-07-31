package com.gmo.discord.hanyu.bot.api.entities;

import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.base.MoreObjects;

/**
 * @author tedelen
 */
@JsonDeserialize(builder = DetectionResponse.Builder.class)
public class DetectionResponse {
    private final String language;
    private final double score;
    private final boolean isTranslationSupported;
    private final boolean isTransliterationSupported;
    private final List<DetectionResponse> alternatives;

    private DetectionResponse(Builder builder) {
        language = builder.language;
        score = builder.score;
        isTranslationSupported = builder.isTranslationSupported;
        isTransliterationSupported = builder.isTransliterationSupported;
        alternatives = builder.alternatives;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public String getLanguage() {
        return language;
    }

    public double getScore() {
        return score;
    }

    public boolean isTranslationSupported() {
        return isTranslationSupported;
    }

    public boolean isTransliterationSupported() {
        return isTransliterationSupported;
    }

    public List<DetectionResponse> getAlternatives() {
        return alternatives;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DetectionResponse that = (DetectionResponse) o;
        return Double.compare(that.score, score) == 0 &&
                isTranslationSupported == that.isTranslationSupported &&
                isTransliterationSupported == that.isTransliterationSupported &&
                Objects.equals(language, that.language) &&
                Objects.equals(alternatives, that.alternatives);
    }

    @Override
    public int hashCode() {
        return Objects.hash(language, score, isTranslationSupported, isTransliterationSupported, alternatives);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("language", language)
                .add("score", score)
                .add("isTranslationSupported", isTranslationSupported)
                .add("isTransliterationSupported", isTransliterationSupported)
                .add("alternatives", alternatives)
                .toString();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static final class Builder {
        private String language;
        private double score;
        private boolean isTranslationSupported;
        private boolean isTransliterationSupported;
        private List<DetectionResponse> alternatives;

        private Builder() {
        }

        public Builder withLanguage(String language) {
            this.language = language;
            return this;
        }

        public Builder withScore(double score) {
            this.score = score;
            return this;
        }

        public Builder withIsTranslationSupported(boolean isTranslationSupported) {
            this.isTranslationSupported = isTranslationSupported;
            return this;
        }

        public Builder withIsTransliterationSupported(boolean isTransliterationSupported) {
            this.isTransliterationSupported = isTransliterationSupported;
            return this;
        }

        public Builder withAlternatives(List<DetectionResponse> alternatives) {
            this.alternatives = alternatives;
            return this;
        }

        public DetectionResponse build() {
            return new DetectionResponse(this);
        }
    }
}
