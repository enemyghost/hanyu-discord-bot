package com.gmo.discord.codenames.bot.command;

import com.gmo.discord.codenames.bot.entities.Player;
import com.gmo.discord.codenames.bot.entities.TeamType;
import com.gmo.discord.codenames.bot.game.CodeNames;
import com.gmo.discord.codenames.bot.game.CodeNamesBuilder;
import com.gmo.discord.codenames.bot.store.CodeNamesStore;
import com.gmo.discord.support.command.Command;
import com.gmo.discord.support.command.CommandInfo;
import com.gmo.discord.support.message.DiscordMessage;

import java.util.Objects;
import java.util.Optional;

/**
 * @author tedelen
 */
public class NewGameCommand implements Command {
    private static final String TRIGGER = "!chodes";

    private final CodeNamesStore store;

    public NewGameCommand(final CodeNamesStore store) {
        this.store = Objects.requireNonNull(store);
    }

    @Override
    public boolean canExecute(final CommandInfo commandInfo) {
        return commandInfo.getCommand().equalsIgnoreCase(TRIGGER);
    }

    @Override
    public Iterable<DiscordMessage> execute(final CommandInfo commandInfo) {
        if (store.getGameLobby(commandInfo.getChannel().orElseThrow()).isPresent()) {
            return DiscordMessage.newBuilder()
                    .withText("To join the next game, use `!join <red|blue>`")
                    .build().singleton();
        }
        final Optional<CodeNames> game = store.getGame(commandInfo.getChannel().orElseThrow());
        if (game.isPresent() && !game.get().getGameState().isFinal()) {
            return DiscordMessage.newBuilder()
                    .appendText("Finish the current game before you start a new one. ")
                    .appendText("Use `!board` to see the current board. ")
                    .appendText("You can also use `!abandon` to abandon the current game.")
                    .build().singleton();
        }

        final Player player = new Player(commandInfo.getMember().orElseThrow());
        store.storeGameLobby(commandInfo.getChannel().orElseThrow(), new CodeNamesBuilder()
                .addPlayer(player, TeamType.RED));

        return DiscordMessage.newBuilder()
                .appendText("Let's play chode names! ")
                .appendText(player.getDisplayName() + " has started a game and joined the `red` team. ")
                .appendText("Use `!join <red|blue>` to join a team.")
                .build().singleton();
    }

    @Override
    public DiscordMessage help() {
        return DiscordMessage.newBuilder()
                .appendText("Use `!chodes` to start a new chode game")
                .build();
    }
}
