package com.gmo.discord.codenames.bot.command;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import com.gmo.discord.codenames.bot.entities.Player;
import com.gmo.discord.codenames.bot.exception.GamePlayException;
import com.gmo.discord.codenames.bot.game.CodeNames;
import com.gmo.discord.codenames.bot.store.CodeNamesStore;
import com.gmo.discord.support.command.Command;
import com.gmo.discord.support.command.CommandInfo;
import com.gmo.discord.support.message.DiscordMessage;
import com.google.common.collect.ImmutableList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * {@link Command} to provide a new clue
 *
 * @author tedelen
 */
@Component
public class ClueCommand implements Command {
    private static final List<String> TRIGGER = ImmutableList.of("clue", "!clue");

    private final CodeNamesStore store;

    @Autowired
    public ClueCommand(final CodeNamesStore store) {
        this.store = Objects.requireNonNull(store);
    }

    @Override
    public boolean canExecute(final CommandInfo commandInfo) {
        return TRIGGER.contains(commandInfo.getCommand().toLowerCase());
    }

    @Override
    public Iterable<DiscordMessage> execute(final CommandInfo commandInfo) {
        final Optional<CodeNames> gameOpt = store.getGame(commandInfo.getChannel().orElseThrow());
        if (gameOpt.isEmpty() || gameOpt.get().getGameState().isFinal()) {
            if (commandInfo.getCommand().startsWith("!")) {
                return DiscordMessage.newBuilder()
                        .withText("There is no active game, you cannot give a clue.")
                        .build().singleton();
            } else {
                return Collections.emptySet();
            }
        } else if (commandInfo.getArgCount() < 2) {
            return help().singleton();
        }

        final CodeNames game = gameOpt.get();
        final int lastIndex = commandInfo.getArgCount() - 1;
        final Optional<Integer> count = commandInfo.getIntArg(lastIndex);
        if (count.isEmpty()) {
            return help().singleton();
        }

        final String clue = Arrays.stream(commandInfo.getArgs()).limit(commandInfo.getArgs().length - 1)
                .collect(Collectors.joining(" "));
        try {
            final Player clueGiver = new Player(commandInfo.getMember().orElseThrow());
            final int numGuesses = game.giveClue(clueGiver, clue, count.get());
            store.storeGame(commandInfo.getChannel().orElseThrow(), game);
            final String mentions = game.getActiveTeam().getGuessers().stream()
                    .map(t -> t.getUser().getNicknameMention())
                    .collect(Collectors.joining(" "));
            return DiscordMessage.newBuilder()
                    .withText(String.format("The clue is `%s`. `%s` has %d guesses.",
                            clue, game.getActiveTeam().getType(), numGuesses))
                    .appendText(mentions + " Use `!guess <word>` to make a guess, or `!pass` to pass.")
                    .build()
                    .singleton();
        } catch (final GamePlayException e) {
            return DiscordMessage.newBuilder()
                    .withText(e.getMessage())
                    .build()
                    .singleton();
        }
    }

    @Override
    public DiscordMessage help() {
        return DiscordMessage.newBuilder()
                .withText("Use `!clue <word> <count>` to give a clue and provide `count + 1` guesses to your teammates.")
                .build();
    }
}
