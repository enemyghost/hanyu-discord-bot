package com.gmo.discord.matchups.bot.command;

import reactor.core.publisher.Mono;

import java.util.Collection;

public interface Command {
    boolean canExecute(final CommandInfo commandInfo);
    DiscordMessage execute(final CommandInfo commandInfo);
}
