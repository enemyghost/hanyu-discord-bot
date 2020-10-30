package com.gmo.discord.codenames.bot.command;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.gmo.discord.codenames.bot.entities.Player;
import com.gmo.discord.codenames.bot.entities.Team;
import com.gmo.discord.codenames.bot.exception.GamePlayException;
import com.gmo.discord.codenames.bot.game.CodeNames;
import com.gmo.discord.codenames.bot.output.CodeNamesToPng;
import com.gmo.discord.codenames.bot.store.CodeNamesStore;
import com.gmo.discord.support.command.Command;
import com.gmo.discord.support.command.CommandInfo;
import com.gmo.discord.support.message.DiscordMessage;
import com.google.common.collect.ImmutableList;
import discord4j.core.object.entity.Member;

/**
 * @author tedelen
 */
public class GuessCommand implements Command {
    private static final List<String> TRIGGER = ImmutableList.of("guess", "!guess");

    private final CodeNamesStore store;

    public GuessCommand(final CodeNamesStore store) {
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
                        .withText("There is no active game, you cannot guess.")
                        .build().singleton();
            } else {
                return Collections.emptySet();
            }
        } else if (commandInfo.getArgs().length < 1) {
            return help().singleton();
        }

        final CodeNames codeNames = gameOpt.get();
        final String guess = String.join(" ", commandInfo.getArgs());
        try {
            final Member member = commandInfo.getMember().orElseThrow();
            final Player guesser = new Player(member);
            final int numGuesses = codeNames.revealCard(guesser, guess);
            final String resultMessage;
            if (numGuesses == 0) {
                if (codeNames.getGameState().isFinal()) {
                    final Team winner = codeNames.getWinner().orElseThrow(() -> new IllegalStateException("Game is over but no winner?"));
                    if (winner.getPlayers().stream().map(Player::getUser).anyMatch(t -> t.equals(member))) {
                        resultMessage = String.format("Nice! %s team wins!", winner.getType());
                    } else if (codeNames.assassinRevealed()) {
                        resultMessage = String.format("LMAO! %s got assassinated. %s team wins!", guesser.getDisplayName(), winner.getType());
                    } else {
                        resultMessage = String.format("%s handed the game away. %s team wins!", guesser.getDisplayName(), winner.getType());
                    }
                } else {
                    resultMessage = "Great. Now it's " +
                            codeNames.getActiveTeam().getType() + "'s turn. " +
                            codeNames.getActiveTeam().getClueGiver().getUser().getNicknameMention() +
                            ", give a clue when you're ready with `!clue <word> <count>`.";
                }
            } else {
                resultMessage = "Good guess. You have " + numGuesses + " guesses remaining. Use `!guess <word>` or `!pass`";
            }
            store.storeGame(commandInfo.getChannel().orElseThrow(), codeNames);
            final ImmutableList.Builder<DiscordMessage> result = ImmutableList.builder();
            result.add(DiscordMessage.newBuilder()
                    .withContent(CodeNamesToPng.INSTANCE.getPngBytes(codeNames.map(), codeNames.getGameState().isFinal()))
                    .build());
            result.add(DiscordMessage.newBuilder()
                    .withText(resultMessage)
                    .build());
            if (codeNames.getGameState().isFinal()) {
                result.add(DiscordMessage.newBuilder()
                        .withText("Use `!rechode` to play again with the same teams.")
                        .build());
            }
            return result.build();
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
                .withText("Use `!guess <word>` to make a guess.")
                .build();
    }
}
