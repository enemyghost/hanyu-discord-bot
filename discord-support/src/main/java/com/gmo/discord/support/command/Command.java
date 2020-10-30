package com.gmo.discord.support.command;

import com.gmo.discord.support.message.DiscordMessage;

import java.util.Collection;

public interface Command {
    boolean canExecute(final CommandInfo commandInfo);
    Iterable<DiscordMessage> execute(final CommandInfo commandInfo);
    DiscordMessage help();
}
