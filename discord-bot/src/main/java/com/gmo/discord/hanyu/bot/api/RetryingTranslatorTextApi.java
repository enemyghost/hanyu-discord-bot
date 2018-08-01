package com.gmo.discord.hanyu.bot.api;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import com.github.rholder.retry.BlockStrategies;
import com.github.rholder.retry.BlockStrategy;
import com.github.rholder.retry.RetryException;
import com.github.rholder.retry.Retryer;
import com.github.rholder.retry.RetryerBuilder;
import com.github.rholder.retry.StopStrategies;
import com.github.rholder.retry.StopStrategy;
import com.github.rholder.retry.WaitStrategies;
import com.github.rholder.retry.WaitStrategy;
import com.gmo.discord.hanyu.bot.api.entities.DetectionResponse;
import com.gmo.discord.hanyu.bot.api.entities.DictionaryLookupResponse;
import com.gmo.discord.hanyu.bot.api.entities.TranslationRequest;
import com.gmo.discord.hanyu.bot.api.entities.TranslationResponse;
import com.gmo.discord.hanyu.bot.api.entities.example.ExampleRequest;
import com.gmo.discord.hanyu.bot.api.entities.example.ExampleResponse;
import com.google.common.base.Throwables;

/**
 * @author tedelen
 */
public class RetryingTranslatorTextApi implements TranslatorTextApi {
    private final TranslatorTextApi delegate;
    private final Retryer<List<TranslationResponse>> translateRetryer;
    private final Retryer<DetectionResponse> detectionRetryer;
    private final Retryer<DictionaryLookupResponse> lookupRetryer;
    private final Retryer<ExampleResponse> exampleRetryer;

    private RetryingTranslatorTextApi(final Builder builder) {
        delegate = builder.delegate;
        translateRetryer = getRetryer(builder);
        detectionRetryer = getRetryer(builder);
        lookupRetryer = getRetryer(builder);
        exampleRetryer = getRetryer(builder);
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    @Override
    public List<TranslationResponse> translate(final TranslationRequest request) throws IOException {
        try {
            return translateRetryer.call(() -> delegate.translate(request));
        } catch (final ExecutionException | RetryException e) {
            Throwables.throwIfInstanceOf(Throwables.getRootCause(e), IOException.class);
            throw new RuntimeException(e);
        }
    }

    @Override
    public DetectionResponse detect(final TranslationRequest request) throws IOException {
        try {
            return detectionRetryer.call(() -> delegate.detect(request));
        } catch (final ExecutionException | RetryException e) {
            Throwables.throwIfInstanceOf(Throwables.getRootCause(e), IOException.class);
            throw new RuntimeException(e);
        }
    }

    @Override
    public DictionaryLookupResponse lookup(final TranslationRequest request) throws IOException {
        try {
            return lookupRetryer.call(() -> delegate.lookup(request));
        } catch (final ExecutionException | RetryException e) {
            Throwables.throwIfInstanceOf(Throwables.getRootCause(e), IOException.class);
            throw new RuntimeException(e);
        }
    }

    @Override
    public ExampleResponse examples(final ExampleRequest request) throws IOException {
        try {
            return exampleRetryer.call(() -> delegate.examples(request));
        } catch (final ExecutionException | RetryException e) {
            Throwables.throwIfInstanceOf(Throwables.getRootCause(e), IOException.class);
            throw new RuntimeException(e);
        }
    }

    private static <T> Retryer<T> getRetryer(final Builder builder) {
        return RetryerBuilder.<T>newBuilder()
                .withStopStrategy(builder.stopStrategy)
                .withWaitStrategy(builder.waitStrategy)
                .withBlockStrategy(builder.blockStrategy)
                .retryIfExceptionOfType(IOException.class)
                .build();
    }


    public static final class Builder {
        private TranslatorTextApi delegate;
        private StopStrategy stopStrategy;
        private WaitStrategy waitStrategy;
        private BlockStrategy blockStrategy;

        private Builder() {
            stopStrategy = StopStrategies.stopAfterAttempt(5);
            waitStrategy = WaitStrategies.exponentialWait(2000, TimeUnit.MILLISECONDS);
            blockStrategy = BlockStrategies.threadSleepStrategy();
        }

        public Builder withDelegate(final TranslatorTextApi val) {
            delegate = val;
            return this;
        }

        public Builder withStopStrategy(final StopStrategy val) {
            stopStrategy = val;
            return this;
        }

        public Builder withWaitStrategy(final WaitStrategy val) {
            waitStrategy = val;
            return this;
        }

        public Builder withBlockStrategy(final BlockStrategy val) {
            blockStrategy = val;
            return this;
        }

        public RetryingTranslatorTextApi build() {
            requireNonNull(delegate, "Null delegate");
            requireNonNull(stopStrategy, "Null stop strategy");
            requireNonNull(waitStrategy, "Null wait strategy");
            requireNonNull(blockStrategy, "Null block strategy");

            return new RetryingTranslatorTextApi(this);
        }
    }
}
