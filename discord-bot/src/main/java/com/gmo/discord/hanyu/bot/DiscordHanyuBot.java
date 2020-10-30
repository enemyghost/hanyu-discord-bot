package com.gmo.discord.hanyu.bot;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gmo.discord.hanyu.bot.api.RetryingTranslatorTextApi;
import com.gmo.discord.hanyu.bot.api.TranslatorTextApi;
import com.gmo.discord.support.command.CommandInfo;
import com.gmo.discord.hanyu.bot.command.ExampleCommand;
import com.gmo.discord.hanyu.bot.command.LookupCommand;
import com.gmo.discord.hanyu.bot.command.TranslateCommand;
import com.gmo.discord.support.message.DiscordMessage;
import com.gmo.discord.hanyu.bot.microsoft.MicrosoftTranslatorTextApi;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
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

/**
 * Contains the engine code for the bot. Initiates the discord client and registers the bot listener.
 *
 * @author tedelen
 */
public class DiscordHanyuBot {
    private static final Logger LOGGER = LoggerFactory.getLogger(DiscordHanyuBot.class);
    private static final String DEFAULT_PREFIX = "!";
    private static IDiscordClient client;

    private final List<ICommand> commandList;
    private final String prefix;

    public static void main(String[] args) throws DiscordException, RateLimitException {
        final String token = System.getenv("HANYU_BOT_TOKEN");
        if (token == null || token.isEmpty()) {
            throw new IllegalStateException("Could not get bot token");
        }

        final TranslatorTextApi api = RetryingTranslatorTextApi.newBuilder()
                .withDelegate(MicrosoftTranslatorTextApi.newBuilder()
                        .withHttpClient(HttpClientBuilder.create().build())
                        .build())
                .build();

        String prefix = DEFAULT_PREFIX;
        for (int i = 0; i < args.length; i++) {
            if (args[i].equalsIgnoreCase("--prefix") && i+1 < args.length) {
                prefix = args[i+1];
                break;
            }
        }

        System.out.println("Logging bot in...");
        client = new ClientBuilder().withToken(token).build();
        client.getDispatcher().registerListener(new DiscordHanyuBot(api, prefix));
        client.login();
    }

    public DiscordHanyuBot(final TranslatorTextApi translatorTextApi, final String prefix) {
        this.commandList = ImmutableList.of(new TranslateCommand(translatorTextApi),
                new LookupCommand(translatorTextApi),
                new ExampleCommand(translatorTextApi));
        this.prefix = prefix;
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
            final String content;
            if (message.getContent().startsWith(prefix)) {
                content = prefix + message.getContent().substring(1).trim().replaceAll("\\s+", " ");
            } else {
                return;
            }

            final String[] split = content.split(" ");
            final String command = split[0].replaceFirst(prefix, "");
            final String[] args = split.length >= 2 ? Arrays.copyOfRange(split, 1, split.length) : new String[0];
            final CommandInfo commandInfo = CommandInfo.newBuilder()
                    .withChannel(channel)
                    .withGuild(guild)
                    .withMessage(message)
                    .withArgs(args)
                    .withCommand(command)
                    .withUser(message.getAuthor())
                    .build();
            final AtomicInteger messagesAgo = new AtomicInteger();
            commandList.stream().filter(t -> t.canHandle(commandInfo)).findFirst().ifPresent(cmd -> {
                final IMessage previousMessage = channel.getMessageHistory().stream()
                        .peek(t -> messagesAgo.incrementAndGet())
                        .filter(t -> t.getAuthor().equals(client.getOurUser()))
                        .findFirst()
                        .orElse(null);
                sendMessage(Iterables.getOnlyElement(cmd.execute(commandInfo)), messagesAgo.get() <= 5 ? previousMessage : null, channel);
            });
        } catch (final Exception e) {
            LOGGER.error("Exception processing message: " + message.getContent(), e);
        }
    }

    private void sendMessage(final DiscordMessage resultMessage,
                             final IMessage previousMessage,
                             final IChannel channel) {
        resultMessage.getEmbedObject().ifPresent(embedObject -> {
            if (resultMessage.isReplacePrevious() && previousMessage != null) {
                previousMessage.edit(embedObject);
            } else {
                channel.sendMessage(embedObject);
            }
        });
        resultMessage.getText().ifPresent(text -> {
            if (resultMessage.isReplacePrevious() && previousMessage != null) {
                previousMessage.edit(text);
            } else {
                channel.sendMessage(text);
            }
        });
    }
}
