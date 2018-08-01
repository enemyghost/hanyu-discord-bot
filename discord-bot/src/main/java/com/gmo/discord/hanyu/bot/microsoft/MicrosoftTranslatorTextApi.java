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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.gmo.discord.hanyu.bot.api.entities.DetectionResponse;
import com.gmo.discord.hanyu.bot.api.entities.DictionaryLookupResponse;
import com.gmo.discord.hanyu.bot.api.entities.TranslationRequest;
import com.gmo.discord.hanyu.bot.api.entities.TranslationResponse;
import com.gmo.discord.hanyu.bot.api.TranslatorTextApi;
import com.gmo.discord.hanyu.bot.api.entities.example.ExampleRequest;
import com.gmo.discord.hanyu.bot.api.entities.example.ExampleResponse;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.io.CharStreams;

/**
 * @author tedelen
 */
public class MicrosoftTranslatorTextApi implements TranslatorTextApi {
    private static final Logger LOGGER = LoggerFactory.getLogger(MicrosoftTranslatorTextApi.class);

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final ObjectReader TRANSLATION_RESPONSE_READER = OBJECT_MAPPER.readerFor(new TypeReference<List<TranslationResponse>>() {});
    private static final ObjectReader DETECTION_RESPONSE_READER = OBJECT_MAPPER.readerFor(new TypeReference<List<DetectionResponse>>() {});
    private static final ObjectReader DICTIONARY_LOOKUP_RESPONSE_READER = OBJECT_MAPPER.readerFor(new TypeReference<List<DictionaryLookupResponse>>() {});
    private static final ObjectReader DICTIONARY_EXAMPLE_RESPONSE_READER = OBJECT_MAPPER.readerFor(new TypeReference<List<ExampleResponse>>() {});
    private static final String API_HOST = "https://api.cognitive.microsofttranslator.com";
    private static final String API_TRANSLATE_PATH = "/translate?api-version=3.0";
    private static final String API_DICTIONARY_LOOKUP_PATH = "/dictionary/lookup?api-version=3.0";
    private static final String API_DICTIONARY_EXAMPLE_PATH = "/dictionary/examples?api-version=3.0";
    private static final String API_DETECT_PATH = "/detect?api-version=3.0";
    private static final String API_TO_PARAM = "to";
    private static final String API_FROM_PARAM = "from";
    private static final Charset UTF8_CHARSET = Charset.forName("UTF-8");

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
    public List<TranslationResponse> translate(final TranslationRequest request) throws IOException {
        final UUID clientTraceId = UUID.randomUUID();
        final URI uri;
        try {
            final URIBuilder uriBuilder = new URIBuilder(apiHost + API_TRANSLATE_PATH);
            request.getDestinationLanguages().forEach(lang -> uriBuilder.addParameter(API_TO_PARAM, lang));
            uri = uriBuilder.build();
        } catch (final URISyntaxException e) {
            throw new RuntimeException("Invalid URI", e);
        }
        final HttpPost postRequest = createPostWithHeaders(uri, clientTraceId);
        final String content = OBJECT_MAPPER.writeValueAsString(request.getText());
        postRequest.setEntity(new StringEntity(content, UTF8_CHARSET));

        final HttpResponse response = httpClient.execute(postRequest);
        if (response.getStatusLine().getStatusCode() == 200) {
            try (final Reader reader = new InputStreamReader(response.getEntity().getContent())) {
                final String responseJson = CharStreams.toString(reader);
                return TRANSLATION_RESPONSE_READER.readValue(responseJson);
            }
        } else {
            LOGGER.error("Failed status code {}: {}", response.getStatusLine().getStatusCode(), response.getStatusLine().getReasonPhrase());
            throw new IOException(String.format("Failed status code %d: %s", response.getStatusLine().getStatusCode(), response.getStatusLine().getReasonPhrase()));
        }
    }

