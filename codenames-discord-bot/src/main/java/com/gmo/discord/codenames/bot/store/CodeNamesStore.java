package com.gmo.discord.codenames.bot.store;

import java.util.Optional;

import com.gmo.discord.codenames.bot.game.CodeNames;
import com.gmo.discord.codenames.bot.game.CodeNamesBuilder;
import sx.blah.discord.handle.obj.IChannel;

/**
 * @author tedelen
 */
public interface CodeNamesStore {
    Optional<CodeNames> getGame(final IChannel channel);
    void storeGame(final IChannel channel, final CodeNames game);
    Optional<CodeNames> deleteGame(final IChannel channel);

    Optional<CodeNamesBuilder> getGameLobby(final IChannel channel);
    void storeGameLobby(final IChannel channel, final CodeNamesBuilder lobby);
    Optional<CodeNamesBuilder> deleteGameLobby(final IChannel channel);
}
