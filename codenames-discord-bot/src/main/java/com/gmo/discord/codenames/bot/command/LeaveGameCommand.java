package com.gmo.discord.codenames.bot.command;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import com.gmo.discord.codenames.bot.entities.Player;
import com.gmo.discord.codenames.bot.game.CodeNamesBuilder;
import com.gmo.discord.codenames.bot.store.CodeNamesStore;
import com.gmo.discord.support.command.CommandInfo;
import com.gmo.discord.support.command.ICommand;
import com.gmo.discord.support.message.DiscordMessage;

public class LeaveGameCommand implements ICommand {
    private static final String TRIGGER = "!leave";

    private final CodeNamesStore store;

    public LeaveGameCommand(final CodeNamesStore store) {
        this.store = Objects.requireNonNull(store);
    }

    @Override
    public boolean canHandle(final CommandInfo commandInfo) {
        return commandInfo.getCommand().equalsIgnoreCase(TRIGGER);
    }

    @Override
    public Iterable<DiscordMessage> execute(final CommandInfo commandInfo) {
        final Optional<CodeNamesBuilder> gameBuilder = store.getGameLobby(commandInfo.getChannel());
        if (!gameBuilder.isPresent()) {
            if (store.getGame(commandInfo.getChannel()).map(t -> !t.getGameState().isFinal()).orElse(false)) {
                return DiscordMessage.newBuilder()
                        .withText("You can't leave a game that's in progress, don't be a lil bitch.")
                        .build().singleton();
            } else {
                return DiscordMessage.newBuilder()
                        .withText("There's no game to leave. Use `!chodes` to start a new game.")
                        .build().singleton();
            }
        }

        final Player p = new Player(commandInfo.getUser(), commandInfo.getUserName());
        final String message;
        if (!gameBuilder.get().getPlayers().contains(p)) {
            message = p.getDisplayName() +  " is not on any teams. ";
        } else {
            gameBuilder.get().removePlayer(p);
            message = p.getDisplayName() + " left the game. ";
        }

        return DiscordMessage.newBuilder()
                .appendText(message)
                .appendText("\nRed Team: ")
                .appendText(gameBuilder.get().getRedTeam().getPlayers().stream().map(Player::getDisplayName).collect(Collectors.joining(", ")))
                .appendText("\nBlue Team: ")
                .appendText(gameBuilder.get().getBlueTeam().getPlayers().stream().map(Player::getDisplayName).collect(Collectors.joining(", ")))
                .build()
                .singleton();
    }

    @Override
    public DiscordMessage help() {
        return DiscordMessage.newBuilder()
                .withText("Use `!leave` to leave the game.")
                .build();
    }
}
