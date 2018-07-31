package com.gmo.discord.hanyu.bot.api.entities;

import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.base.MoreObjects;

/**
 * @author tedelen
 */
@JsonDeserialize(builder = DictionaryTranslation.Builder.class)
public class DictionaryTranslation {
    private final String normalizedTarget;
    private final String displayTarget;
    private final String posTag;
    private final double confidence;
    private final String prefixWord;
    private final List<BackTranslation> backTranslations;

    private DictionaryTranslation(Builder builder) {
        normalizedTarget = builder.normalizedTarget;
        displayTarget = builder.displayTarget;
        posTag = builder.posTag;
        confidence = builder.confidence;
        prefixWord = builder.prefixWord;
        backTranslations = builder.backTranslations;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public String getNormalizedTarget() {
        return normalizedTarget;
    }

    public String getDisplayTarget() {
        return displayTarget;
    }

    public String getPosTag() {
        return posTag;
    }

    public double getConfidence() {
        return confidence;
    }

    public String getPrefixWord() {
        return prefixWord;
    }

    public List<BackTranslation> getBackTranslations() {
        return backTranslations;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DictionaryTranslation that = (DictionaryTranslation) o;
        return Double.compare(that.confidence, confidence) == 0 &&
                Objects.equals(normalizedTarget, that.normalizedTarget) &&
                Objects.equals(displayTarget, that.displayTarget) &&
                Objects.equals(posTag, that.posTag) &&
                Objects.equals(prefixWord, that.prefixWord) &&
                Objects.equals(backTranslations, that.backTranslations);
    }

    @Override
    public int hashCode() {
        return Objects.hash(normalizedTarget, displayTarget, posTag, confidence, prefixWord, backTranslations);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("normalizedTarget", normalizedTarget)
                .add("displayTarget", displayTarget)
                .add("posTag", posTag)
                .add("confidence", confidence)
                .add("prefixWord", prefixWord)
                .add("backTranslations", backTranslations)
                .toString();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static final class Builder {
        private String normalizedTarget;
        private String displayTarget;
        private String posTag;
        private double confidence;
        private String prefixWord;
        private List<BackTranslation> backTranslations;

        private Builder() {
        }

        public Builder withNormalizedTarget(String normalizedTarget) {
            this.normalizedTarget = normalizedTarget;
            return this;
        }

        public Builder withDisplayTarget(String displayTarget) {
            this.displayTarget = displayTarget;
            return this;
        }

        public Builder withPosTag(String posTag) {
            this.posTag = posTag;
            return this;
        }

        public Builder withConfidence(double confidence) {
            this.confidence = confidence;
            return this;
        }

        public Builder withPrefixWord(String prefixWord) {
            this.prefixWord = prefixWord;
            return this;
        }

        public Builder withBackTranslations(List<BackTranslation> backTranslations) {
            this.backTranslations = backTranslations;
            return this;
        }

        public DictionaryTranslation build() {
            return new DictionaryTranslation(this);
        }
    }
}
