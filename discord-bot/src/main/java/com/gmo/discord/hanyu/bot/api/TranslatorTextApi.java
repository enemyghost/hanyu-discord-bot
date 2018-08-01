package com.gmo.discord.hanyu.bot.api;

import java.io.IOException;
import java.util.List;

import com.gmo.discord.hanyu.bot.api.entities.DetectionResponse;
import com.gmo.discord.hanyu.bot.api.entities.DictionaryLookupResponse;
import com.gmo.discord.hanyu.bot.api.entities.TranslationRequest;
import com.gmo.discord.hanyu.bot.api.entities.TranslationResponse;
import com.gmo.discord.hanyu.bot.api.entities.example.ExampleRequest;
import com.gmo.discord.hanyu.bot.api.entities.example.ExampleResponse;

/**
 * @author tedelen
 */
public interface TranslatorTextApi {
    List<TranslationResponse> translate(final TranslationRequest request) throws IOException;
    DetectionResponse detect(final TranslationRequest request) throws IOException;
    DictionaryLookupResponse lookup(final TranslationRequest request) throws IOException;
    ExampleResponse examples(final ExampleRequest request) throws IOException;
}
