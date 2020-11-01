package com.gmo.discord.codenames.bot;

import com.gmo.discord.codenames.bot.config.CodeNamesBotProperties;
import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.lifecycle.ReadyEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * Code Names Bot entry point
 *
 * @author tedelen
 */
@SpringBootApplication
@EnableConfigurationProperties(CodeNamesBotProperties.class)
public class CodeNamesDiscordBot implements ApplicationRunner {
    private static final Logger LOGGER = LoggerFactory.getLogger(CodeNamesDiscordBot.class);
    private final CodeNamesBotProperties codeNamesBotProperties;
    private final CodeNamesCommandDispatcher discordCodeNamesBot;

    @Autowired
    public CodeNamesDiscordBot(final CodeNamesBotProperties codeNamesBotProperties,
                               final CodeNamesCommandDispatcher discordCodeNamesBot) {
        this.codeNamesBotProperties = codeNamesBotProperties;
        this.discordCodeNamesBot = discordCodeNamesBot;
    }

    @Override
    public void run(final ApplicationArguments args)  {
        final DiscordClient client = DiscordClient.create(codeNamesBotProperties.getBotToken());
        final GatewayDiscordClient gateway = client.login().block();
        if (gateway != null) {
            gateway.on(ReadyEvent.class)
                    .subscribe(event -> LOGGER.info("Bot is ready. Gateway Version:" + event.getGatewayVersion()));
            gateway.on(MessageCreateEvent.class)
                    .flatMap(discordCodeNamesBot::onMessage)
                    .subscribe();
            gateway.onDisconnect().block();
        } else {
            LOGGER.error("Could not get gateway from client");
        }
    }

    public static void main(final String[] args) {
        SpringApplication.run(CodeNamesDiscordBot.class, args);
    }
}
