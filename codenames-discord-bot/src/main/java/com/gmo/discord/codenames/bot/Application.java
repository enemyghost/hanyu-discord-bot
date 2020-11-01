package com.gmo.discord.codenames.bot;

import com.gmo.discord.codenames.bot.config.CodeNamesBotProperties;
import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.lifecycle.ReadyEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(CodeNamesBotProperties.class)
public class Application implements ApplicationRunner {
    private final CodeNamesBotProperties codeNamesBotProperties;
    private final DiscordCodeNamesBot discordCodeNamesBot;

    @Autowired
    public Application(final CodeNamesBotProperties codeNamesBotProperties,
                       final DiscordCodeNamesBot discordCodeNamesBot) {
        this.codeNamesBotProperties = codeNamesBotProperties;
        this.discordCodeNamesBot = discordCodeNamesBot;
    }

    @Override
    public void run(final ApplicationArguments args)  {
        final DiscordClient client = DiscordClient.create(codeNamesBotProperties.getBotToken());
        final GatewayDiscordClient gateway = client.login().block();
        if (gateway != null) {
            gateway.on(ReadyEvent.class)
                    .subscribe(event -> System.out.println("Bot is ready. Gateway Version:" + event.getGatewayVersion()));
            gateway.on(MessageCreateEvent.class)
                    .flatMap(discordCodeNamesBot::onMessage)
                    .subscribe();
            gateway.onDisconnect().block();
        }
    }

    public static void main(final String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
