package com.gmo.discord.hanyu.bot.api;

import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.base.MoreObjects;

/**
 * @author tedelen
 */
@JsonDeserialize(builder = DictionaryLookupResponse.Builder.class)
public class DictionaryLookupResponse {
    private final String normalizedSource;
    private final String displaySource;
    private final List<DictionaryTranslation> translations;

    private DictionaryLookupResponse(Builder builder) {
        normalizedSource = builder.normalizedSource;
        displaySource = builder.displaySource;
        translations = builder.translations;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public String getNormalizedSource() {
        return normalizedSource;
    }

    public String getDisplaySource() {
        return displaySource;
    }

    public List<DictionaryTranslation> getTranslations() {
        return translations;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DictionaryLookupResponse that = (DictionaryLookupResponse) o;
        return Objects.equals(normalizedSource, that.normalizedSource) &&
                Objects.equals(displaySource, that.displaySource) &&
                Objects.equals(translations, that.translations);
    }

    @Override
    public int hashCode() {
        return Objects.hash(normalizedSource, displaySource, translations);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("normalizedSource", normalizedSource)
                .add("displaySource", displaySource)
                .add("translations", translations)
                .toString();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static final class Builder {
        private String normalizedSource;
        private String displaySource;
        private List<DictionaryTranslation> translations;

        private Builder() {
        }

        public Builder withNormalizedSource(String normalizedSource) {
            this.normalizedSource = normalizedSource;
            return this;
        }

        public Builder withDisplaySource(String displaySource) {
            this.displaySource = displaySource;
            return this;
        }

        public Builder withTranslations(List<DictionaryTranslation> translations) {
            this.translations = translations;
            return this;
        }

        public DictionaryLookupResponse build() {
            return new DictionaryLookupResponse(this);
        }
    }
}
