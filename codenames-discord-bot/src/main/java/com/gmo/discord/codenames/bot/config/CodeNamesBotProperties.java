package com.gmo.discord.codenames.bot.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;

@ConfigurationProperties
@Validated
public class CodeNamesBotProperties {
    @NotBlank
    private String codenamesBotToken;

    public String getCodenamesBotToken() {
        return codenamesBotToken;
    }

    public void setCodenamesBotToken(final String codenamesBotToken) {
        this.codenamesBotToken = codenamesBotToken;
    }
}
