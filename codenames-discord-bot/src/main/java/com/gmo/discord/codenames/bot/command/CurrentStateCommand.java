package com.gmo.discord.codenames.bot.command;

import com.gmo.discord.codenames.bot.entities.Player;
import com.gmo.discord.codenames.bot.game.CodeNames;
import com.gmo.discord.codenames.bot.output.CodeNamesToPng;
import com.gmo.discord.codenames.bot.store.CodeNamesStore;
import com.gmo.discord.support.command.Command;
import com.gmo.discord.support.command.CommandInfo;
import com.gmo.discord.support.message.DiscordMessage;
import com.google.common.collect.ImmutableList;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author tedelen
 */
public class CurrentStateCommand implements Command {
    private static final String TRIGGER = "!board";

    private final CodeNamesStore store;

    public CurrentStateCommand(final CodeNamesStore store) {
        this.store = Objects.requireNonNull(store);
    }

    @Override
    public boolean canExecute(final CommandInfo commandInfo) {
        return commandInfo.getCommand().equalsIgnoreCase(TRIGGER);
    }

    @Override
    public Iterable<DiscordMessage> execute(final CommandInfo commandInfo) {
        final Optional<CodeNames> game = store.getGame(commandInfo.getChannel().orElseThrow());
        if (game.isEmpty()) {
            return DiscordMessage.newBuilder()
                    .withText("No current game. Use `!chodes` to start a new game.")
                    .build().singleton();
        }

        final String text;
        if (game.get().getWinner().isPresent()) {
            text = String.format("The %s team won.", game.get().getWinner().get().getType());
        } else {
            final int guessesRemaining = game.get().getGuessesRemaining();
            text = guessesRemaining > 0
                    ? String.format("Waiting on %s to guess. The clue is %s; you have %d guesses. Use `!guess <word>` to make a guess or `!pass` to pass!",
                            game.get().getActiveTeam().getGuessers().stream().map(Player::getDisplayName).collect(Collectors.joining(" and ")),
                            game.get().getActiveClue().orElse(""),
                            guessesRemaining)
                    : String.format("Waiting on %s to give a clue for the %s team. Use `!clue <word> <count>` to give a clue!",
                            game.get().getActiveTeam().getClueGiver().getDisplayName(),
                            game.get().getActiveTeam().getType());
        }
        return ImmutableList.of(
                DiscordMessage.newBuilder()
                        .withContent(CodeNamesToPng.INSTANCE.getPngBytes(game.get().map(), false))
                        .build(),
                DiscordMessage.newBuilder()
                        .withText(text)
                        .build());
    }

    @Override
    public DiscordMessage help() {
        return DiscordMessage.newBuilder()
                .withText("Use `!board` to see the state of the current game.")
                .build();
    }
}
