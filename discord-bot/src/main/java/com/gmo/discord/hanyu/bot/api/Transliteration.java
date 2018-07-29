package com.gmo.discord.hanyu.bot.api;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.base.MoreObjects;

/**
 * @author tedelen
 */
@JsonDeserialize(builder = Transliteration.Builder.class)
public class Transliteration {
    private final String text;
    private final String script;

    private Transliteration(Builder builder) {
        text = builder.text;
        script = builder.script;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public String getText() {
        return text;
    }

    public String getScript() {
        return script;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Transliteration that = (Transliteration) o;
        return Objects.equals(text, that.text) &&
                Objects.equals(script, that.script);
    }

    @Override
    public int hashCode() {
        return Objects.hash(text, script);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("text", text)
                .add("script", script)
                .toString();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static final class Builder {
        private String text;
        private String script;

        private Builder() {
        }

        public Builder withText(final String text) {
            this.text = text;
            return this;
        }

        public Builder withScript(final String script) {
            this.script = script;
            return this;
        }

        public Transliteration build() {
            return new Transliteration(this);
        }
    }
}
