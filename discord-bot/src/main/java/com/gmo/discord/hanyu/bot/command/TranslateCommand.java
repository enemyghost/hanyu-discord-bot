package com.gmo.discord.hanyu.bot.command;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
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
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

/**
 * {@link ICommand} which translates a detected language into Mandarin, pinyin, and English.
 *
 * @author tedelen
 */
public class TranslateCommand implements ICommand {
    private static final Logger LOGGER = LoggerFactory.getLogger(TranslateCommand.class);
    private static final Set<String> ALIASES = ImmutableSet.of("translate", "t", "tranny");
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

    @Override
    public HanyuMessage help() {
        return HanyuMessage.newBuilder()
                .withText("```Usage: !translate [no-pinyin] <text>\nExample: !translate 你好```")
                .build();
    }
}
