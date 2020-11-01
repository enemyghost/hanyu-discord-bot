package com.gmo.discord.codenames.bot.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;

@ConfigurationProperties(prefix = "codenames")
@Validated
public class CodeNamesBotProperties {
    @NotBlank
    private String botToken;

    public String getBotToken() {
        return botToken;
    }

    public void setBotToken(final String botToken) {
        this.botToken = botToken;
    }
}
