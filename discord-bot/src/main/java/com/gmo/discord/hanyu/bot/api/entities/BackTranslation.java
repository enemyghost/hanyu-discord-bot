package com.gmo.discord.hanyu.bot.api.entities;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.base.MoreObjects;

/**
 * @author tedelen
 */
@JsonDeserialize(builder = BackTranslation.Builder.class)
public class BackTranslation {
    private final String normalizedText;
    private final String displayText;
    private final int numExamples;
    private final int frequencyCount;

    private BackTranslation(Builder builder) {
        normalizedText = builder.normalizedText;
        displayText = builder.displayText;
        numExamples = builder.numExamples;
        frequencyCount = builder.frequencyCount;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public String getNormalizedText() {
        return normalizedText;
    }

    public String getDisplayText() {
        return displayText;
    }

    public int getNumExamples() {
        return numExamples;
    }

    public int getFrequencyCount() {
        return frequencyCount;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BackTranslation that = (BackTranslation) o;
        return numExamples == that.numExamples &&
                frequencyCount == that.frequencyCount &&
                Objects.equals(normalizedText, that.normalizedText) &&
                Objects.equals(displayText, that.displayText);
    }

    @Override
    public int hashCode() {
        return Objects.hash(normalizedText, displayText, numExamples, frequencyCount);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("normalizedText", normalizedText)
                .add("displayText", displayText)
                .add("numExamples", numExamples)
                .add("frequencyCount", frequencyCount)
                .toString();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static final class Builder {
        private String normalizedText;
        private String displayText;
        private int numExamples;
        private int frequencyCount;

        private Builder() {
        }

        public Builder withNormalizedText(String normalizedText) {
            this.normalizedText = normalizedText;
            return this;
        }

        public Builder withDisplayText(String displayText) {
            this.displayText = displayText;
            return this;
        }

        public Builder withNumExamples(int numExamples) {
            this.numExamples = numExamples;
            return this;
        }

        public Builder withFrequencyCount(int frequencyCount) {
            this.frequencyCount = frequencyCount;
            return this;
        }

        public BackTranslation build() {
            return new BackTranslation(this);
        }
    }
}
