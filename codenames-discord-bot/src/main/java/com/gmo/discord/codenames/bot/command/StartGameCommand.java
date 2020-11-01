package com.gmo.discord.codenames.bot.command;

import com.gmo.discord.codenames.bot.entities.Team;
import com.gmo.discord.codenames.bot.entities.TeamType;
import com.gmo.discord.codenames.bot.game.CodeNames;
import com.gmo.discord.codenames.bot.game.CodeNamesBuilder;
import com.gmo.discord.codenames.bot.output.CodeNamesToPng;
import com.gmo.discord.codenames.bot.store.CodeNamesStore;
import com.gmo.discord.support.command.Command;
import com.gmo.discord.support.command.CommandInfo;
import com.gmo.discord.support.message.Color;
import com.gmo.discord.support.message.DiscordMessage;
import com.gmo.discord.support.message.Embed;
import com.google.common.collect.ImmutableList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * {@link Command} for starting a game if both teams are complete
 *
 * @author tedelen
 */
@Component
public class StartGameCommand implements Command {
    private static final List<String> TRIGGER = ImmutableList.of("!deal", "!start");

    private final CodeNamesStore store;

    @Autowired
    public StartGameCommand(final CodeNamesStore store) {
        this.store = Objects.requireNonNull(store);
    }

    @Override
    public boolean canExecute(final CommandInfo commandInfo) {
        return TRIGGER.contains(commandInfo.getCommand().toLowerCase());
    }

    @Override
    public Iterable<DiscordMessage> execute(final CommandInfo commandInfo) {
        final Optional<CodeNamesBuilder> gameBuilderOpt = store.getGameLobby(commandInfo.getChannel().orElseThrow());
        if (gameBuilderOpt.isEmpty()) {
            return DiscordMessage.newBuilder()
                    .withText("No games available to start. Use `!chodes` to start a new game.")
                    .build().singleton();
        }
        final CodeNamesBuilder gameBuilder = gameBuilderOpt.get();
        if (!gameBuilder.getRedTeam().isValid() || !gameBuilder.getBlueTeam().isValid()) {
            return DiscordMessage.newBuilder()
                    .withText("Teams must be full to start a new game.")
                    .build().singleton();
        }

        store.storeGame(commandInfo.getChannel().orElseThrow(), gameBuilder.build());
        store.deleteGameLobby(commandInfo.getChannel().orElseThrow());

        final CodeNames codeNames = store.getGame(commandInfo.getChannel().orElseThrow())
                .orElseThrow(() -> new RuntimeException("Failed to create game"));
        store.storeGame(commandInfo.getChannel().orElseThrow(), codeNames);
        final Team nextTeam = codeNames.getActiveTeam();
        final Team passiveTeam = codeNames.getPassiveTeam();
        return ImmutableList.of(
                DiscordMessage.newBuilder()
                        .withContent(CodeNamesToPng.INSTANCE.getPngBytes(codeNames.map(), false))
                        .build(),
                DiscordMessage.newBuilder()
                        .withEmbed(Embed.newBuilder()
                                .withTitle("New game started")
                                .withColor(nextTeam.getType() == TeamType.RED ? Color.RED : Color.BLUE)
                                .addContent(String.format("%s will give the first clue for the `%s` team. ", nextTeam.getClueGiver().getDisplayName(), nextTeam.getType()))
                                .addContent(String.format("%s will give clues for the `%s` team. ", passiveTeam.getClueGiver().getDisplayName(), passiveTeam.getType()))
                                .build())
                        .build(),
                DiscordMessage.newBuilder()
                        .withText(String.format("%s, when you're ready, use `!clue <word> <count>` to give a clue and provide your team with `count + 1` guesses.",
                                nextTeam.getClueGiver().getDisplayName()))
                        .build(),
                DiscordMessage.newBuilder()
                        .withText("You are the clue giver for the `red` team. Here's the game key.")
                        .withDirectRecipient(codeNames.getRedTeam().getClueGiver().getUser())
                        .withContent(CodeNamesToPng.INSTANCE.getPngBytes(codeNames.map(), true))
                        .build(),
                DiscordMessage.newBuilder()
                        .withText("You are the clue giver for the `blue` team. Here's the game key.")
                        .withDirectRecipient(codeNames.getBlueTeam().getClueGiver().getUser())
                        .withContent(CodeNamesToPng.INSTANCE.getPngBytes(codeNames.map(), true))
                        .build());
    }

    @Override
    public DiscordMessage help() {
        return DiscordMessage.newBuilder()
                .withText("Use `!deal` to start a game when all players are ready.")
                .build();
    }
}
