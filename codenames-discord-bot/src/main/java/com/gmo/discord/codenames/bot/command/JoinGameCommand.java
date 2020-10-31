package com.gmo.discord.codenames.bot.command;

import com.gmo.discord.codenames.bot.entities.Player;
import com.gmo.discord.codenames.bot.entities.TeamType;
import com.gmo.discord.codenames.bot.game.CodeNamesBuilder;
import com.gmo.discord.codenames.bot.store.CodeNamesStore;
import com.gmo.discord.support.command.Command;
import com.gmo.discord.support.command.CommandInfo;
import com.gmo.discord.support.message.DiscordMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author tedelen
 */
@Component
public class JoinGameCommand implements Command {
    private static final String TRIGGER = "!join";

    private final CodeNamesStore store;

    @Autowired
    public JoinGameCommand(final CodeNamesStore store) {
        this.store = Objects.requireNonNull(store);
    }

    @Override
    public boolean canExecute(final CommandInfo commandInfo) {
        return commandInfo.getCommand().equalsIgnoreCase(TRIGGER);
    }

    @Override
    public Iterable<DiscordMessage> execute(final CommandInfo commandInfo) {
        final Optional<CodeNamesBuilder> gameBuilder = store.getGameLobby(commandInfo.getChannel().orElseThrow());
        if (gameBuilder.isEmpty()) {
            return DiscordMessage.newBuilder()
                    .withText("No available games to join. Use `!chodes` to start a new game.")
                    .build().singleton();
        }

        final Player p = new Player(commandInfo.getMember().orElseThrow());

        final String message;
        if (gameBuilder.get().getPlayers().contains(p)) {
            message = p.getDisplayName() +  " is already on a team. ";
        } else {
            final Optional<TeamType> teamType = commandInfo.getArg(0)
                    .map(t -> TeamType.valueOf(t.toUpperCase()));
            if (teamType.isPresent()) {
                gameBuilder.get().addPlayer(p, teamType.get());
            } else {
                gameBuilder.get().addPlayer(p);
            }
            message = p.getDisplayName() + " joined the game. ";
        }

        final String startGame =
                (gameBuilder.get().getRedTeam().isValid() && gameBuilder.get().getBlueTeam().isValid())
                        ? "\nUse `!deal` to start the game. "
                        : "";

        return DiscordMessage.newBuilder()
                .appendText(message)
                .appendText(startGame)
                .appendText("\nUse `!join <red|blue>` to join.")
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
                .withText("Use `!join <red|blue>` to join the red or blue team for the next game. " +
                        "Omit the team name to join the team who needs players.")
                .build();
    }
}
