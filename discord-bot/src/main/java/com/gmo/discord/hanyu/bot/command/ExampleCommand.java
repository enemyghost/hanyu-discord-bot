package com.gmo.discord.hanyu.bot.command;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gmo.discord.hanyu.bot.api.TranslatorTextApi;
import com.gmo.discord.hanyu.bot.api.entities.DetectionResponse;
import com.gmo.discord.hanyu.bot.api.entities.Translation;
import com.gmo.discord.hanyu.bot.api.entities.TranslationRequest;
import com.gmo.discord.hanyu.bot.api.entities.Transliteration;
import com.gmo.discord.hanyu.bot.api.entities.example.Example;
import com.gmo.discord.hanyu.bot.api.entities.example.ExampleRequest;
import com.gmo.discord.hanyu.bot.api.entities.example.ExampleResponse;
import com.gmo.discord.support.command.CommandInfo;
import com.gmo.discord.support.message.DiscordMessage;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;

/**
 * @author tedelen
 */
public class ExampleCommand implements ICommand {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExampleCommand.class);
    private static final int MAX_EXAMPLES = 3;
    private static final Splitter SPLITTER = Splitter.on(",");
    private static final Set<String> ALIASES = ImmutableSet.of("example", "examples", "e", "ex");
    private static final Set<String> PEENLESS_ALIASES = ImmutableSet.of("peenless", "pl", "no-pinyin", "np");
    private static final Set<String> HELP_ALIASES = ImmutableSet.of("help", "h");

    private static final int MAX_LENGTH = 200;

    private final TranslatorTextApi translateApi;

    public ExampleCommand(final TranslatorTextApi translateApi) {
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

        if (HELP_ALIASES.stream().anyMatch(textToTranslate::startsWith)) {
            return help().singleton();
        }

        if (textToTranslate.length() > MAX_LENGTH) {
            return DiscordMessage.newBuilder()
                    .withText(String.format("Message of length %d exceeds max length %d", textToTranslate.length(), MAX_LENGTH))
                    .build().singleton();
        }

        final List<String> parts = SPLITTER.splitToList(textToTranslate).stream().map(String::trim).collect(Collectors.toList());
        if (parts.size() == 0 || parts.size() > 2) {
            return help().singleton();
        }

        final String textOrTranslation = parts.get(0);
        final TranslationRequest detectionRequest = TranslationRequest.newBuilder()
                .addText(textOrTranslation)
                .build();
        try {
            final DetectionResponse response = translateApi.detect(detectionRequest);
            final boolean firstChinese = response.getLanguage().equalsIgnoreCase("zh-Hans");
            String chinese = firstChinese ? parts.get(0) : parts.size() == 2 ? parts.get(1) : "";
            String english = !firstChinese ? parts.get(0) : parts.size() == 2 ? parts.get(1) : "";
            if (firstChinese && parts.size() == 1) {
                english = translateApi.translate(TranslationRequest.newBuilder()
                        .withSourceLanguage("zh-Hans")
                        .addDestinationLanguage("en")
                        .addText(parts.get(0))
                        .build()).stream()
                        .findFirst()
                        .map(t -> t.getTranslations().get(0).getText())
                        .orElse("");
            } else if (parts.size() == 1) {
                chinese = translateApi.translate(TranslationRequest.newBuilder()
                        .withSourceLanguage("en")
                        .addDestinationLanguage("zh-Hans")
                        .addText(parts.get(0))
                        .build()).stream()
                        .findFirst()
                        .map(t -> t.getTranslations().get(0).getText())
                        .orElse("");
            }

            if (Strings.isNullOrEmpty(chinese) || Strings.isNullOrEmpty(english)) {
                return DiscordMessage.newBuilder()
                        .withText(String.format("Could not find translation for `%s`", parts.get(0)))
                        .build().singleton();
            }

            final ExampleRequest exampleRequest = ExampleRequest.newBuilder()
                    .withSourceLanguage("en")
                    .withDestinationLanguage("zh-Hans")
                    .withSourceText(english)
                    .withDestinationTranslation(chinese)
                    .build();
            final ExampleResponse examples = translateApi.examples(exampleRequest);
            if (examples.getExamples().size() == 0) {
                return DiscordMessage.newBuilder()
                        .withText(String.format("I couldn\'t find any examples for `%s`, `%s`", chinese, english))
                        .build().singleton();
            }

            final Map<String, Example> chineseToExamples = examples.getExamples().stream()
                    .limit(MAX_EXAMPLES)
                    .collect(Collectors.toMap(Example::getTargetSentence, Function.identity()));
            final Map<String, String> chineseToPinyin = new HashMap<>();
            if (!peenless) {
                final TranslationRequest pinyinRequest = TranslationRequest.newBuilder()
                        .withSourceLanguage("zh-Hans")
                        .addDestinationLanguage("zh-Latn")
                        .withText(chineseToExamples.keySet())
                        .build();
                chineseToPinyin.putAll(translateApi.translate(pinyinRequest)
                        .stream()
                        .map(r -> r.getTranslations().stream().filter(t -> t.getTransliteration().filter(tl -> tl.getScript().equals("Latn")).isPresent()).findFirst().orElse(null))
                        .filter(Objects::nonNull)
                        .collect(Collectors.toMap(Translation::getText, t -> t.getTransliteration().map(Transliteration::getText).orElse(""))));
            }

            final List<String> translations = chineseToExamples.entrySet().stream()
                    .map(e -> String.format("%s%s\n%s\n",
                            chineseToPinyin.containsKey(e.getKey())
                                    ? chineseToPinyin.get(e.getKey()) + "\n"
                                    : "",
                            e.getKey(),
                            e.getValue().getSourceSentence()))
                    .collect(Collectors.toList());

            return DiscordMessage.newBuilder()
                    .withText(String.format("```\n%s```", Joiner.on("\n").join(translations)))
                    .build().singleton();
        } catch (final IOException e) {
            LOGGER.error("Exception reaching ms translator api", e);
            return DiscordMessage.newBuilder()
                    .withText("Failure to reach MS API")
                    .build().singleton();
        }
    }

    @Override
    public DiscordMessage help() {
        return DiscordMessage.newBuilder()
                .withText("Usage: !examples[ no-pinyin|np] 你好, hello")
                .build();
    }
}
