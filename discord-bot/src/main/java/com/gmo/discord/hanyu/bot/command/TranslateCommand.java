package com.gmo.discord.hanyu.bot.command;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gmo.discord.hanyu.bot.api.entities.BackTranslation;
import com.gmo.discord.hanyu.bot.api.entities.DetectionResponse;
import com.gmo.discord.hanyu.bot.api.entities.DictionaryLookupResponse;
import com.gmo.discord.hanyu.bot.api.entities.DictionaryTranslation;
import com.gmo.discord.hanyu.bot.api.entities.Translation;
import com.gmo.discord.hanyu.bot.api.entities.TranslationRequest;
import com.gmo.discord.hanyu.bot.api.entities.TranslationResponse;
import com.gmo.discord.hanyu.bot.api.TranslatorTextApi;
import com.gmo.discord.hanyu.bot.api.entities.Transliteration;
import com.gmo.discord.hanyu.bot.message.HanyuMessage;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

/**
 * !translate en 汉语
 *
 * @author tedelen
 */
public class TranslateCommand implements ICommand {
    private static final Logger LOGGER = LoggerFactory.getLogger(TranslateCommand.class);
    private static final Set<String> ALIASES = ImmutableSet.of("translate", "t", "tranny", "lookup", "l");
    private static final Set<String> PEENLESS_ALIASES = ImmutableSet.of("peenless", "pl", "no-pinyin", "np");

    private static final int MAX_LENGTH = 200;

    private final TranslatorTextApi translateApi;

    public TranslateCommand(final TranslatorTextApi translateApi) {
        this.translateApi = Objects.requireNonNull(translateApi, "Null translate API");
    }

    @Override
    public boolean canHandle(final CommandInfo commandInfo) {
        return ALIASES.contains(commandInfo.getCommand().toLowerCase());
    }

    @Override
    public HanyuMessage execute(final CommandInfo commandInfo) {
        if (!canHandle(commandInfo)) {
            throw new IllegalArgumentException("Invalid command, must call canHandle first");
        }

        if (commandInfo.getArgs().length < 1) {
            return help();
        }

        final boolean peenless = PEENLESS_ALIASES.contains(commandInfo.getArg(0).orElse("").trim().toLowerCase());
        final String[] textArgs = peenless
                ? Arrays.copyOfRange(commandInfo.getArgs(), 1, commandInfo.getArgs().length)
                : commandInfo.getArgs();

        final String textToTranslate = Joiner.on(" ").join(textArgs);
        if (textToTranslate.length() > MAX_LENGTH) {
            return HanyuMessage.newBuilder()
                    .withText(String.format("Message of length %d exceeds max length %d", textToTranslate.length(), MAX_LENGTH))
                    .build();
        }

        final TranslationRequest.Builder requestBuilder = TranslationRequest.newBuilder()
                .addText(textToTranslate);

        final Map<String, String> chineseToPinyinMap = new HashMap<>();
        if (commandInfo.getCommand().equalsIgnoreCase("lookup")
                || commandInfo.getCommand().equalsIgnoreCase("l")) {
            final DictionaryLookupResponse response;
            try {
                final DetectionResponse detectionResponse = translateApi.detect(requestBuilder.build());
                response = translateApi.lookup(requestBuilder
                        .withSourceLanguage(detectionResponse.getLanguage())
                        .addDestinationLanguage(detectionResponse.getLanguage().equalsIgnoreCase("zh-Hans")
                                ? "en"
                                : "zh-Hans")
                        .build());
                if (!peenless && detectionResponse.getLanguage().equalsIgnoreCase("en")) {
                    final TranslationRequest pinyinRequest = TranslationRequest.newBuilder()
                            .withSourceLanguage("zh-Hans")
                            .addDestinationLanguage("zh-Latn")
                            .withText(response.getTranslations().stream()
                            .map(DictionaryTranslation::getNormalizedTarget)
                            .collect(Collectors.toList()))
                            .build();
                    if (!pinyinRequest.getText().isEmpty()) {
                        translateApi.translate(pinyinRequest)
                                .stream()
                                .map(r -> r.getTranslations().stream().filter(t -> t.getTransliteration().filter(tl -> tl.getScript().equals("Latn")).isPresent()).findFirst().orElse(null))
                                .filter(Objects::nonNull)
                                .forEach(t -> chineseToPinyinMap.put(t.getText(), t.getTransliteration().map(Transliteration::getText).orElse("")));
                    } else {
                        return HanyuMessage.newBuilder()
                                .withText(String.format("No dictionary entry found for `%s`", textToTranslate))
                                .build();
                    }
                }
            } catch (final IOException e) {
                LOGGER.error("Exception reaching ms translator api", e);
                return HanyuMessage.newBuilder()
                        .withText("Failure to reach MS API")
                        .build();
            }

            final List<String> translations = response.getTranslations()
                    .stream()
                    .sorted(Comparator.comparingDouble(DictionaryTranslation::getConfidence).reversed())
                    .map(t -> String.format("%s%s\n\t%s", t.getDisplayTarget(),
                            chineseToPinyinMap.containsKey(t.getNormalizedTarget())
                                    ? " " + chineseToPinyinMap.get(t.getNormalizedTarget())
                                    : "",
                            t.getBackTranslations()
                                    .stream()
                                    .map(BackTranslation::getDisplayText)
                                    .collect(Collectors.joining(", "))))
                    .collect(Collectors.toList());

            return HanyuMessage.newBuilder()
                    .withText(String.format("```%s```", Joiner.on("\n").join(translations)))
                    .build();
        } else {
            if (!peenless) {
                requestBuilder.addDestinationLanguage("zh-Latn");
            }

            final TranslationRequest request = requestBuilder
                    .addDestinationLanguage("zh-Hans")
                    .addDestinationLanguage("en")
                    .build();
            final TranslationResponse translate;
            try {
                translate = Iterables.getOnlyElement(translateApi.translate(request));
            } catch (final IOException e) {
                LOGGER.error("Exception reaching ms translator api", e);
                return HanyuMessage.newBuilder()
                        .withText("Failure to reach MS API")
                        .build();
            }

            final List<String> translations = new ArrayList<>();
            for (final Translation t : translate.getTranslations()) {
                translations.add(t.getTransliteration().map(Transliteration::getText).orElse(t.getText()));
            }

            return HanyuMessage.newBuilder()
                    .withText(String.format("```%s```", Joiner.on("\n").join(translations)))
                    .build();
        }
    }

    @Override
    public HanyuMessage help() {
        return HanyuMessage.newBuilder()
                .withText("```Usage: !translate <text>\nExample: -translate 你好```")
                .build();
    }
}
