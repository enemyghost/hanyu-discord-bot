package com.gmo.discord.hanyu.bot.api.entities;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.base.MoreObjects;

/**
 * @author tedelen
 */
@JsonDeserialize(builder = TranslationResponse.Builder.class)
public class TranslationResponse {
    private final TranslationRequest request;
    private final List<Translation> translations;
    private final UUID clientTraceId;

    private TranslationResponse(final Builder builder) {
        request = builder.request;
        translations = builder.translations;
        clientTraceId = builder.clientTraceId;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static Builder newBuilder(final TranslationResponse response) {
        return newBuilder()
                .withClientTraceId(response.getClientTraceId().orElse(null))
                .withTranslations(response.getTranslations())
                .withRequest(response.getRequest());
    }

    public TranslationRequest getRequest() {
        return request;
    }

    public List<Translation> getTranslations() {
        return translations;
    }

    public Optional<UUID> getClientTraceId() {
        return Optional.ofNullable(clientTraceId);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TranslationResponse that = (TranslationResponse) o;
        return Objects.equals(request, that.request) &&
                Objects.equals(translations, that.translations) &&
                Objects.equals(clientTraceId, that.clientTraceId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(request, translations, clientTraceId);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("request", request)
                .add("translations", translations)
                .add("clientTraceId", clientTraceId)
                .toString();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static final class Builder {
        private TranslationRequest request;
        private List<Translation> translations;
        private UUID clientTraceId;

        private Builder() {
            translations = new ArrayList<>();
        }

        public Builder withRequest(final TranslationRequest request) {
            this.request = request;
            return this;
        }

        public Builder addTranslation(final Translation translation) {
            this.translations.add(translation);
            return this;
        }

        public Builder withTranslations(final List<Translation> translations) {
            this.translations = new ArrayList<>(translations);
            return this;
        }

        public Builder withClientTraceId(final UUID clientTraceId) {
            this.clientTraceId = clientTraceId;
            return this;
        }

        public TranslationResponse build() {
            Objects.requireNonNull(translations, "Null translations");
            return new TranslationResponse(this);
        }
    }
}
