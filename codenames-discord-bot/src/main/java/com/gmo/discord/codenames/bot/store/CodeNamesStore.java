package com.gmo.discord.codenames.bot.store;

import com.gmo.discord.codenames.bot.game.CodeNames;
import com.gmo.discord.codenames.bot.game.CodeNamesBuilder;
import discord4j.core.object.entity.channel.Channel;

import java.util.Optional;

/**
 * @author tedelen
 */
public interface CodeNamesStore {
    Optional<CodeNames> getGame(final Channel channel);
    void storeGame(final Channel channel, final CodeNames game);
    Optional<CodeNames> deleteGame(final Channel channel);

    Optional<CodeNamesBuilder> getGameLobby(final Channel channel);
    void storeGameLobby(final Channel channel, final CodeNamesBuilder lobby);
    Optional<CodeNamesBuilder> deleteGameLobby(final Channel channel);
}
