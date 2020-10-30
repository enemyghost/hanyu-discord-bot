package com.gmo.discord.codenames.bot;

import com.gmo.discord.codenames.bot.store.CodeNamesStore;
import com.gmo.discord.codenames.bot.store.InMemoryCodeNamesStore;
import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.lifecycle.ReadyEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;

public class Application {
    public static void main(String[] args) {
        final String token = System.getenv("CODENAMES_BOT_TOKEN");
        if (token == null || token.isEmpty()) {
            throw new IllegalStateException("Could not get bot token");
        }

        final CodeNamesStore codeNamesStore = new InMemoryCodeNamesStore();
        final DiscordCodeNamesBot apiBot = new DiscordCodeNamesBot(codeNamesStore);
        final DiscordClient client = DiscordClient.create(token);
        final GatewayDiscordClient gateway = client.login().block();

        gateway.on(ReadyEvent.class)
                .subscribe(event -> System.out.println("Bot is ready. Gateway Version:" +  event.getGatewayVersion()));
        gateway.on(MessageCreateEvent.class)
                .flatMap(apiBot::onMessage)
                .subscribe();
        gateway.onDisconnect().block();
    }
}
