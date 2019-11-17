package com.gmo.discord.matchups.bot.command;

import com.gmo.matchup.analyzer.api.entities.reference.Sport;
import com.google.common.collect.ImmutableList;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class SportsCommand implements Command {
    public static final SportsCommand INSTANCE = new SportsCommand();

    private static final List<String> TRIGGER = ImmutableList.of("!sports");

    private SportsCommand() { }

    @Override
    public boolean canExecute(final CommandInfo commandInfo) {
        return TRIGGER.contains(commandInfo.getCommand().toLowerCase());
    }

    @Override
    public DiscordMessage execute(final CommandInfo commandInfo) {
        return DiscordMessage.newBuilder()
                .appendText("Available sports: ")
                .appendText(Arrays.stream(Sport.values()).map(Sport::getSportName).collect(Collectors.joining(", ")))
                .build();
    }
}
