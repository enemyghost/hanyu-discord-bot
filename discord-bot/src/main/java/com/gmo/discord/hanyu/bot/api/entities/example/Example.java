package com.gmo.discord.hanyu.bot.api.entities.example;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.base.MoreObjects;

/**
 * @author tedelen
 */
@JsonDeserialize(builder = Example.Builder.class)
public class Example {
    private final String sourcePrefix;
    private final String sourceTerm;
    private final String sourceSuffix;
    private final String targetPrefix;
    private final String targetTerm;
    private final String targetSuffix;
    private final String sourceSentence;
    private final String targetSentence;

    private Example(final Builder builder) {
        sourcePrefix = builder.sourcePrefix;
        sourceTerm = builder.sourceTerm;
        sourceSuffix = builder.sourceSuffix;
        targetPrefix = builder.targetPrefix;
        targetTerm = builder.targetTerm;
        targetSuffix = builder.targetSuffix;
        sourceSentence = builder.sourcePrefix + builder.sourceTerm + builder.sourceSuffix;
        targetSentence = builder.targetPrefix + builder.targetTerm + builder.targetSuffix;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public String getSourcePrefix() {
        return sourcePrefix;
    }

    public String getSourceTerm() {
        return sourceTerm;
    }

    public String getSourceSuffix() {
        return sourceSuffix;
    }

    public String getTargetPrefix() {
        return targetPrefix;
    }

    public String getTargetTerm() {
        return targetTerm;
    }

    public String getTargetSuffix() {
        return targetSuffix;
    }

    public String getSourceSentence() {
        return sourceSentence;
    }

    public String getTargetSentence() {
        return targetSentence;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Example example = (Example) o;
        return Objects.equals(sourcePrefix, example.sourcePrefix) &&
                Objects.equals(sourceTerm, example.sourceTerm) &&
                Objects.equals(sourceSuffix, example.sourceSuffix) &&
                Objects.equals(targetPrefix, example.targetPrefix) &&
                Objects.equals(targetTerm, example.targetTerm) &&
                Objects.equals(targetSuffix, example.targetSuffix);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sourcePrefix, sourceTerm, sourceSuffix, targetPrefix, targetTerm, targetSuffix);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("sourcePrefix", sourcePrefix)
                .add("sourceTerm", sourceTerm)
                .add("sourceSuffix", sourceSuffix)
                .add("targetPrefix", targetPrefix)
                .add("targetTerm", targetTerm)
                .add("targetSuffix", targetSuffix)
                .toString();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static final class Builder {
        private String sourcePrefix;
        private String sourceTerm;
        private String sourceSuffix;
        private String targetPrefix;
        private String targetTerm;
        private String targetSuffix;

        private Builder() {
        }

        public Builder withSourcePrefix(final String sourcePrefix) {
            this.sourcePrefix = sourcePrefix;
            return this;
        }

        public Builder withSourceTerm(final String sourceTerm) {
            this.sourceTerm = sourceTerm;
            return this;
        }

        public Builder withSourceSuffix(final String sourceSuffix) {
            this.sourceSuffix = sourceSuffix;
            return this;
        }

        public Builder withTargetPrefix(final String targetPrefix) {
            this.targetPrefix = targetPrefix;
            return this;
        }

        public Builder withTargetTerm(final String targetTerm) {
            this.targetTerm = targetTerm;
            return this;
        }

        public Builder withTargetSuffix(final String targetSuffix) {
            this.targetSuffix = targetSuffix;
            return this;
        }

        public Example build() {
            return new Example(this);
        }
    }
}
