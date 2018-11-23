package com.gmo.discord.codenames.bot.command;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.gmo.discord.codenames.bot.game.CodeNames;
import com.gmo.discord.codenames.bot.store.CodeNamesStore;
import com.gmo.discord.support.command.CommandInfo;
import com.gmo.discord.support.command.ICommand;
import com.gmo.discord.support.message.DiscordMessage;
import com.google.common.collect.ImmutableList;

/**
 * @author tedelen
 */
public class AbandonCommand implements ICommand {
    private static final List<String> TRIGGER = ImmutableList.of("!!abandon, !abandon");

    private final CodeNamesStore store;

    public AbandonCommand(final CodeNamesStore store) {
        this.store = Objects.requireNonNull(store);
    }

    @Override
    public boolean canHandle(final CommandInfo commandInfo) {
        return TRIGGER.contains(commandInfo.getCommand().toLowerCase());
    }

    @Override
    public Iterable<DiscordMessage> execute(final CommandInfo commandInfo) {
        if (commandInfo.getCommand().startsWith("!!")) {
            final Optional<CodeNames> gameOpt = store.getGame(commandInfo.getChannel());
            if (!gameOpt.isPresent() || gameOpt.get().getGameState().isFinal()) {
                return DiscordMessage.newBuilder()
                        .withText("There is no active game to abandon.")
                        .build().singleton();
            }

            store.deleteGameLobby(commandInfo.getChannel());
            return DiscordMessage.newBuilder()
                    .withText("Alright, quitter. The game has been deleted. Start a new game with `!chodes`")
                    .build()
                    .singleton();
        } else {
            return DiscordMessage.newBuilder()
                    .withText("Are you sure? If you really want to abandon the current game, use `!!abandon`")
                    .build()
                    .singleton();
        }
    }

    @Override
    public DiscordMessage help() {
        return DiscordMessage.newBuilder()
                .withText("Use `!abandon` to abandon the current game.")
                .build();
    }
}
