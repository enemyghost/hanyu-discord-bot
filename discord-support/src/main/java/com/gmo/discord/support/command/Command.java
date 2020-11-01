package com.gmo.discord.support.command;

import com.gmo.discord.support.message.DiscordMessage;

import java.util.Collection;

/**
 * A command
 */
public interface Command {
    /**
     * Determines if the command can operate on the given parsed {@link CommandInfo}
     *
     * Callers should ensure this method returns true before calling {@link #execute(CommandInfo)}, otherwise
     * unexpected behavior may occur / exceptions may be thrown.
     *
     * @param commandInfo {@link CommandInfo} parsed from a discord message
     * @return true if this command can operate on the message, false otherwise
     */
    boolean canExecute(final CommandInfo commandInfo);

    /**
     * Executes the given command. Callers should first ensure {@link #canExecute(CommandInfo)} returns true
     * for the same input.
     *
     * @param commandInfo {@link CommandInfo} parsed from a discord message
     * @return one or more messages that should be sent to the discord channel as a result of the command's execution
     */
    Iterable<DiscordMessage> execute(final CommandInfo commandInfo);

    /**
     * Describes how to use this command in a single {@link DiscordMessage}
     *
     * @return a message to be sent to the discord channel describing how to use this command
     */
    DiscordMessage help();
}
