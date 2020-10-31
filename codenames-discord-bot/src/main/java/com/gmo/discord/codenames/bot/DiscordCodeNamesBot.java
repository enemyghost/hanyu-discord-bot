package com.gmo.discord.codenames.bot;

import com.gmo.discord.support.command.Command;
import com.gmo.discord.support.command.CommandInfo;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.Channel;
import discord4j.discordjson.json.ImmutableMessageCreateRequest;
import discord4j.rest.util.MultipartRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.io.ByteArrayInputStream;
import java.time.Clock;
import java.util.Arrays;
import java.util.List;
import java.util.stream.StreamSupport;

@Service
public class DiscordCodeNamesBot {
    private static final Logger LOGGER = LoggerFactory.getLogger(DiscordCodeNamesBot.class);

    private final List<Command> commands;

    @Autowired
    public DiscordCodeNamesBot(final List<Command> commands) {
        this.commands = List.copyOf(commands);
    }

    public Mono<Void> onMessage(final MessageCreateEvent event) {
        final Message message = event.getMessage();
        final Member member = message.getAuthorAsMember().block();
        final Channel channel = message.getChannel().block();
        final Guild guild = message.getGuild().block();
        if (member == null || member.isBot()) {
            return Mono.empty();
        }

        try {
            final String content = message.getContent().trim().replaceAll("\\s+", " ");
            final String[] split = content.split(" ");
            final String command = split[0];
            final String[] args = split.length >= 2 ? Arrays.copyOfRange(split, 1, split.length) : new String[0];
            final CommandInfo commandInfo = CommandInfo.newBuilder()
                    .withChannel(channel)
                    .withGuild(guild)
                    .withMessage(message.getContent())
                    .withArgs(args)
                    .withCommand(command)
                    .withMember(member)
                    .build();
            try {
                return commands.stream().filter(t -> t.canExecute(commandInfo))
                        .findFirst()
                        .map(c -> c.execute(commandInfo))
                        .map(messages ->
                                StreamSupport.stream(messages.spliterator(), false)
                                        .map(m -> m.getDirectRecipient()
                                                .<Mono<? extends Channel>>map(Member::getPrivateChannel)
                                                .orElse(message.getChannel())
                                                .flatMap(ch -> {
                                                    if (m.getEmbed().isPresent()) {
                                                        return ch.getRestChannel().createMessage(m.getEmbed().get().toEmbedData());
                                                    } else if (m.getContent().isPresent()) {
                                                        final MultipartRequest multipartRequest = new MultipartRequest(
                                                                ImmutableMessageCreateRequest.builder().content(m.getText().orElse("")).build(),
                                                                "chodeNames-" + Clock.systemUTC().millis() + ".png",
                                                                new ByteArrayInputStream(m.getContent().get()));
                                                        return ch.getRestChannel().createMessage(multipartRequest);
                                                    } else if (m.getText().isPresent()) {
                                                        return ch.getRestChannel().createMessage(m.getText().get());
                                                    }
                                                    return Mono.empty();
                                                }))
                                        .reduce(Mono.empty(), Mono::then)
                                        .doOnError(e -> LOGGER.error("Error occurred sending messages", e))
                                        .then())
                        .orElse(Mono.empty());
            } catch (final Exception e) {
                LOGGER.error("Something bad happened executing the command " + commandInfo.toString(), e);
                return Mono.empty();
            }
        } catch (final Exception e) {
            LOGGER.error("Exception processing message: " + message.getContent(), e);
            return Mono.empty();
        }
    }
}
