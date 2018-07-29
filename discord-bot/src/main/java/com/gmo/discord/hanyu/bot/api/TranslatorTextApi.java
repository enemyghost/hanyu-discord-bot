package com.gmo.discord.hanyu.bot.api;

import java.io.IOException;

/**
 * @author tedelen
 */
public interface TranslatorTextApi {
    TranslationResponse translate(final TranslationRequest request) throws IOException;
}
