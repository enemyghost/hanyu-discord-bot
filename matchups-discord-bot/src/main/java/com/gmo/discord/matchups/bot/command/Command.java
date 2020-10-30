package com.gmo.discord.matchups.bot.command;

public interface Command {
    boolean canExecute(final CommandInfo commandInfo);
    DiscordMessage execute(final CommandInfo commandInfo);
}
