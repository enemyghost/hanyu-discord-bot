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
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;

/**
 * Code Names Discord Bot command dispatcher
 */
@Service
public class CodeNamesCommandDispatcher {
    private static final Logger LOGGER = LoggerFactory.getLogger(CodeNamesCommandDispatcher.class);

    private final List<Command> commands;

    @Autowired
    public CodeNamesCommandDispatcher(final List<Command> commands) {
        this.commands = List.copyOf(commands);
    }

    /**
     * Handles a new message by parsing it and, if applicable, dispatching to a wired {@link Command}
     *
     * @param event {@link MessageCreateEvent} from discord4j
     * @return {@link Mono}
     */
    public Mono<Void> onMessage(final MessageCreateEvent event) {
        final Message message = event.getMessage();
        final Member member = message.getAuthorAsMember().block();
        final Channel channel = message.getChannel().block();
        final Guild guild = message.getGuild().block();

        // Ignore messages sent by bots
        if (member == null || member.isBot()) {
            return Mono.empty();
        }
        final CommandInfo commandInfo;
        try {
            final String content = message.getContent().trim().replaceAll("\\s+", " ");
            final String[] split = content.split(" ");
            final String command = split[0];
            final String[] args = split.length >= 2 ? Arrays.copyOfRange(split, 1, split.length) : new String[0];
            commandInfo = CommandInfo.newBuilder()
                    .withChannel(channel)
                    .withGuild(guild)
                    .withMessage(message.getContent())
                    .withArgs(args)
                    .withCommand(command)
                    .withMember(member)
                    .build();
        } catch (final Exception e) {
            LOGGER.error("Exception processing message: " + message.getContent(), e);
            return Mono.empty();
        }

        try {
            final Optional<Command> command = commands.stream()
                    .filter(t -> t.canExecute(commandInfo))
                    .findFirst();

            if (command.isEmpty()) {
                return Mono.empty();
            }

            return StreamSupport.stream(command.get().execute(commandInfo).spliterator(), false)
                    .map(outputMessage -> outputMessage.getDirectRecipient()
                            .<Mono<? extends Channel>>map(Member::getPrivateChannel)
                            .orElse(message.getChannel())
                            .flatMap(ch -> {
                                if (outputMessage.getEmbed().isPresent()) {
                                    return ch.getRestChannel().createMessage(outputMessage.getEmbed().get().toEmbedData());
                                } else if (outputMessage.getContent().isPresent()) {
                                    final MultipartRequest multipartRequest = new MultipartRequest(
                                            ImmutableMessageCreateRequest.builder().content(outputMessage.getText().orElse("")).build(),
                                            "chodeNames-" + Clock.systemUTC().millis() + ".png",
                                            new ByteArrayInputStream(outputMessage.getContent().get()));
                                    return ch.getRestChannel().createMessage(multipartRequest);
                                } else if (outputMessage.getText().isPresent()) {
                                    return ch.getRestChannel().createMessage(outputMessage.getText().get());
                                }
                                return Mono.empty();
                            }))
                    .reduce(Mono.empty(), Mono::then)
                    .doOnError(e -> LOGGER.error("Error occurred sending messages", e))
                    .then();
        } catch (final Exception e) {
            LOGGER.error("Something bad happened executing the command " + commandInfo.toString(), e);
            return Mono.empty();
        }
    }
}
