package com.gmo.discord.hanyu.bot.command;


import java.io.IOException;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gmo.discord.hanyu.bot.api.TranslatorTextApi;
import com.gmo.discord.hanyu.bot.api.entities.Translation;
import com.gmo.discord.hanyu.bot.api.entities.TranslationRequest;
import com.gmo.discord.hanyu.bot.api.entities.TranslationResponse;
import com.gmo.discord.hanyu.bot.api.entities.Transliteration;
import com.gmo.discord.hanyu.bot.message.HanyuMessage;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import sx.blah.discord.handle.obj.IMessage;

/**
 * {@link ICommand} which translates a detected language into Mandarin, pinyin, and English.
 *
 * @author tedelen
 */
public class TranslateCommand implements ICommand {
    private static final Logger LOGGER = LoggerFactory.getLogger(TranslateCommand.class);
    private static final Set<String> ALIASES = ImmutableSet.of("translate", "t", "tranny");
    private static final Set<String> PEENLESS_ALIASES = ImmutableSet.of("peenless", "pl", "no-pinyin", "np");
    private static final Set<String> HELP_ALIASES = ImmutableSet.of("help", "h");

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

        String textToTranslate = Joiner.on(" ").join(commandInfo.getArgs());

        final boolean peenless = Arrays.stream(commandInfo.getArgs()).anyMatch(PEENLESS_ALIASES::contains);
        textToTranslate = textToTranslate.replaceAll(String.join(" |", PEENLESS_ALIASES), "");

        if (HELP_ALIASES.stream().anyMatch(textToTranslate::equalsIgnoreCase)) {
            return help();
        }

        if (textToTranslate.trim().isEmpty()) {
            textToTranslate = commandInfo.getChannel().getMessageHistory(20).stream()
                    .filter(m -> !m.getAuthor().isBot()
                                    && m != commandInfo.getMessage()
                                    && !m.getContent().startsWith(commandInfo.getMessage().getContent().trim().substring(0, 1)))
                    .max(Comparator.comparingLong(t -> t.getTimestamp().toEpochSecond(ZoneOffset.UTC)))
                    .map(IMessage::getContent)
                    .orElse(textToTranslate.trim());

            if (Strings.isNullOrEmpty(textToTranslate)) {
                return HanyuMessage.newBuilder()
                        .withText("Could not find a recent message to translate. Bot messages and commands are ignored.")
                        .build();
            }
        }

        if (textToTranslate.length() > MAX_LENGTH) {
            return HanyuMessage.newBuilder()
                    .withText(String.format("Message of length %d exceeds max length %d", textToTranslate.length(), MAX_LENGTH))
                    .build();
        }

        final TranslationRequest.Builder requestBuilder = TranslationRequest.newBuilder()
                .addText(textToTranslate);

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
                .withText(String.format("```\n%s```", Joiner.on("\n").join(translations)))
                .build();
    }

    @Override
    public HanyuMessage help() {
        return HanyuMessage.newBuilder()
                .withText("```Usage: !translate[ no-pinyin|np][ <text>]\nExample: !translate 你好\n" +
                        "No arguments will translate the previous message.```")
                .build();
    }
}
