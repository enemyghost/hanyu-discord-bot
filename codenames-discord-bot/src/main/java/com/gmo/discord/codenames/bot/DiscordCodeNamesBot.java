package com.gmo.discord.codenames.bot;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.Clock;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gmo.discord.codenames.bot.command.AbandonCommand;
import com.gmo.discord.codenames.bot.command.ClueCommand;
import com.gmo.discord.codenames.bot.command.CurrentStateCommand;
import com.gmo.discord.codenames.bot.command.DMCommand;
import com.gmo.discord.codenames.bot.command.GuessCommand;
import com.gmo.discord.codenames.bot.command.JoinGameCommand;
import com.gmo.discord.codenames.bot.command.LeaveGameCommand;
import com.gmo.discord.codenames.bot.command.NewGameCommand;
import com.gmo.discord.codenames.bot.command.NextGameCommand;
import com.gmo.discord.codenames.bot.command.PassCommand;
import com.gmo.discord.codenames.bot.command.StartGameCommand;
import com.gmo.discord.codenames.bot.store.CodeNamesStore;
import com.gmo.discord.codenames.bot.store.InMemoryCodeNamesStore;
import com.gmo.discord.support.command.CommandInfo;
import com.gmo.discord.support.command.ICommand;
import com.gmo.discord.support.message.DiscordMessage;
import com.google.common.collect.ImmutableList;
import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.ReadyEvent;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;

public class DiscordCodeNamesBot {
    private static final Logger LOGGER = LoggerFactory.getLogger(DiscordCodeNamesBot.class);

    private static IDiscordClient client;

    public static void main(String[] args) throws DiscordException, RateLimitException {
        final String token = System.getenv("CODENAMES_BOT_TOKEN");
        if (token == null || token.isEmpty()) {
            throw new IllegalStateException("Could not get bot token");
        }

        final CodeNamesStore codeNamesStore = new InMemoryCodeNamesStore();

        System.out.println("Logging bot in...");
        client = new ClientBuilder().withToken(token).build();
        client.getDispatcher().registerListener(new DiscordCodeNamesBot(codeNamesStore));
        client.login();
    }

    private final List<ICommand> commandList;

    public DiscordCodeNamesBot(final CodeNamesStore gameStore) {
        this.commandList = ImmutableList.of(
                new AbandonCommand(gameStore),
                new ClueCommand(gameStore),
                new CurrentStateCommand(gameStore),
                new GuessCommand(gameStore),
                new JoinGameCommand(gameStore),
                new LeaveGameCommand(gameStore),
                new NewGameCommand(gameStore),
                new NextGameCommand(gameStore),
                new PassCommand(gameStore),
                new StartGameCommand(gameStore),
                new DMCommand());
    }

    @EventSubscriber
    public void onReady(final ReadyEvent event) {
        System.out.println("Bot is now ready!");
    }

    @EventSubscriber
    public void onMessage(final MessageReceivedEvent event) throws RateLimitException, DiscordException, MissingPermissionsException {
        final IMessage message = event.getMessage();
        final IUser user = message.getAuthor();
        final IChannel channel = message.getChannel();
        final IGuild guild = message.getGuild();
        if (user.isBot()) {
            return;
        }

        try {
            final String content = message.getContent().trim().replaceAll("\\s+", " ");
            final String[] split = content.split(" ");
            final String command = split[0];
            final String[] args = split.length >= 2 ? Arrays.copyOfRange(split, 1, split.length) : new String[0];
            final CommandInfo commandInfo = CommandInfo.newBuilder()
                    .withChannel(channel)
                    .withGuild(guild)
                    .withMessage(message)
                    .withArgs(args)
                    .withCommand(command)
                    .withUser(message.getAuthor())
                    .build();

            commandList.stream().filter(t -> t.canHandle(commandInfo)).findFirst().ifPresent(cmd -> {
                try {
                    for (final DiscordMessage response : cmd.execute(commandInfo)) {
                        sendMessage(response, response.getDirectRecipient().<IChannel>map(IUser::getOrCreatePMChannel).orElse(channel));
                    }
                } catch (final Exception e) {
                    LOGGER.error("Something bad happened executing the command " + commandInfo.toString(), e);
                }
            });
        } catch (final Exception e) {
            LOGGER.error("Exception processing message: " + message.getContent(), e);
        }
    }

    private void sendMessage(final DiscordMessage resultMessage,
                             final IChannel channel) {
        if (resultMessage.getContent().isPresent()) {
            resultMessage.getContent().ifPresent(content -> {
                try (final ByteArrayInputStream bis = new ByteArrayInputStream(content)) {
                    channel.sendFile(resultMessage.getText().orElse(""),
                            bis,
                            "chodeNames-" + Clock.systemUTC().millis() + ".png");
                } catch (final IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
        } else if (resultMessage.getEmbedObject().isPresent()) {
            resultMessage.getEmbedObject().ifPresent(channel::sendMessage);
        } else if (resultMessage.getText().isPresent()) {
            resultMessage.getText().ifPresent(channel::sendMessage);
        }
    }
}
