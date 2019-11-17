package com.gmo.discord.matchups.bot;

import com.gmo.matchup.api.client.MatchupApiFeignClientFactory;
import discord4j.core.DiscordClient;
import discord4j.core.DiscordClientBuilder;
import discord4j.core.event.domain.lifecycle.ReadyEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;

public class Application {
    public static void main(String[] args) {
        final String token = System.getenv("MATCHUPS_BOT_TOKEN");
        if (token == null || token.isEmpty()) {
            throw new IllegalStateException("Could not get bot token");
        }

        final DiscordMatchupsApiBot apiBot = new DiscordMatchupsApiBot(MatchupApiFeignClientFactory.createDefault(System.getenv("MATCHUPS_BASE_URL")));
        final DiscordClient client = new DiscordClientBuilder(token).build();
        client.getEventDispatcher().on(ReadyEvent.class)
                .subscribe(event -> System.out.println("Bot is ready. Gateway Version:" +  event.getGatewayVersion()));
        client.getEventDispatcher().on(MessageCreateEvent.class)
                .flatMap(apiBot::onMessage)
                .subscribe();
        client.login().block();
    }
}
