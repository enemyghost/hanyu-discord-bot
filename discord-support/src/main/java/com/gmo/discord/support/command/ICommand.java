package com.gmo.discord.support.command;

import com.gmo.discord.support.message.DiscordMessage;

/**
 * @author tedelen
 */
public interface ICommand {
    boolean canHandle(final CommandInfo commandInfo);
    Iterable<DiscordMessage> execute(final CommandInfo commandInfo);
    DiscordMessage help();
}
