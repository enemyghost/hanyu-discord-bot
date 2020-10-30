package com.gmo.discord.hanyu.bot.command;

import java.io.IOException;
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

import com.gmo.discord.hanyu.bot.api.TranslatorTextApi;
import com.gmo.discord.hanyu.bot.api.entities.BackTranslation;
import com.gmo.discord.hanyu.bot.api.entities.DetectionResponse;
import com.gmo.discord.hanyu.bot.api.entities.DictionaryLookupResponse;
import com.gmo.discord.hanyu.bot.api.entities.DictionaryTranslation;
import com.gmo.discord.hanyu.bot.api.entities.Translation;
import com.gmo.discord.hanyu.bot.api.entities.TranslationRequest;
import com.gmo.discord.hanyu.bot.api.entities.Transliteration;
import com.gmo.discord.support.command.CommandInfo;
import com.gmo.discord.support.message.DiscordMessage;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableSet;

/**
 * {@link ICommand} which looks up an english or chinese term in a Chinese-English dictionary.
 *
 * @author tedelen
 */
public class LookupCommand implements ICommand {
    private static final Logger LOGGER = LoggerFactory.getLogger(TranslateCommand.class);
    private static final Set<String> ALIASES = ImmutableSet.of("lookup", "l");
    private static final Set<String> PEENLESS_ALIASES = ImmutableSet.of("peenless", "pl", "no-pinyin", "np");
    private static final Set<String> HELP_ALIASES = ImmutableSet.of("help", "h");

    private static final int MAX_LENGTH = 200;

    private final TranslatorTextApi translateApi;

    public LookupCommand(final TranslatorTextApi translateApi) {
        this.translateApi = Objects.requireNonNull(translateApi, "Null translate API");
    }

    @Override
    public boolean canHandle(final CommandInfo commandInfo) {
        return ALIASES.contains(commandInfo.getCommand());
    }

    @Override
    public Iterable<DiscordMessage> execute(final CommandInfo commandInfo) {
        if (!canHandle(commandInfo)) {
            throw new IllegalArgumentException("Invalid command, must call canHandle first");
        }

        String textToTranslate = Joiner.on(" ").join(commandInfo.getArgs());

        final boolean peenless = Arrays.stream(commandInfo.getArgs()).anyMatch(PEENLESS_ALIASES::contains);
        textToTranslate = textToTranslate.replaceAll(String.join(" |", PEENLESS_ALIASES), "");

        if (HELP_ALIASES.stream().anyMatch(textToTranslate::equalsIgnoreCase)) {
            return help().singleton();
        }

        if (textToTranslate.length() > MAX_LENGTH) {
            return DiscordMessage.newBuilder()
                    .withText(String.format("Message of length %d exceeds max length %d", textToTranslate.length(), MAX_LENGTH))
                    .build().singleton();
        }

        final TranslationRequest.Builder requestBuilder = TranslationRequest.newBuilder()
                .addText(textToTranslate);
        final Map<String, String> chineseToPinyinMap = new HashMap<>();
        final DictionaryLookupResponse response;
        try {
            final DetectionResponse detectionResponse = translateApi.detect(requestBuilder.build());
            response = translateApi.lookup(requestBuilder
                    .withSourceLanguage(detectionResponse.getLanguage().equalsIgnoreCase("zh-Hans")
                            ? "zh-Hans"
                            : "en")
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
                    chineseToPinyinMap.putAll(translateApi.translate(pinyinRequest)
                            .stream()
                            .map(r -> r.getTranslations().stream().filter(t -> t.getTransliteration().filter(tl -> tl.getScript().equals("Latn")).isPresent()).findFirst().orElse(null))
                            .filter(Objects::nonNull)
                            .collect(Collectors.toMap(Translation::getText, t -> t.getTransliteration().map(Transliteration::getText).orElse(""))));
                } else {
                    return DiscordMessage.newBuilder()
                            .withText(String.format("I couldn\'t find a dictionary entry for `%s`", textToTranslate))
                            .build().singleton();
                }
            }
        } catch (final IOException e) {
            LOGGER.error("Exception reaching ms translator api", e);
            return DiscordMessage.newBuilder()
                    .withText("Failure to reach MS API")
                    .build().singleton();
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

        return DiscordMessage.newBuilder()
                .withText(String.format("```\n%s```", Joiner.on("\n").join(translations)))
                .build().singleton();
    }

    @Override
    public DiscordMessage help() {
        return DiscordMessage.newBuilder()
                .withText("```Usage: !lookup [no-pinyin|np] <text>\nExample: !lookup penis```")
                .build();
    }
}
