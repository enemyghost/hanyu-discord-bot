package com.gmo.discord.hanyu.bot.api.entities.example;

import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.base.MoreObjects;

/**
 * @author tedelen
 */
@JsonDeserialize(builder = ExampleResponse.Builder.class)
public class ExampleResponse {
    private final String normalizedSource;
    private final String normalizedTarget;
    private final List<Example> examples;

    private ExampleResponse(Builder builder) {
        normalizedSource = builder.normalizedSource;
        normalizedTarget = builder.normalizedTarget;
        examples = builder.examples;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public String getNormalizedSource() {
        return normalizedSource;
    }

    public String getNormalizedTarget() {
        return normalizedTarget;
    }

    public List<Example> getExamples() {
        return examples;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExampleResponse that = (ExampleResponse) o;
        return Objects.equals(normalizedSource, that.normalizedSource) &&
                Objects.equals(normalizedTarget, that.normalizedTarget) &&
                Objects.equals(examples, that.examples);
    }

    @Override
    public int hashCode() {
        return Objects.hash(normalizedSource, normalizedTarget, examples);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("normalizedSource", normalizedSource)
                .add("normalizedTarget", normalizedTarget)
                .add("examples", examples)
                .toString();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static final class Builder {
        private String normalizedSource;
        private String normalizedTarget;
        private List<Example> examples;

        private Builder() {
        }

        public Builder withNormalizedSource(final String normalizedSource) {
            this.normalizedSource = normalizedSource;
            return this;
        }

        public Builder withNormalizedTarget(final String normalizedTarget) {
            this.normalizedTarget = normalizedTarget;
            return this;
        }

        public Builder withExamples(final List<Example> examples) {
            this.examples = examples;
            return this;
        }

        public ExampleResponse build() {
            return new ExampleResponse(this);
        }
    }
}