    @Override
    public DetectionResponse detect(final TranslationRequest request) throws IOException {
        final UUID clientTraceId = UUID.randomUUID();
        final URI uri;
        try {
            uri = new URIBuilder(apiHost + API_DETECT_PATH).build();
        } catch (final URISyntaxException e) {
            throw new RuntimeException("Invalid URI", e);
        }
        final HttpPost postRequest = createPostWithHeaders(uri, clientTraceId);
        final String content = OBJECT_MAPPER.writeValueAsString(request.getText());
        postRequest.setEntity(new StringEntity(content, UTF8_CHARSET));
        final HttpResponse response = httpClient.execute(postRequest);
        if (response.getStatusLine().getStatusCode() == 200) {
            try (final Reader reader = new InputStreamReader(response.getEntity().getContent())) {
                final String responseJson = CharStreams.toString(reader);
                return Iterables.getOnlyElement(DETECTION_RESPONSE_READER.readValue(responseJson));
            }
        } else {
            LOGGER.error("Failed status code {}: {}", response.getStatusLine().getStatusCode(), response.getStatusLine().getReasonPhrase());
            throw new IOException(String.format("Failed status code %d: %s", response.getStatusLine().getStatusCode(), response.getStatusLine().getReasonPhrase()));
        }
    }

    @Override
    public DictionaryLookupResponse lookup(final TranslationRequest request) throws IOException {
        final URI uri;
        try {
            final URIBuilder uriBuilder = new URIBuilder(apiHost + API_DICTIONARY_LOOKUP_PATH);
            request.getDestinationLanguages().forEach(lang -> uriBuilder.addParameter(API_TO_PARAM, lang));
            uriBuilder.addParameter(API_FROM_PARAM, request.getSourceLanguage().orElseThrow(() -> new RuntimeException("Source language required")));
            uri = uriBuilder.build();
        } catch (final URISyntaxException e) {
            throw new RuntimeException("Invalid URI", e);
        }
        final HttpPost postRequest = createPostWithHeaders(uri, UUID.randomUUID());
        final String content = OBJECT_MAPPER.writeValueAsString(request.getText());
        postRequest.setEntity(new StringEntity(content, UTF8_CHARSET));
        final HttpResponse response = httpClient.execute(postRequest);
        if (response.getStatusLine().getStatusCode() == 200) {
            try (final Reader reader = new InputStreamReader(response.getEntity().getContent())) {
                final String responseJson = CharStreams.toString(reader);
                return Iterables.getOnlyElement(DICTIONARY_LOOKUP_RESPONSE_READER.readValue(responseJson));
            }
        } else {
            LOGGER.error("Failed status code {}: {}", response.getStatusLine().getStatusCode(), response.getStatusLine().getReasonPhrase());
            throw new IOException(String.format("Failed status code %d: %s", response.getStatusLine().getStatusCode(), response.getStatusLine().getReasonPhrase()));
        }
    }

    @Override
    public ExampleResponse examples(final ExampleRequest request) throws IOException {
        final URI uri;
        try {
            final URIBuilder uriBuilder = new URIBuilder(apiHost + API_DICTIONARY_EXAMPLE_PATH);
            uriBuilder.addParameter(API_TO_PARAM, request.getDestinationLanguage());
            uriBuilder.addParameter(API_FROM_PARAM, request.getSourceLanguage());
            uri = uriBuilder.build();
        } catch (final URISyntaxException e) {
            throw new RuntimeException("Invalid URI", e);
        }

        final HttpPost postRequest = createPostWithHeaders(uri, UUID.randomUUID());
        final String content = OBJECT_MAPPER.writeValueAsString(ImmutableList.of(request));
        postRequest.setEntity(new StringEntity(content, UTF8_CHARSET));
        final HttpResponse response = httpClient.execute(postRequest);
        if (response.getStatusLine().getStatusCode() == 200) {
            try (final Reader reader = new InputStreamReader(response.getEntity().getContent())) {
                final String responseJson = CharStreams.toString(reader);
                return Iterables.getOnlyElement(DICTIONARY_EXAMPLE_RESPONSE_READER.readValue(responseJson));
            }
        } else {
            LOGGER.error("Failed status code {}: {}", response.getStatusLine().getStatusCode(), response.getStatusLine().getReasonPhrase());
            throw new IOException(String.format("Failed status code %d: %s", response.getStatusLine().getStatusCode(), response.getStatusLine().getReasonPhrase()));
        }
    }

    private HttpPost createPostWithHeaders(final URI uri, final UUID clientTraceId) {
        final HttpPost postRequest = new HttpPost(uri);
        postRequest.setHeader("Content-Type", "application/json");
        postRequest.setHeader("Ocp-Apim-Subscription-Key", apiKeySupplier.get());
        postRequest.setHeader("X-ClientTraceId", clientTraceId.toString());
        return postRequest;
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
