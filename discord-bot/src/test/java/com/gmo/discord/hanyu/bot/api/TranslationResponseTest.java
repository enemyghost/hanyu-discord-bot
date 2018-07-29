package com.gmo.discord.hanyu.bot.api;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

public class TranslationResponseTest {
    private static final String RESPONSE_JSON = "[{\"detectedLanguage\":{\"language\":\"en\",\"score\":1.0},\"translations\":[{\"text\":\"你好\",\"to\":\"zh-Hans\"},{\"text\":\"你好\",\"transliteration\":{\"text\":\"nǐ hǎo\",\"script\":\"Latn\"},\"to\":\"zh-Hans\"}]}]";
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Test
    public void testDeser() throws IOException {
        final List<TranslationResponse> translationResponse = OBJECT_MAPPER.readValue(RESPONSE_JSON, new TypeReference<ArrayList<TranslationResponse>>() { });
        final TranslationResponse expected = TranslationResponse.newBuilder()
                .withTranslations(ImmutableList.of(
                        Translation.newBuilder()
                                .withDestinationLanguage("zh-Hans")
                                .withText("你好")
                                .build(),
                        Translation.newBuilder()
                                .withDestinationLanguage("zh-Hans")
                                .withText("你好")
                                .withTransliteration(Transliteration.newBuilder()
                                        .withText("nǐ hǎo")
                                        .withScript("Latn")
                                        .build())
                                .build()
                ))
                .build();
        assertEquals(expected, Iterables.getOnlyElement(translationResponse));
    }
}
