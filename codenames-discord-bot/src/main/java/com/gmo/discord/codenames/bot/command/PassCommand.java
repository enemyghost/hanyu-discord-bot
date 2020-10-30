package com.gmo.discord.codenames.bot.command;

import com.gmo.discord.codenames.bot.entities.Player;
import com.gmo.discord.codenames.bot.exception.GamePlayException;
import com.gmo.discord.codenames.bot.game.CodeNames;
import com.gmo.discord.codenames.bot.output.CodeNamesToPng;
import com.gmo.discord.codenames.bot.store.CodeNamesStore;
import com.gmo.discord.support.command.Command;
import com.gmo.discord.support.command.CommandInfo;
import com.gmo.discord.support.message.DiscordMessage;
import com.google.common.collect.ImmutableList;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * @author tedelen
 */
public class PassCommand implements Command {
    private static final List<String> TRIGGER = ImmutableList.of("!pass");

    private final CodeNamesStore store;

    public PassCommand(final CodeNamesStore store) {
        this.store = Objects.requireNonNull(store);
    }

    @Override
    public boolean canExecute(final CommandInfo commandInfo) {
        return TRIGGER.contains(commandInfo.getCommand().toLowerCase());
    }

    @Override
    public Iterable<DiscordMessage> execute(final CommandInfo commandInfo) {
        final Optional<CodeNames> gameOpt = store.getGame(commandInfo.getChannel().orElseThrow());
        if (!gameOpt.isPresent() || gameOpt.get().getGameState().isFinal()) {
            if (commandInfo.getCommand().startsWith("!")) {
                return DiscordMessage.newBuilder()
                        .withText("There is no active game, you cannot pass.")
                        .build().singleton();
            } else {
                return Collections.emptySet();
            }
        }

        final CodeNames codeNames = gameOpt.get();
        try {
            final Player passer = new Player(commandInfo.getMember().orElseThrow());
            codeNames.pass(passer);
            store.storeGame(commandInfo.getChannel().orElseThrow(), codeNames);
            return ImmutableList.of(DiscordMessage.newBuilder()
                            .withContent(CodeNamesToPng.INSTANCE.getPngBytes(codeNames.map(), false))
                            .build(),
                    DiscordMessage.newBuilder()
                            .appendText(passer.getDisplayName() + " passed; better luck next time. It's " + codeNames.getActiveTeam().getType() + "'s turn. ")
                            .appendText(codeNames.getActiveTeam().getClueGiver().getUser().getNicknameMention() + ", give a clue when you're ready with `!clue <word> <count>`.")
            .build());
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
                .withText("Use `!pass` to pass on your turn.")
                .build();
    }
}
