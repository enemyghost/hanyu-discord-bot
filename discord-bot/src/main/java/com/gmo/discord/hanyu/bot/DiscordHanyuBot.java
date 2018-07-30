package com.gmo.discord.hanyu.bot;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.http.impl.client.HttpClientBuilder;

import com.gmo.discord.hanyu.bot.api.TranslatorTextApi;
import com.gmo.discord.hanyu.bot.command.CommandInfo;
import com.gmo.discord.hanyu.bot.command.ICommand;
import com.gmo.discord.hanyu.bot.command.TranslateCommand;
import com.gmo.discord.hanyu.bot.message.HanyuMessage;
import com.gmo.discord.hanyu.bot.microsoft.MicrosoftTranslatorTextApi;
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

/**
 * Contains the engine code for the bot. Initiates the discord client and registers the bot listener.
 *
 * @author tedelen
 */
public class DiscordHanyuBot {
    private static final String DEFAULT_PREFIX = "!";
    private static IDiscordClient client;

    private final List<ICommand> commandList;
    private final String prefix;

    public static void main(String[] args) throws DiscordException, RateLimitException {
        final String token = System.getenv("HANYU_BOT_TOKEN");
        if (token == null || token.isEmpty()) {
            throw new IllegalStateException("Could not get bot token");
        }

        final TranslatorTextApi api = MicrosoftTranslatorTextApi.newBuilder()
                .withHttpClient(HttpClientBuilder.create().build())
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
        this.commandList = ImmutableList.of(new TranslateCommand(translatorTextApi));
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

        if (user.isBot()) {
            return;
        }

        final IChannel channel = message.getChannel();
        final IGuild guild = message.getGuild();

        final String[] split = message.getContent().split(" ");

        if (split.length >= 1 && split[0].startsWith(prefix)) {
            String command = split[0].replaceFirst(prefix, "");
            String[] args = split.length >= 2 ? Arrays.copyOfRange(split, 1, split.length) : new String[0];

            final CommandInfo commandInfo = CommandInfo.newBuilder()
                    .withChannel(channel)
                    .withGuild(guild)
                    .withArgs(args)
                    .withCommand(command)
                    .withUser(message.getAuthor())
                    .build();
            final AtomicInteger messagesAgo = new AtomicInteger();
            commandList.stream().filter(t-> t.canHandle(commandInfo)).findFirst().ifPresent(cmd -> {
                final IMessage previousMessage = channel.getMessageHistory().stream()
                        .peek(t -> messagesAgo.incrementAndGet())
                        .filter(t -> t.getAuthor().equals(client.getOurUser()))
                        .findFirst()
                        .orElse(null);
                sendMessage(cmd.execute(commandInfo), messagesAgo.get() <= 5 ? previousMessage : null, channel);
            });
        }
    }

    private void sendMessage(final HanyuMessage resultMessage,
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
