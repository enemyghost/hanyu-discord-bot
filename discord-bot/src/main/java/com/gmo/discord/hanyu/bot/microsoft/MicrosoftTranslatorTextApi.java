package com.gmo.discord.hanyu.bot.microsoft;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.gmo.discord.hanyu.bot.api.TranslationRequest;
import com.gmo.discord.hanyu.bot.api.TranslationResponse;
import com.gmo.discord.hanyu.bot.api.TranslatorTextApi;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.io.CharStreams;

/**
 * @author tedelen
 */
public class MicrosoftTranslatorTextApi implements TranslatorTextApi {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final ObjectReader OBJECT_READER = OBJECT_MAPPER.readerFor(new TypeReference<List<TranslationResponse>>() {});
    private static final String API_HOST = "https://api.cognitive.microsofttranslator.com";
    private static final String API_PATH = "/translate?api-version=3.0";
    private static final String API_TO_PARAM = "to";

    private final HttpClient httpClient;
    private final String apiHost;
    private final Supplier<String> apiKeySupplier;

    private MicrosoftTranslatorTextApi(final Builder builder) {
        httpClient = builder.httpClient;
        apiHost = builder.apiHost;
        apiKeySupplier = builder.apiKeySupplier;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    @Override
    public TranslationResponse translate(final TranslationRequest request) throws IOException {
        final UUID clientTraceId = UUID.randomUUID();
        final URI uri;
        try {
            final URIBuilder uriBuilder = new URIBuilder(apiHost + API_PATH);
            request.getDestinationLanguages().forEach(lang -> uriBuilder.addParameter(API_TO_PARAM, lang));
            uri = uriBuilder.build();
        } catch (final URISyntaxException e) {
            throw new RuntimeException("Invalid URI", e);
        }
        final HttpPost postRequest = new HttpPost(uri);
        final String content = OBJECT_MAPPER.writeValueAsString(ImmutableList.of(request));
        postRequest.setHeader("Content-Type", "application/json");
        postRequest.setHeader("Ocp-Apim-Subscription-Key", apiKeySupplier.get());
        postRequest.setHeader("X-ClientTraceId", clientTraceId.toString());
        postRequest.setEntity(new StringEntity(content, Charset.forName("UTF-8")));

        final HttpResponse response = httpClient.execute(postRequest);
        if (response.getStatusLine().getStatusCode() == 200) {
            try (final Reader reader = new InputStreamReader(response.getEntity().getContent())) {
                final String responseJson = CharStreams.toString(reader);
                return TranslationResponse.newBuilder(Iterables.getOnlyElement(OBJECT_READER.readValue(responseJson)))
                        .withRequest(request)
                        .withClientTraceId(clientTraceId)
                        .build();
            }
        } else {
            throw new IOException(String.format("Failed status code %d: %s", response.getStatusLine().getStatusCode(), response.getStatusLine().getReasonPhrase()));
        }
    }

    public static final class Builder {
        private HttpClient httpClient;
        private String apiHost;
        private Supplier<String> apiKeySupplier;

        private Builder() {
            apiHost = API_HOST;
            apiKeySupplier = () -> System.getenv("MS_TRANSLATOR_KEY");
        }

        public Builder withHttpClient(final HttpClient httpClient) {
            this.httpClient = httpClient;
            return this;
        }

        public Builder withApiHost(final String apiHost) {
            this.apiHost = apiHost;
            return this;
        }

        public Builder withApiKeySupplier(final Supplier<String> apiKeySupplier) {
            this.apiKeySupplier = apiKeySupplier;
            return this;
        }

        public MicrosoftTranslatorTextApi build() {
            return new MicrosoftTranslatorTextApi(this);
        }
    }
}
