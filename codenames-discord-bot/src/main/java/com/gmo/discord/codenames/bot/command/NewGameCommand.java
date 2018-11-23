package com.gmo.discord.codenames.bot.command;

import java.util.Objects;
import java.util.Optional;

import com.gmo.discord.codenames.bot.entities.Player;
import com.gmo.discord.codenames.bot.entities.TeamType;
import com.gmo.discord.codenames.bot.game.CodeNames;
import com.gmo.discord.codenames.bot.game.CodeNamesBuilder;
import com.gmo.discord.codenames.bot.store.CodeNamesStore;
import com.gmo.discord.support.command.CommandInfo;
import com.gmo.discord.support.command.ICommand;
import com.gmo.discord.support.message.DiscordMessage;

/**
 * @author tedelen
 */
public class NewGameCommand implements ICommand {
    private static final String TRIGGER = "!chodes";

    private final CodeNamesStore store;

    public NewGameCommand(final CodeNamesStore store) {
        this.store = Objects.requireNonNull(store);
    }

    @Override
    public boolean canHandle(final CommandInfo commandInfo) {
        return commandInfo.getCommand().equalsIgnoreCase(TRIGGER);
    }

    @Override
    public Iterable<DiscordMessage> execute(final CommandInfo commandInfo) {
        if (store.getGameLobby(commandInfo.getChannel()).isPresent()) {
            return DiscordMessage.newBuilder()
                    .withText("To join the next game, use `!join <red|blue>`")
                    .build().singleton();
        }
        final Optional<CodeNames> game = store.getGame(commandInfo.getChannel());
        if (game.isPresent() && !game.get().getGameState().isFinal()) {
            return DiscordMessage.newBuilder()
                    .appendText("Finish the current game before you start a new one. ")
                    .appendText("Use `!board` to see the current board. ")
                    .appendText("You can also use `!abandon` to abandon the current game.")
                    .build().singleton();
        }

        store.storeGameLobby(commandInfo.getChannel(), new CodeNamesBuilder()
                .addPlayer(new Player(commandInfo.getUser(), commandInfo.getUserName()), TeamType.RED));

        return DiscordMessage.newBuilder()
                .appendText("Let's play chode names! ")
                .appendText(commandInfo.getUserName() + " has started a game and joined the `red` team. ")
                .appendText("Use `!join[ red|blue]` to join a team.")
                .build().singleton();
    }

    @Override
    public DiscordMessage help() {
        return DiscordMessage.newBuilder()
                .appendText("Use `!chodes` to start a new chode game")
                .build();
    }
}
