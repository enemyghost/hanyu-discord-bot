package com.gmo.discord.hanyu.bot.command;

import com.gmo.discord.hanyu.bot.message.HanyuMessage;

/**
 * @author tedelen
 */
public interface ICommand {
    boolean canHandle(final CommandInfo commandInfo);
    HanyuMessage execute(final CommandInfo commandInfo);
    HanyuMessage help();
}
