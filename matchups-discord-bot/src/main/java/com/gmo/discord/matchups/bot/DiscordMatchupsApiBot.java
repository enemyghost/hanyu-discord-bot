package com.gmo.discord.matchups.bot;

import com.gmo.discord.matchups.bot.command.Command;
import com.gmo.discord.matchups.bot.command.CommandInfo;
import com.gmo.discord.matchups.bot.command.MatchupCommand;
import com.gmo.discord.matchups.bot.command.MatchupsCommand;
import com.gmo.discord.matchups.bot.command.SportsCommand;
import com.gmo.matchup.api.client.MatchupApiClient;
import com.google.common.collect.ImmutableList;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class DiscordMatchupsApiBot {
    private static final Logger LOGGER = LoggerFactory.getLogger(DiscordMatchupsApiBot.class);

    private final List<Command> commands;

    public DiscordMatchupsApiBot(final MatchupApiClient matchupApiClient) {
        this.commands = ImmutableList.of(new MatchupsCommand(Objects.requireNonNull(matchupApiClient)),
                new MatchupCommand(matchupApiClient),
                SportsCommand.INSTANCE);
    }

    public void onReady() {
        System.out.println("Bot is now ready!");
    }

    public Mono<Void> onMessage(final MessageCreateEvent messageCreateEvent) {
        final Message message = messageCreateEvent.getMessage();
        final Member member = message.getAuthorAsMember().block();

        if (member == null || member.isBot()) {
            return Mono.empty();
        }

        final Guild guild = message.getGuild().block();

        final String content = message.getContent().orElse("")
                .trim().replaceAll("\\s+", " ");
        final String[] split = content.split(" ");
        final String command = split[0];
        final String[] args = split.length >= 2 ? Arrays.copyOfRange(split, 1, split.length) : new String[0];
        final CommandInfo commandInfo = CommandInfo.newBuilder()
                .withChannel(message.getChannel().block())
                .withGuild(guild)
                .withMessage(content)
                .withArgs(args)
                .withCommand(command)
                .withMember(member)
                .build();

        final Optional<Command> first = commands.stream().filter(t -> t.canExecute(commandInfo)).findFirst();
        if (first.isEmpty()) {
            return Mono.empty();
        } else {
            try {
                final Command cmd = first.get();
                return message.getChannel()
                        .flatMap(channel -> channel.createMessage(cmd.execute(commandInfo).getText().orElse("")))
                        .then();
            } catch (final Exception e) {
                LOGGER.error("Something bad happened executing the command " + commandInfo.toString(), e);
                return Mono.empty();
            }
        }
    }
}
