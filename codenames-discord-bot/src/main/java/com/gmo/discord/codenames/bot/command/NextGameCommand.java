package com.gmo.discord.codenames.bot.command;

import java.util.Objects;
import java.util.Optional;

import com.gmo.discord.codenames.bot.entities.Team;
import com.gmo.discord.codenames.bot.game.CodeNames;
import com.gmo.discord.codenames.bot.game.CodeNamesBuilder;
import com.gmo.discord.codenames.bot.output.CodeNamesToPng;
import com.gmo.discord.codenames.bot.store.CodeNamesStore;
import com.gmo.discord.support.command.Command;
import com.gmo.discord.support.command.CommandInfo;
import com.gmo.discord.support.message.DiscordMessage;
import com.google.common.collect.ImmutableList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * {@link Command} for beginning a new game with the same teams after a game is completed
 *
 * @author tedelen
 */
@Component
public class NextGameCommand implements Command {
    private static final String TRIGGER = "!rechode";

    private final CodeNamesStore store;

    @Autowired
    public NextGameCommand(final CodeNamesStore store) {
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
                    .withText("No current game, you cannot rechode. Use `!chodes` to start a new game.")
                    .build().singleton();
        } else if (!game.get().getGameState().isFinal()) {
            return DiscordMessage.newBuilder()
                    .withText("Hey chode! Finish the current game before starting a new one.")
                    .build().singleton();
        }

        final CodeNames codeNames = new CodeNamesBuilder(game.get()).build();
        final Team nextTeam = codeNames.getActiveTeam();
        store.storeGame(commandInfo.getChannel().orElseThrow(), codeNames);
        return ImmutableList.of(
                DiscordMessage.newBuilder()
                        .appendText("Starting a new game with the same teams. ")
                        .appendText(nextTeam.getClueGiver().getDisplayName() + " will give the first clue for the " + nextTeam.getType() + " team.")
                        .withContent(CodeNamesToPng.INSTANCE.getPngBytes(codeNames.map(), false))
                        .build(),
                DiscordMessage.newBuilder()
                        .withText("You are the clue giver for the RED team. Here's the game key.")
                        .withDirectRecipient(codeNames.getRedTeam().getClueGiver().getUser())
                        .withContent(CodeNamesToPng.INSTANCE.getPngBytes(codeNames.map(), true))
                        .build(),
                DiscordMessage.newBuilder()
                        .withText("You are the clue giver for the BLUE team. Here's the game key.")
                        .withDirectRecipient(codeNames.getBlueTeam().getClueGiver().getUser())
                        .withContent(CodeNamesToPng.INSTANCE.getPngBytes(codeNames.map(), true))
                        .build());
    }

    @Override
    public DiscordMessage help() {
        return DiscordMessage.newBuilder()
                .withText("Use `!rechode` after a game is completed to start a new game with the same teams. " +
                        "Rotates the clue giver and which team starts.")
                .build();
    }
}
